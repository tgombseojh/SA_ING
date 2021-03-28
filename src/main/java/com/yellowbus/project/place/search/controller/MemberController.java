package com.yellowbus.project.place.search.controller;

import com.google.gson.Gson;
import com.yellowbus.project.place.search.entity.Member;
import com.yellowbus.project.place.search.exception.SignupException;
import com.yellowbus.project.place.search.repository.MemberRepository;
import com.yellowbus.project.place.search.service.PlaceService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@AllArgsConstructor
@RestController
public class MemberController {

    PlaceService placeService;
    MemberRepository memberRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    Gson gson;

    private static final Logger logger = LogManager.getLogger(MemberController.class);

    @PostMapping("/signup")
    public ResponseEntity<HashMap<String, Object>> signup(@RequestParam HashMap<String, Object> hashMap) {
        Member member = gson.fromJson(gson.toJsonTree(hashMap), Member.class);
        member.setPassword(bCryptPasswordEncoder.encode(member.getPassword()));

        logger.debug("member : "+member);

        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            throw new SignupException(e.getMessage());
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "You have successfully signed up");

        return ResponseEntity.ok(map);
    }

    @PostMapping("/login/success")
    public ResponseEntity<HashMap<String, Object>> loginSuccess() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "Login succeed");

        return ResponseEntity.ok(map);
    }

    @PostMapping("/login/failure")
    public ResponseEntity<HashMap<String, Object>> fail() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "Login failure");

        return ResponseEntity.ok(map);
    }

}
