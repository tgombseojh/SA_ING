package com.yellowbus.project.place.search.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.yellowbus.project.place.search.component.PlaceComponent;
import com.yellowbus.project.place.search.entity.*;
import com.yellowbus.project.place.search.exception.KakaoAPIException;
import com.yellowbus.project.place.search.exception.NaverAPIException;
import com.yellowbus.project.place.search.repository.HotKeyWordRepository;
import com.yellowbus.project.place.search.repository.SearchHistoryRepository;
import com.yellowbus.project.place.search.repository.SearchResultRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@AllArgsConstructor
@Service
public class PlaceService {

    PlaceComponent placeComponent;
    HotKeyWordRepository hotKeyWordRepository;
    SearchHistoryRepository searchHistoryRepository;
    SearchResultRepository searchResultRepository;

    Gson gson;

    private static final Logger logger = LogManager.getLogger(PlaceService.class);

    final String kakaoApiKey = "KakaoAK a1ff1dac0d12a6bfe4d672cd00c1894b";
    final String kakaoUri = "https://dapi.kakao.com/v2/local/search/keyword.json";
    final String naverUri = "https://openapi.naver.com/v1/search/local.json";
    final String naverClientId = "4v8zp9lfz1ti3guCS8XC";
    final String naverClientSecret = "5GSV4Q3Rk8";

    public HashMap<String, Object> v2Place(String searchWord, Member userInfo) throws Exception {
        logger.debug(" ========= PlaceService v2Place ========= ");
        logger.debug("  "+Thread.currentThread().getThreadGroup().getName());
        logger.debug("  "+Thread.currentThread().getName());

        // Async Job1 // 누가 언제 무엇을 검색했는지를 기록
        placeComponent.saveSearchHistory(searchWord, userInfo);

        // Async Job2 // 인기검색어 10개를 제공하기 위해서 검색할때마다 카운트 증가
        placeComponent.saveHotKeyWord(searchWord);

        // redis cache 를 도입하자.. 장소는 실시간으로 바뀌는 성격의 데이터가 아니므로, 캐시 유효시간은 1시간으로 설정하자..
        // 검색어가 cache 에 있으면 API를 호출하지 않는다..
        // 없으면, 호출하고 나서 그 결과를 cache 에 넣는다.
        Optional<SearchResult> cache = placeComponent.findToCache(searchWord);
        HashMap<String, Object> kakaoNaverPlace;
        if (cache.isEmpty()) {
            // Async Job3
            CompletableFuture<List<String>> task1 = placeComponent.kakaoPlaceAPI(searchWord);
            try {
                task1.join();
            } catch (Exception e) {
                throw new KakaoAPIException(e.getMessage());
            }

            // Async Job4
            CompletableFuture<List<String>> task2 = placeComponent.naverPlaceAPI(searchWord);
            try {
                task2.join();
            } catch (Exception e) {
                throw new NaverAPIException(e.getMessage());
            }

            // Async Job3 & 4 가 완료되면 정렬 및 합치기
            kakaoNaverPlace = task1.thenCombine(task2, (kakao, naver) -> placeComponent.combineKakaoAndNaver(kakao, naver)).get();

            // caching
            placeComponent.saveSearchResult(searchWord, gson.toJson(kakaoNaverPlace));
        } else {
            Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
            kakaoNaverPlace = gson.fromJson(cache.get().getResult(), type);
            logger.debug("result from cache : "+kakaoNaverPlace);
        }

        return kakaoNaverPlace;
    }

    @Async
    public void saveSearchHistory(String searchWord, Member userInfo) {
        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setUserId(userInfo.getUserId());
        searchHistory.setUserName(userInfo.getUsername());
        searchHistory.setKeyWord(searchWord);
        searchHistory.setDate(new Date());

        searchHistoryRepository.save(searchHistory);

        logger.debug(" searchHistoryRepository : "+Thread.currentThread().getName()+", "+Thread.currentThread().getThreadGroup().getName());
    }

    @Async
    @Transactional
    public void saveHotKeyWord(String searchWord) {
        HotKeyWord hotKeyWord = hotKeyWordRepository.findByKeyWord(searchWord);
        // 존재하는 키워드라면 횟수 증가 후 업뎃
        if (hotKeyWord!=null) {
            hotKeyWord.setSearchCount(hotKeyWord.getSearchCount()+1);
        } else {    // 존재하지 않으면 인서트
            hotKeyWord = new HotKeyWord();
            hotKeyWord.setKeyWord(searchWord);
            hotKeyWord.setSearchCount(1L);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            hotKeyWord.setDate(simpleDateFormat.format(new Date()));
        }
        hotKeyWordRepository.save(hotKeyWord);
    }

    public HttpEntity<String> getHttpEntity(HttpHeaders httpHeaders) {
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        return  entity;
    }

