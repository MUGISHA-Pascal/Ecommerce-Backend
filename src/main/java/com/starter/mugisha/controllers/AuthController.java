package com.starter.mugisha.controllers;

import com.starter.mugisha.dtos.SIgninDto;
import com.starter.mugisha.dtos.SignupDto;
import com.starter.mugisha.exceptions.ApiRequestException;
import com.starter.mugisha.models.Role;
import com.starter.mugisha.models.User;
import com.starter.mugisha.payload.JwtAuthResponse;
import com.starter.mugisha.repository.RoleRepository;
import com.starter.mugisha.repository.UserRepository;
import com.starter.mugisha.security.JwtTokenProvider;
import com.starter.mugisha.services.UserService;
import com.starter.mugisha.util.Response;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserRepository userRepository;
    @PostMapping(path = "/signin")
    @Operation(summary = "signin into your account")
    public ResponseEntity<JwtAuthResponse> signin(@Valid @RequestBody SIgninDto signInRequest){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getEmail(),signInRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt=null;
        try{
            jwt=jwtTokenProvider.generateToken(authentication);
        }catch(Exception e){
            System.out.println("error generating jwt");
            e.printStackTrace();
        }
        return ResponseEntity.ok(new JwtAuthResponse(jwt));
    }
    @PostMapping(path = "/signup")
    @Operation(summary="create new user")
    public ResponseEntity<Response> signup(@RequestBody @Valid SignupDto signupRequest){
        User user = new User(signupRequest.getEmail(),signupRequest.getFirstName(),signupRequest.getLastName(),signupRequest.getMobile(),signupRequest.getGender(),signupRequest.getPassword());
        boolean userExists = userRepository.findByEmail(user.getEmail()).isPresent();
        if(userRepository.existsByMobile(user.getMobile())){
            throw new ApiRequestException("phone number already  in use");
        }
        if(user.getEmail() != null && userExists){
            throw new ApiRequestException("Email already in use");
        }
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        Role userRole = roleRepository.findByName(signupRequest.getRole()).orElseThrow(()-> new ApiRequestException("User Role not set"));
        user.setRoles(Collections.singleton(userRole));
        userRepository.save(user);
    return new ResponseEntity<>(new Response("Registered Successfully", ZonedDateTime.now(),true), HttpStatus.CREATED);
     }
}
