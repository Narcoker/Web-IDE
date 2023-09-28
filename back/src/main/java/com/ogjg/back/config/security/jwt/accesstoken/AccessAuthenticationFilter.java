package com.ogjg.back.config.security.jwt.accesstoken;

import com.ogjg.back.config.security.exception.JwtAuthFailure;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class AccessAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final List<String> permitUrlList;

    private final String PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (isPermitted(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessToken = getAccessToken(request);
            AccessAuthenticationToken accessAuthenticationToken = new AccessAuthenticationToken(accessToken);

            Authentication authenticate = authenticationManager.authenticate(accessAuthenticationToken);

            SecurityContextHolder.getContext().setAuthentication(authenticate);
        } catch (Exception e) {
            throw new JwtAuthFailure("AccessToken 인증 실패");
        }
    }

    /*
     * 허용된 URL 인지 확인하기
     * */
    private boolean isPermitted(String requestUri) {
        for (String pattern : permitUrlList) {
            if (Pattern.matches(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }

    /*
     * 헤더에서 토큰값 가져오기
     * */
    private String getAccessToken(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");

        if (jwt != null && jwt.startsWith(PREFIX)) {
            return jwt.substring(PREFIX.length());
        }

        throw new JwtAuthFailure("유효하지 않은 JWT 입니다.");
    }
}