package com.yellowbus.project.place.search.repository;

import com.yellowbus.project.place.search.entity.HotKeyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotKeyWordRepository extends JpaRepository<HotKeyWord, Long> {

    Optional<HotKeyWord> findOneByKeyWord(@Param("searchWord") String keyWord);

    List<HotKeyWord> findTop10ByOrderBySearchCountDesc();

}
