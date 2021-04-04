package com.yellowbus.project.place.search.controller;

import com.google.gson.Gson;
import com.yellowbus.project.place.search.entity.Member;
import com.yellowbus.project.place.search.service.PlaceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@AllArgsConstructor
@RestController
@Slf4j
public class PlaceController {

    PlaceService placeService;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    AsyncTaskExecutor taskExecutor;
    Gson gson;

    @GetMapping("/v1/place/{searchWord}")
    public CompletableFuture<ResponseEntity<HashMap<String, Object>>> v3Place(@PathVariable String searchWord, Authentication authentication) {

        Member userInfo = (Member)authentication.getPrincipal();

        return CompletableFuture.supplyAsync(() -> {
            try {
                return new ResponseEntity<>(placeService.v1Place(searchWord, userInfo), HttpStatus.OK);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }, taskExecutor);
    }

    @GetMapping("/v1/search/history")
    public CompletableFuture<HashMap<String, Object>> history(Authentication authentication) {
        Member userInfo = (Member)authentication.getPrincipal();
        return CompletableFuture.supplyAsync(() -> placeService.getSearchHistory(userInfo), taskExecutor);
    }

    @GetMapping("/v1/search/hot10keywords")
    public CompletableFuture<HashMap<String, Object>> hotKeyWord() {
        return CompletableFuture.supplyAsync(() -> placeService.getHotKeyWord(), taskExecutor);
    }

}
