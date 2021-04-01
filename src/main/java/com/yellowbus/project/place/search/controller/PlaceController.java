package com.yellowbus.project.place.search.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yellowbus.project.place.search.entity.Member;
import com.yellowbus.project.place.search.entity.SearchResult;
import com.yellowbus.project.place.search.exception.KakaoAPIException;
import com.yellowbus.project.place.search.exception.NaverAPIException;
import com.yellowbus.project.place.search.service.PlaceService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@RestController
public class PlaceController {

    PlaceService placeService;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    Gson gson;

    private static final Logger logger = LogManager.getLogger(PlaceController.class);

    @GetMapping("/v1/place/{searchWord}")
    public Callable<HashMap<String, Object>> v1Place(@PathVariable String searchWord, Authentication authentication) throws Exception {

        Member userInfo = (Member)authentication.getPrincipal();

        logger.debug(" ========= place ========= ");
        logger.debug("  "+userInfo.getUsername());

        // Async Job1 // 누가 언제 무엇을 검색했는지를 기록
        placeService.saveSearchHistory(searchWord, userInfo);

        // Async Job2 // 인기검색어 10개를 제공하기 위해서 검색할때마다 카운트 증가
        placeService.saveHotKeyWord(searchWord);

        // redis cache 를 도입하자.. 장소는 실시간으로 바뀌는 성격의 데이터가 아니므로, 캐시 유효시간은 1시간으로 설정하자..
        // 검색어가 cache 에 있으면 API를 호출하지 않는다..
        // 없으면, 호출하고 나서 그 결과를 cache 에 넣는다.
        Optional<SearchResult> cache = placeService.findToCache(searchWord);
        HashMap<String, Object> kakaoNaverPlace;
        if (cache.isEmpty()) {
            // Async Job3
            CompletableFuture<List<String>> task1 = placeService.kakaoPlaceAPI(searchWord);
            try {
                task1.join();
            } catch (Exception e) {
                throw new KakaoAPIException(e.getMessage());
            }

            // Async Job4
            CompletableFuture<List<String>> task2 = placeService.naverPlaceAPI(searchWord);
            try {
                task2.join();
            } catch (Exception e) {
                throw new NaverAPIException(e.getMessage());
            }

            // Async Job3 & 4 가 완료되면 정렬 및 합치기
            kakaoNaverPlace = task1.thenCombine(task2, (kakao, naver) -> placeService.combineKakaoAndNaver(kakao, naver)).get();

            // caching
            placeService.saveSearchResult(searchWord, gson.toJson(kakaoNaverPlace));
        } else {
            Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
            kakaoNaverPlace = gson.fromJson(cache.get().getResult(), type);
            logger.debug("result from cache : "+kakaoNaverPlace);
        }

        return () -> {
            return kakaoNaverPlace;
        };

    }


    @GetMapping("/v1/search/history")
    public ResponseEntity<HashMap<String, Object>> history(Authentication authentication) {
        Member userInfo = (Member)authentication.getPrincipal();

        HashMap<String, Object> searchHistory = placeService.getSearchHistory(userInfo);

        return ResponseEntity.ok(searchHistory);
    }

    @GetMapping("/v1/search/hot10keywords")
    public ResponseEntity<HashMap<String, Object>> hotKeyWord() {

        HashMap<String, Object> hotKeyWords = placeService.getHotKeyWord();

        return ResponseEntity.ok(hotKeyWords);
    }

}
