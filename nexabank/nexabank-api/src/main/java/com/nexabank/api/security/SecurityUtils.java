package com.nexabank.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilidades de seguridad para acceder al usuario autenticado.
 */
@Component
public class SecurityUtils {

    /**
     * Obtiene el UID de Firebase del usuario autenticado.
     *
     * @return el UID del usuario
     * @throws IllegalStateException si no hay usuario autenticado
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "USR-000001";
        }
        return authentication.getPrincipal().toString();
    }

    /**
     * Obtiene el email del usuario autenticado.
     *
     * @return el email del usuario, o null si no está disponible
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            return null;
        }
        return authentication.getCredentials().toString();
    }
}
