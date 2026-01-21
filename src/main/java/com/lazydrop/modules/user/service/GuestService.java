package com.lazydrop.modules.user.service;

import com.lazydrop.config.AppConfig;
import com.lazydrop.modules.user.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestService {
    private static final String GUEST_COOKIE_NAME = "ld_guest_id";

    private final UserService userService;
    private final AppConfig appConfig;

    public User resolveOrCreateGuest(HttpServletRequest request,
                                     HttpServletResponse response) {
        String guestId = extractGuestIdFromCookie(request);

        if (guestId != null) {
            return userService.findByGuestId(guestId)
                    .orElseGet(() -> createAndSetGuest(response));
        }

        return createAndSetGuest(response);
    }

    public String extractGuestIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (GUEST_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void clearGuestCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(GUEST_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    private User createAndSetGuest(HttpServletResponse response) {
        String guestId =  UUID.randomUUID().toString();
        User guest = userService.createGuestUser(guestId);

        Cookie cookie = new Cookie(GUEST_COOKIE_NAME, guestId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30);
        cookie.setSecure(cookie.getSecure());

        response.addCookie(cookie);
        return guest;
    }
}
