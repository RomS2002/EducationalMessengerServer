package ru.roms2002.messenger.server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.roms2002.messenger.server.service.CustomUserDetailsService;
import ru.roms2002.messenger.server.utils.JwtUtil;
import ru.roms2002.messenger.server.utils.StaticVariable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
public class JwtWebConfig extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, ServletException {
        String jwtToken = null;
        String username;
        Cookie cookie = WebUtils.getCookie(request, StaticVariable.SECURE_COOKIE);

        if (cookie != null) {
            jwtToken = cookie.getValue();
        }
        if (jwtToken != null) {
            username = jwtUtil.getUserNameFromJwtToken(jwtToken);
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ex) {
                //this is very important, since it guarantees the user is not authenticated at all
                filterChain.doFilter(request, response);
                SecurityContextHolder.clearContext();
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