    @Async
    public CompletableFuture<List<String>> kakaoPlaceAPI(String searchWord) {
        String uri = kakaoUri+"?page=1&size=10&sort=accuracy&query="+searchWord;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set("Authorization", kakaoApiKey);
        logger.info(" 000000000000000 ");
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        logger.info(" 111111111111111 ");
        JsonObject jo = gson.fromJson(responseEntity.getBody(), JsonObject.class);
        logger.info(" 222222222222222 ");
        JsonArray jsonElements = jo.getAsJsonArray("documents");
        List<String> kakaoPlaceList = new ArrayList<>();
        for (int i=0; i<jsonElements.size(); i++) {
            JsonObject jsonObject = jsonElements.get(i).getAsJsonObject();
            kakaoPlaceList.add(jsonObject.get("place_name").getAsString().replaceAll(" ", ""));
        }

        return CompletableFuture.completedFuture(kakaoPlaceList);
    }

    @Async
    public CompletableFuture<List<String>> naverPlaceAPI(String searchWord) {
        String uri = naverUri+"?display=10&query="+searchWord;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set("X-Naver-Client-Id", naverClientId);
        httpHeaders.set("X-Naver-Client-Secret", naverClientSecret);

        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, getHttpEntity(httpHeaders), String.class);

        JsonObject jo = gson.fromJson(responseEntity.getBody(), JsonObject.class);

        JsonArray jsonElements = jo.getAsJsonArray("items");
        List<String> naverPlaceList = new ArrayList<>();
        for (int i=0; i<jsonElements.size(); i++) {
            JsonObject jsonObject = jsonElements.get(i).getAsJsonObject();
            naverPlaceList.add(jsonObject.get("title").getAsString().replaceAll(" ", "").replaceAll("<b>", "").replaceAll("</b>", ""));
        }

        return CompletableFuture.completedFuture(naverPlaceList);
    }

    public HashMap<String, Object> combineKakaoAndNaver(List<String> kakaoPlaceList, List<String> naverPlaceList) {
        // kakao 원본데이터 백업
        List<String> originKakaoPlaceList = new ArrayList<>(kakaoPlaceList);
        // naver 원본데이터 백업
        List<String> originNaverPlaceList = new ArrayList<>(naverPlaceList);

        // 교집합
        kakaoPlaceList.retainAll(naverPlaceList);

        // 파이널에 교집합을 먼저 넣는다
        List<String> finalPlaceList = new ArrayList<>(kakaoPlaceList);

        // 카카오에는 교집합만 있으므로 다시 원본으로 복원
        kakaoPlaceList.clear();
        kakaoPlaceList.addAll(originKakaoPlaceList);

        // 차집합
        kakaoPlaceList.removeAll(naverPlaceList);

        // 파이널에 카카오의 나머지 항목들을 넣는다
        finalPlaceList.addAll(kakaoPlaceList);

        // 카카오에는 차집합만 있으므로 다시 원본으로 복원
        kakaoPlaceList.clear();
        kakaoPlaceList.addAll(originKakaoPlaceList);

        // 반대로 차집합
        naverPlaceList.removeAll(kakaoPlaceList);

        // 파이널에 카카오의 나머지 항목들을 넣는다
        finalPlaceList.addAll(naverPlaceList);

        // 네이버에는 차집합만 있으므로 다시 원본으로 복원
        naverPlaceList.clear();
        naverPlaceList.addAll(originNaverPlaceList);

        logger.debug("");
        logger.debug(" origin kakao : "+originKakaoPlaceList);
        logger.debug(" origin naver : "+originNaverPlaceList);
        logger.debug(" final : "+finalPlaceList);
        logger.debug("");

        HashMap<String, String> hashMap;
        List<HashMap> list = new ArrayList<>();
        for(String placeName : finalPlaceList) {
            hashMap = new HashMap<>();
            hashMap.put("title", placeName);
            list.add(hashMap);
        }

        HashMap<String, Object> finalMap = new HashMap<>();
        finalMap.put("place", list);

        return finalMap;
    }

    public HashMap<String, Object> getSearchHistory(Member userInfo) {
        List<SearchHistory> searchHistoryList = searchHistoryRepository.findTop20ByUserIdOrderByDateDesc(userInfo.getUserId());

        String pattern = "yyyy-MM-dd hh:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        List<HashMap> resultList = new ArrayList<>();
        HashMap<String, String> hashMap;
        for(SearchHistory searchHistory : searchHistoryList) {
            hashMap = new HashMap<>();
            hashMap.put("date", simpleDateFormat.format(searchHistory.getDate()));
            hashMap.put("keyword", searchHistory.getKeyWord());

            resultList.add(hashMap);
        }

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("history", resultList);

        return resultMap;
    }

    public HashMap<String, Object> getHotKeyWord() {
        List<HotKeyWord> hotKeyWordList = hotKeyWordRepository.findTop10ByOrderBySearchCountDesc();

        List<HashMap> resultList = new ArrayList<>();
        HashMap<String, Object> hashMap;
        for(HotKeyWord hotKeyWord : hotKeyWordList) {
            hashMap = new HashMap<>();
            hashMap.put("keyword", hotKeyWord.getKeyWord());
            hashMap.put("search_count", hotKeyWord.getSearchCount());

            resultList.add(hashMap);
        }

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("hot10keywords", resultList);

        return resultMap;
    }

    public Optional<SearchResult> findToCache(String searchWord) {
        return searchResultRepository.findById(searchWord);
    }

    public void saveSearchResult(String searchWord, String kakaoNaverPlace) {
        SearchResult searchResult = new SearchResult(searchWord, kakaoNaverPlace);
        searchResultRepository.save(searchResult);
    }
}
