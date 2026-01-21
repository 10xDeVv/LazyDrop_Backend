package com.lazydrop.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

public class SupabaseJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken>{
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID supabaseUserId = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");

        UserPrincipal principal = new UserPrincipal(
                supabaseUserId,
                email
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
