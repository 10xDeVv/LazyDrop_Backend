package com.lazydrop.auth;
import com.lazydrop.modules.user.model.User;
import com.lazydrop.modules.user.service.GuestService;
import com.lazydrop.modules.user.service.UserService;
import com.lazydrop.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdentityResolver {
    private final UserService userService;
    private final GuestService guestService;

    public User resolve(
            UserPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        if (principal == null) {
            return guestService.resolveOrCreateGuest(request, response);
        }

        UUID supabaseUserId = principal.getSupabaseUserId();
        String email = principal.getEmail();

        String guestId = guestService.extractGuestIdFromCookie(request);

        var existingUser = userService.findBySupabaseUserId(supabaseUserId);
        if (existingUser.isPresent()) {
            if (guestId != null) guestService.clearGuestCookie(response);
            return existingUser.get();
        }

        if (guestId != null) {
            var guestOpt = userService.findByGuestId(guestId);
            if (guestOpt.isPresent()) {
                User g = guestOpt.get();
                if (g.isGuest() && g.getSupabaseUserId() == null) {
                    User upgraded = userService.upgradeGuestToAuthenticated(guestId, supabaseUserId, email);
                    guestService.clearGuestCookie(response);
                    return upgraded;
                }
            }
        }

        return userService.getOrCreateUser(supabaseUserId, email);
    }
}
