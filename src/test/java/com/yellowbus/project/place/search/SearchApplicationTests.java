package com.yellowbus.project.place.search;

import com.yellowbus.project.place.search.controller.MemberController;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class SearchApplicationTests extends AbstractControllerTest {

    @Autowired
    MemberController memberController;

    @Override
    protected Object controller() {
        return memberController;
    }

    @Test @Order(1)
    public void signup() throws Exception {
        mockMvc.perform(
                post("/signup").param("email", "tiger@gmail.com").param("name", "seojh").param("password", "tiger")
        );
    }

    @Test @Order(2)
    public void login() throws Exception {
        mockMvc.perform(
                formLogin("/signin").user("tiger@gmail.com").password("tiger")
        );
    }



}
