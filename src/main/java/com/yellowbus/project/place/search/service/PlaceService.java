package com.yellowbus.project.place.search.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yellowbus.project.place.search.component.PlaceComponent;
import com.yellowbus.project.place.search.entity.HotKeyWord;
import com.yellowbus.project.place.search.entity.Member;
import com.yellowbus.project.place.search.entity.SearchHistory;
import com.yellowbus.project.place.search.entity.SearchResult;
import com.yellowbus.project.place.search.exception.KakaoAPIException;
import com.yellowbus.project.place.search.exception.NaverAPIException;
import com.yellowbus.project.place.search.repository.HotKeyWordRepository;
import com.yellowbus.project.place.search.repository.SearchHistoryRepository;
import com.yellowbus.project.place.search.repository.SearchResultRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@AllArgsConstructor
@Service
@Slf4j
public class PlaceService {

    PlaceComponent placeComponent;
    HotKeyWordRepository hotKeyWordRepository;
    SearchHistoryRepository searchHistoryRepository;
    SearchResultRepository searchResultRepository;

    Gson gson;

    public HashMap<String, Object> v1Place(String searchWord, Member userInfo) throws ExecutionException, InterruptedException {
        log.info(" === 11111111111111111111 === ");

        // Async Job1 // 누가 언제 무엇을 검색했는지를 기록
        placeComponent.saveSearchHistory(searchWord, userInfo);

        log.info(" === 22222222222222222222 === ");

        // Async Job2 // 인기검색어 10개를 제공하기 위해서 검색할때마다 카운트 증가
        placeComponent.saveHotKeyWord(searchWord);

        log.info(" === 33333333333333333333 === ");

        // redis cache 를 도입하자.. 장소는 실시간으로 바뀌는 성격의 데이터가 아니므로, 캐시 유효시간은 1시간으로 설정하자..
        // 검색어가 cache 에 있으면 API를 호출하지 않는다..
        // 없으면, 호출하고 나서 그 결과를 cache 에 넣는다.
        // Async Job3
        Optional<SearchResult> cache = placeComponent.findToCache(searchWord);
        HashMap<String, Object> kakaoNaverPlace;
        if (cache.isEmpty()) {
            log.info(" === cache empty === ");

            CompletableFuture<List<String>> task1 = placeComponent.kakaoPlaceAPI(searchWord);
            try {
                task1.join();
            } catch (Exception e) {
                throw new KakaoAPIException(e.getMessage());
            }

            CompletableFuture<List<String>> task2 = placeComponent.naverPlaceAPI(searchWord);
            try {
                task2.join();
            } catch (Exception e) {
                throw new NaverAPIException(e.getMessage());
            }

            // Async task1 & task2 가 완료되면 정렬 및 합치기
            kakaoNaverPlace = task1.thenCombine(task2, (kakao, naver) -> placeComponent.combineKakaoAndNaver(kakao, naver)).get();

            // caching
            placeComponent.saveSearchResult(searchWord, gson.toJson(kakaoNaverPlace));
        } else {
            Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
            kakaoNaverPlace = gson.fromJson(cache.get().getResult(), type);
            log.debug("result from cache : "+kakaoNaverPlace);
        }

        log.info(" === 44444444444444444444 === ");

        return kakaoNaverPlace;
    }

    public HashMap<String, Object> getSearchHistory(Member userInfo) {
        log.debug(" ========= PlaceService getSearchHistory ========= ");
        log.debug("  "+Thread.currentThread().getThreadGroup().getName());
        log.debug("  "+Thread.currentThread().getName());

        List<SearchHistory> searchHistoryList = searchHistoryRepository.findTop20ByUserIdOrderByDateDesc(userInfo.getUserId());
        log.debug(" ========= searchHistoryList : "+searchHistoryList);

        return new SearchHistory().changeDateFormat(searchHistoryList);
    }

    public HashMap<String, Object> getHotKeyWord() {
        log.debug(" ========= PlaceService getHotKeyWord ========= ");
        log.debug("  "+Thread.currentThread().getThreadGroup().getName());
        log.debug("  "+Thread.currentThread().getName());

        List<HotKeyWord> hotKeyWordList = hotKeyWordRepository.findTop10ByOrderBySearchCountDesc();

        return new HotKeyWord().changeFromat(hotKeyWordList);
    }


}
