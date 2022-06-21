package com.joomak.backend.controller.member;


import com.joomak.backend.domain.member.dto.MemberLoginDto;
import com.joomak.backend.domain.member.dto.TokenDto;
import com.joomak.backend.domain.member.entity.Member;
import com.joomak.backend.service.MemberService;
import com.joomak.backend.token.JwtAuthenticationFilterV2;
import com.joomak.backend.token.JwtTokenProviderV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Slf4j
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProviderV2 tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    // 회원번호로 한명의 회원 조회
    @GetMapping(value = "/{memberId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Member> getMember(@PathVariable("memberId") Long memberId) {
        Member member = memberService.findById(memberId);
        return ResponseEntity.ok(member);
    }
    // 모든 회원 조회
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Member>> getAllMembers() {
        List<Member> member = memberService.findAll();
        return ResponseEntity.ok(member);
    }

    @PostMapping
    public ResponseEntity<Member> save(@RequestBody Member member) {
        return ResponseEntity.ok(memberService.save(member));
    }

    @PostMapping(value = "/sign-up")
    public ResponseEntity<UserDetails> signUp() {

        return null;
    }

    // 특정 회원 밴 처리(악성유저처리)
    @PutMapping(value = "/ban/{memberId}")
    public ResponseEntity<Member> ban(@PathVariable Long memberId) {
        return ResponseEntity.ok(memberService.ban(memberId));
    }
    // 로그인
    @PostMapping(value="/login")
    public ResponseEntity<UserDetails> login(@RequestBody MemberLoginDto memberLoginDto){
        log.info("MemberController - login API is called");

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(memberLoginDto.getEmail(), memberLoginDto.getPassword());

        // authenticate Method가 실행되면 UserDetailsService의 loadUserByUsername통해 Database에 있는 Member 정보를 받아와서 Setting 한다.
        // Default AuthenticationProvider에서는 authenticate Method 실행 시 사용자 id / pwd를 비교한다.
        Authentication authentication = authenticationManagerBuilder
            .getObject()
            .authenticate(usernamePasswordAuthenticationToken);

        String jwt = tokenProvider.createToken(authentication);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtAuthenticationFilterV2.AUTHORIZATION_HEADER, "Bearer " + jwt);

        memberLoginDto.setTokenDto(
            TokenDto.builder()
                .token(jwt)
                .build()
        );

        return ResponseEntity.ok(memberLoginDto);
    }
}
