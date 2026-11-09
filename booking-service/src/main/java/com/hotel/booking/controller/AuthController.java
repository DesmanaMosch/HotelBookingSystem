package com.hotel.booking.controller;

import com.hotel.booking.dto.AuthRequest;
import com.hotel.booking.dto.AuthResponse;
import com.hotel.booking.dto.RegisterRequest;
import com.hotel.booking.entity.Role;
import com.hotel.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/auth")
    public AuthResponse authenticate(@Valid @RequestBody AuthRequest request) {
        return userService.authenticate(request);
    }

    @PostMapping
    public AuthResponse createUser(
            @Valid @RequestBody RegisterRequest request,
            @RequestParam(defaultValue = "USER") String role) {
        return userService.createUserByAdmin(request, Role.valueOf(role));
    }

    @DeleteMapping
    public void deleteUser(@RequestParam Long id) {
        userService.deleteUser(id);
    }
}