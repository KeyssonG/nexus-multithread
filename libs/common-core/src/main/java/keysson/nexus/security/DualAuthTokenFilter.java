package keysson.nexus.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component("dualAuthTokenFilter")
public class DualAuthTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DualAuthTokenFilter.class);

    @Autowired
    @Qualifier("validacaoADJwtUtil")
    private JwtUtil legacyJwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // Try legacy JWT first (existing system)
        if (legacyJwtUtil.isTokenValid(token)) {
            Integer userId = legacyJwtUtil.extractUserId(token);
            Integer companyId = legacyJwtUtil.extractCompanyId(token);

            List<SimpleGrantedAuthority> authorities = Collections.emptyList();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.setAttribute("CleanJwt", token);
            request.setAttribute("TokenSource", "legacy");

            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
