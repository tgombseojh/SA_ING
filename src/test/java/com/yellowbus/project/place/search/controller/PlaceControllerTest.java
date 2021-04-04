package com.yellowbus.project.place.search.controller;

import com.yellowbus.project.place.search.entity.Member;
import com.yellowbus.project.place.search.repository.MemberRepository;
import com.yellowbus.project.place.search.repository.SearchResultRepository;
import com.yellowbus.project.place.search.service.PlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableAsync
class PlaceControllerTest {

    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

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
                //.alwaysDo(print())
                .build();

        member = new Member();
        member.setEmail("aaa@gmail.com");
        member.setPassword("tiger");
        member.setName("seojh");

        // redis cache 초기화
        // searchResultRepository.deleteAll();
    }

    // PlaceComponent 38번 라인 활성화 후 테스트
    @Test @Order(1)
    public void test1() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                get("/v1/place/{searchword}", "판교김밥")
                        .characterEncoding("UTF-8")
                        .with( user(member) )
        ).andReturn();

        mockMvc.perform(asyncDispatch(mvcResult)).andDo(print()).andExpect(status().is(200));
    }

    @Test @Order(2)
    public void test2() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                get("/v1/search/history")
                        .characterEncoding("UTF-8")
                        .with( user(member) )
        ).andReturn();

        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());
    }

    @Test @Order(3)
    public void test3() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                get("/v1/search/hot10keywords")
                        .characterEncoding("UTF-8")
                        .with( user(member) )
        ).andReturn();

        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());
    }

}