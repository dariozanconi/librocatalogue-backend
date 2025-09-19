package com.example.BookCatalogueApplication.service;

import com.example.BookCatalogueApplication.model.RegisterRequest;
import com.example.BookCatalogueApplication.model.Users;
import com.example.BookCatalogueApplication.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Value("${app.registration.secret}")
    private String registrationSecret;

    public boolean validateCode(String code) {
        return registrationSecret.equals(code);
    }

    public String verify(Users user)  {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            return jwtService.generateToken(user.getUsername());
    }

    public Users register(RegisterRequest request) {
        Users userToSave = new Users();
        userToSave.setUsername(request.getUsername());
        userToSave.setPassword(encoder.encode(request.getPassword()));
        if (userRepo.findByUsername(userToSave.getUsername())!=null)
            throw new BadCredentialsException("User already in use");
        userRepo.save(userToSave);

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        return user;
    }

    public String deleteAccount(String username) {
        Users user = userRepo.findByUsername(username);
        if (user != null) {
            userRepo.delete(user);
            return "Account deleted";
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }
}
