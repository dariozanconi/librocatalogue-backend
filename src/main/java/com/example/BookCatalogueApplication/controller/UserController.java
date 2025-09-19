package com.example.BookCatalogueApplication.controller;

import com.example.BookCatalogueApplication.model.RegisterRequest;
import com.example.BookCatalogueApplication.model.Users;
import com.example.BookCatalogueApplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Users user) {
        try {
            return new ResponseEntity<>(userService.verify(user), HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            if (!userService.validateCode(request.getCode())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid registration code");
            }
            return ResponseEntity.ok(userService.register(request));
        } catch (BadCredentialsException e){
            return new ResponseEntity<String>("User already in use", HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("/deleteaccount")
    public ResponseEntity<String> deleteAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return new ResponseEntity<>(userService.deleteAccount(username), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
