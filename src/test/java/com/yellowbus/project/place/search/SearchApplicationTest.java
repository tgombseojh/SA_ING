package com.yellowbus.project.place.search;

import com.yellowbus.project.place.search.entity.Member;
import com.yellowbus.project.place.search.repository.MemberRepository;
import com.yellowbus.project.place.search.repository.SearchResultRepository;
import com.yellowbus.project.place.search.service.MemberService;
import com.yellowbus.project.place.search.service.PlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.concurrent.Callable;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableAsync
class SearchApplicationTest {

    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    PlaceService placeService;

    @Autowired
    SearchResultRepository searchResultRepository;

    @Autowired
    WebApplicationContext ctx;

    Member member;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).apply(springSecurity())
                        .addFilters(new CharacterEncodingFilter("UTF-8", true))
                        .alwaysDo(print())
                        .build();

        member = new Member();
        member.setEmail("tiger@gmail.com");
        member.setPassword("tiger");
        member.setName("seojh");

        // redis cache delete
        searchResultRepository.deleteAll();
    }

    @Test @Order(1)
    public void test1() throws Exception {
        mockMvc.perform(
                        post("/signup")
                                .param("email", member.getEmail())
                                .param("name", member.getName())
                                .param("password", member.getPassword())
                ).andExpect(status().isOk());

        // 이미 존재하는 아이디 입력
        mockMvc.perform(
                        post("/signup")
                                .param("email", member.getEmail())
                                .param("name", member.getName())
                                .param("password", member.getPassword())
                ).andExpect(status().is(500));
    }

    /*@Test  @Order(2)
    public void test2() throws Exception {
        // TODO 키워드 20개를 랜덤하게 30번 검색해볼까
        mockMvc.perform(
                get("/v1/place/{searchword}", "판교떡볶이")
                        .characterEncoding("UTF-8")
                        .with( user(member) )
        ).andExpect(status().isOk());

        // Async 에러테스트는 PlaceService 의 API URL 을 살짝 변경해서 실행해보면 간단하게 확인 가능하다.
    }*/


    @Test @Order(3)
    public void test9() throws Exception {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                mockMvc.perform(
                        get("/v2/place/{searchword}", "판교떡볶이")
                                .characterEncoding("UTF-8")
                                .with( user(member) )
                ).andExpect(status().isOk());
                return null;
            }
        };

        callable.call();
    }

    @Test @Order(4)
    public void test3() throws Exception {
        mockMvc.perform(
                get("/v1/search/history")
                        .characterEncoding("UTF-8")
                        .with( user(member) )
        ).andExpect(status().isOk());
    }

    @Test @Order(5)
    public void test4() throws Exception {
        mockMvc.perform(
                get("/v1/search/hot10keywords")
                        .characterEncoding("UTF-8")
                        .with( user(member) )
        ).andExpect(status().isOk());
    }



}