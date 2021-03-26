package com.yellowbus.project.place.search.repository;

import com.yellowbus.project.place.search.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findTop20ByUserIdOrderByDateDesc(Long userId);

}
