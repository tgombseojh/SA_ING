package com.yellowbus.project.place.search.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash("SearchResult")
public class SearchResult {

    @Id
    String keyWord;

    String result;

    @Builder
    public SearchResult(String keyWord, String result) {
        this.keyWord = keyWord;
        this.result = result;
    }

}
