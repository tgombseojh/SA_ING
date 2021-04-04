package com.yellowbus.project.place.search;

import com.yellowbus.project.place.search.entity.Member;
import com.yellowbus.project.place.search.repository.MemberRepository;
import com.yellowbus.project.place.search.repository.SearchResultRepository;
import com.yellowbus.project.place.search.service.PlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableAsync
class SearchApplicationTest {

    MockMvc mockMvc;

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
        member.setEmail("tiger@gmail.com");
        member.setPassword("tiger");
        member.setName("seojh");

        // redis cache 초기화
        searchResultRepository.deleteAll();
    }

    // 전체 테스트 시나리오 (성능측정- Springboot embedded Tomcat 의 기본 max-threads 는 200)
    // 사용자 2000명 생성과 회원가입, 로그인을 했다고 전제로 하고 진행.
    // 동시 접속자 2천명이 각각 10번씩 다양한 키워드로 조회하고 난 후
    // 자신의 검색기록을 조회
    String[] places = new String[]{"김밥", "국수", "홍게", "국밥", "짜장면", "짬뽕", "피자", "돈가스", "아구찜", "낙지"};
    ArrayList<Member> members = new ArrayList<>();

    // CompletableFuture call
    @Test @Order(1)
    public void test1() throws Exception {
        ArrayList<CompletableFuture> completableFutureArrayList = new ArrayList<>();
        Random random = new Random();
        int idx = 0;
        int size = 4;
        for (int i=1; i<=size; i++) {
            idx = random.nextInt(3);
            int finalIdx = idx;
            CompletableFuture completableFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return new ResponseEntity<>(placeService.v1Place(places[finalIdx], member), HttpStatus.OK);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            });
            completableFutureArrayList.add(completableFuture);
        }

        for (int i=1; i<=size; i++) {
            CompletableFuture.allOf(completableFutureArrayList.toArray(new CompletableFuture[completableFutureArrayList.size()])).join();
        }
    }

    @Test @Order(2)
    public void test2() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                get("/v1/search/hot10keywords")
                        .characterEncoding("UTF-8")
                        .with( user(member) )
        ).andReturn();

        mockMvc.perform(asyncDispatch(mvcResult)).andDo(print()).andExpect(status().isOk());
    }





}