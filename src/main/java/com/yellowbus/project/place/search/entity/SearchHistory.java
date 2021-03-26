package com.yellowbus.project.place.search.entity;

import lombok.*;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.util.Date;

@Table(name = "SearchHistory")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SearchHistory {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "userName")
    private String userName;

    @Column(name = "keyword")
    @NonNull
    private String keyWord;

    @Column(name = "date")
    @NonNull
    private Date date;

}
