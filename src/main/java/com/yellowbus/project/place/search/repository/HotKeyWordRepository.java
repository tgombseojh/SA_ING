package com.yellowbus.project.place.search.repository;

import com.yellowbus.project.place.search.entity.HotKeyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface HotKeyWordRepository extends JpaRepository<HotKeyWord, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<HotKeyWord> findOneByKeyWord(@Param("searchWord") String keyWord);

    /*@Lock(LockModeType.PESSIMISTIC_WRITE)
    public HotKeyWord save(HotKeyWord hotKeyWord);*/

    List<HotKeyWord> findTop10ByOrderBySearchCountDesc();

}
