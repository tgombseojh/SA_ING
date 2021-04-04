package com.yellowbus.project.place.search.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Table(name = "HotKeyWord")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class HotKeyWord {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "keyWord", unique = true)
    @NonNull
    private String keyWord;

    @Column(name = "searchCount")
    @NonNull
    private Long searchCount;

    @Column(name = "searchDate")
    @NonNull
    private String date;

    public HashMap<String, Object> changeFromat(List<HotKeyWord> hotKeyWordList) {
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
}
