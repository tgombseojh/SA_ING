package com.yellowbus.project.place.search.repository;

import com.yellowbus.project.place.search.entity.HotKeyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.List;

public interface HotKeyWordRepository extends JpaRepository<HotKeyWord, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    HotKeyWord findByKeyWord(String keyWord);

    List<HotKeyWord> findTop10ByOrderBySearchCountDesc();


}
