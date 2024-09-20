package com.project.auth_microservice.security;

import com.project.auth_microservice.repository.AccountAccessRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final AccountAccessRepository accountAccessRepository;

    @Autowired
    public SecurityFilter(TokenService tokenService, AccountAccessRepository accountAccessRepository) {
        this.tokenService = tokenService;
        this.accountAccessRepository = accountAccessRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        var token = this.recoverToken(request);
        if(token != null)
        {
            var login = tokenService.validateToken(token);
            UserDetails acessoConta = accountAccessRepository.findByLogin(login);
            var authentication = new UsernamePasswordAuthenticationToken(acessoConta, null, acessoConta.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request)
    {
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}
