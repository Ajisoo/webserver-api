package xyz.ajisoo.backend.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.ajisoo.backend.service.JwtService;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${cookie.verification}")
    boolean verify;

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!verify) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    new Object(),
                    null,
                    Collections.emptyList() // No specific roles/authorities needed for now
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Try to extract the JWT from the "token" cookie
        String jwt = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // 1.1. Try again with auth header if not in cookies
        if (jwt == null) {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        // If no token is found, continue to the next filter
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Validate the token
        try {
            final String subject = jwtService.extractSubject(jwt);

            // If token is valid, set the authentication in the security context
            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // The "principal" is the subject, credentials are null, authorities are empty for this simple case
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        subject,
                        null,
                        Collections.emptyList() // No specific roles/authorities needed for now
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException e) {
            // Token is invalid (expired, bad signature, etc.).
            // We just let the request continue without authentication.
            // The security annotations downstream will then block access.
            logger.warn("Invalid JWT Token: " + e.getMessage());
        }

        // 3. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
