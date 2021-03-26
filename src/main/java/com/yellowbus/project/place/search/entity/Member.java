package com.yellowbus.project.place.search.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="Member")
@Getter @Setter
@NoArgsConstructor
@ToString
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name="email" , unique=true)
    private String email;
    private String name;
    private String password;

}
