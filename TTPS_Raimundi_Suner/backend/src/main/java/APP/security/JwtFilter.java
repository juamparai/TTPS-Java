package APP.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    // Paths that do not require authentication
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/api/usuarios/login",
            "/api/usuarios/registro",
            "/api/publicaciones",
            "/api/mascotas",
            "/swagger-ui.html",
            "/swagger-ui/",
            "/v3/api-docs",
            "/v3/api-docs/"
    ));

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String method = req.getMethod();

        // Always allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            res.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(request, response);
            return;
        }

        // Allow public paths (solo GET para publicaciones y mascotas)
        if (isPublicPath(path, method)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            setCorsHeaders(req, res);
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Authorization header missing or invalid\"}");
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                setCorsHeaders(req, res);
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Token inválido\"}");
                return;
            }

            // Token valid: set attribute with subject (user id)
            String subject = jwtUtil.getSubject(token);
            req.setAttribute("authenticatedUserId", Long.parseLong(subject));

            chain.doFilter(request, response);
        } catch (Exception e) {
            setCorsHeaders(req, res);
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Token inválido: " + e.getMessage() + "\"}");
        }
    }

    private void setCorsHeaders(HttpServletRequest req, HttpServletResponse res) {
        String origin = req.getHeader("Origin");
        if (origin == null || origin.isEmpty()) {
            origin = "http://localhost:4200";
        }
        res.setHeader("Access-Control-Allow-Origin", origin);
        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept");
        res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH");
    }

    private boolean isPublicPath(String path, String method) {
        if (path == null) return false;

        // Allow public access to uploaded images
        if (path.startsWith("/uploads/")) {
            return true;
        }

        // Allow prefix checks for swagger and api-docs
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) return true;

        // Allow login and registro
        if (path.equals("/api/usuarios/login") || path.equals("/api/usuarios/registro")) return true;

        // Allow GET only for publicaciones and mascotas (read-only public access)
        if ("GET".equalsIgnoreCase(method)) {
            if (path.startsWith("/api/publicaciones") || path.startsWith("/api/mascotas")) {
                return true;
            }
        }

        return PUBLIC_PATHS.contains(path);
    }
}
