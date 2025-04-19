package com.damian.xBank.auth;


import com.damian.xBank.auth.exception.JwtAuthenticationException;
import com.damian.xBank.common.utils.JWTUtil;
import com.damian.xBank.customer.CustomerDetails;
import com.damian.xBank.customer.CustomerDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final CustomerDetailsService customerDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public AuthenticationFilter(
            JWTUtil jwtUtil,
            CustomerDetailsService customerDetailsService, AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtUtil = jwtUtil;
        this.customerDetailsService = customerDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = authHeader.substring(7);

        try {
            // check here if token is expired
            jwtUtil.isTokenExpired(jwtToken);
        } catch (ExpiredJwtException e) {
            authenticationEntryPoint.commence(request, response, new JwtAuthenticationException("Token expired"));
            return;
        }

        final String email = jwtUtil.extractEmail(jwtToken);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            CustomerDetails customerDetails = customerDetailsService.loadCustomerByEmail(email);

            if (jwtUtil.isTokenValid(jwtToken, customerDetails)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        customerDetails,
                        null,
                        customerDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}