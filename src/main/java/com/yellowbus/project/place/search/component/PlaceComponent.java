package com.yellowbus.project.place.search.component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yellowbus.project.place.search.controller.PlaceController;
import com.yellowbus.project.place.search.entity.HotKeyWord;
import com.yellowbus.project.place.search.entity.Member;
import com.yellowbus.project.place.search.entity.SearchHistory;
import com.yellowbus.project.place.search.entity.SearchResult;
import com.yellowbus.project.place.search.repository.HotKeyWordRepository;
import com.yellowbus.project.place.search.repository.SearchHistoryRepository;
import com.yellowbus.project.place.search.repository.SearchResultRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
public class PlaceComponent {

    SearchResultRepository searchResultRepository;
    SearchHistoryRepository searchHistoryRepository;
    HotKeyWordRepository hotKeyWordRepository;

    private static final Logger logger = LogManager.getLogger(PlaceComponent.class);

    final String kakaoApiKey = "KakaoAK a1ff1dac0d12a6bfe4d672cd00c1894b";
    final String kakaoUri = "https://dapi.kakao.com/v2/local/search/keyword.json";
    final String naverUri = "https://openapi.naver.com/v1/search/local.json";
    final String naverClientId = "4v8zp9lfz1ti3guCS8XC";
    final String naverClientSecret = "5GSV4Q3Rk8";

    @Async("threadPoolTakExecutor")
    public void saveSearchHistory(String searchWord, Member userInfo) {
        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setUserId(userInfo.getUserId());
        searchHistory.setUserName(userInfo.getUsername());
        searchHistory.setKeyWord(searchWord);
        searchHistory.setDate(new Date());

        searchHistoryRepository.save(searchHistory);

        logger.debug(" ========= PlaceComponent saveSearchHistory ========= ");
        logger.debug("  "+Thread.currentThread().getThreadGroup().getName());
        logger.debug("  "+Thread.currentThread().getName());
    }

    @Async("threadPoolTakExecutor")
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

        logger.debug(" ========= PlaceComponent saveHotKeyWord ========= ");
        logger.debug("  "+Thread.currentThread().getThreadGroup().getName());
        logger.debug("  "+Thread.currentThread().getName());
    }

    public Optional<SearchResult> findToCache(String searchWord) {
        return searchResultRepository.findById(searchWord);
    }

    @Async("threadPoolTakExecutor")
    public CompletableFuture<List<String>> kakaoPlaceAPI(String searchWord) {
        String uri = kakaoUri+"?page=1&size=10&sort=accuracy&query="+searchWord;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set("Authorization", kakaoApiKey);

        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, getHttpEntity(httpHeaders), String.class);

        Gson gson = new Gson();

        JsonObject jo = gson.fromJson(responseEntity.getBody(), JsonObject.class);

        JsonArray jsonElements = jo.getAsJsonArray("documents");
        List<String> kakaoPlaceList = new ArrayList<>();
        for (int i=0; i<jsonElements.size(); i++) {
            JsonObject jsonObject = jsonElements.get(i).getAsJsonObject();
            kakaoPlaceList.add(jsonObject.get("place_name").getAsString().replaceAll(" ", ""));
        }

        return CompletableFuture.completedFuture(kakaoPlaceList);
    }

    @Async("threadPoolTakExecutor")
    public CompletableFuture<List<String>> naverPlaceAPI(String searchWord) {
        String uri = naverUri+"?display=10&query="+searchWord;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set("X-Naver-Client-Id", naverClientId);
        httpHeaders.set("X-Naver-Client-Secret", naverClientSecret);

        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, getHttpEntity(httpHeaders), String.class);

        Gson gson = new Gson();
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

    public void saveSearchResult(String searchWord, String kakaoNaverPlace) {
        SearchResult searchResult = new SearchResult(searchWord, kakaoNaverPlace);
        searchResultRepository.save(searchResult);
    }

    public HttpEntity<String> getHttpEntity(HttpHeaders httpHeaders) {
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        return  entity;
    }

}
