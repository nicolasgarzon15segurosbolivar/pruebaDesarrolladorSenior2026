package com.prueba.apipolizas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Valida el header "x-api-key" en cada peticion.
 *
 * - Si el header falta o no coincide con la clave configurada -> 401 Unauthorized.
 * - La consola H2 queda excluida para facilitar el uso en desarrollo.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "x-api-key";

    private final String apiKey;

    public ApiKeyFilter(@Value("${app.security.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // No exigir api-key para la consola H2 (solo util en desarrollo).
        return path.startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String provided = request.getHeader(HEADER);

        if (provided == null || !provided.equals(apiKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":401,\"error\":\"No autorizado\","
                    + "\"message\":\"Header 'x-api-key' ausente o invalido\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
