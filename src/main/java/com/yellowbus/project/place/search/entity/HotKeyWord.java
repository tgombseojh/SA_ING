package com.yellowbus.project.place.search.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;

import javax.persistence.*;

@Table(name = "HotKeyWord")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class HotKeyWord {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(name = "keyWord")
    @NonNull
    private String keyWord;

    @Column(name = "searchCount")
    @NonNull
    private Long searchCount;

    @Column(name = "date")
    @NonNull
    private String date;

}
