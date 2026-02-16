package com.lazydrop.modules.user.controller;

import com.lazydrop.auth.IdentityResolver;
import com.lazydrop.security.UserPrincipal;
import com.lazydrop.modules.user.dto.UserResponse;
import com.lazydrop.modules.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final IdentityResolver identityResolver;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal, HttpServletRequest request, HttpServletResponse response, Principal principal) {
        User user = identityResolver.resolve(userPrincipal, request, response);
        return ResponseEntity.ok().body(new UserResponse(user.getId(), user.getEmail()));
    }
}
