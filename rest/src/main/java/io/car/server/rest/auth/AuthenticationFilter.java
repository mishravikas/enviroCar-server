/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package io.car.server.rest.auth;

import java.security.Principal;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import io.car.server.core.User;
import io.car.server.core.UserService;
import io.car.server.core.exception.UserNotFoundException;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class AuthenticationFilter implements ContainerRequestFilter {
    
    private final UserService service;

    @Inject
    public AuthenticationFilter(UserService service) {
        this.service = service;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        String username = request.getRequestHeaders().getFirst(AuthConstants.USERNAME_HEADER);
        String token = request.getRequestHeaders().getFirst(AuthConstants.TOKEN_HEADER);

        if (username != null) {
            if (username.isEmpty() || token == null || token.isEmpty()) {
                throw new WebApplicationException(Status.BAD_REQUEST);
            }
            try {
                User user = service.getUser(username);
                if (token.equals(user.getToken())) {
                    request.setSecurityContext(new CustomSecurityContext(username, request.isSecure(), user.isAdmin()));
                } else {
                    throw new WebApplicationException(Status.FORBIDDEN);
                }
            } catch (UserNotFoundException ex) {
                throw new WebApplicationException(ex, Status.FORBIDDEN);
            }
        }
        return request;
    }

    private class CustomSecurityContext implements SecurityContext {
        private final Principal principal;
        private final boolean secure;
        private final boolean isAdmin;

        CustomSecurityContext(final String name, boolean secure, boolean isAdmin) {
            this.secure = secure;
            this.isAdmin = isAdmin;
            this.principal = new CustomPrincipal(name);
        }

        @Override
        public Principal getUserPrincipal() {
            return this.principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            if (role.equals(AuthConstants.ADMIN_ROLE)) {
                return isAdmin;
            } else {
                return role.equals(AuthConstants.USER_ROLE);
            }
        }

        @Override
        public boolean isSecure() {
            return secure;
        }

        @Override
        public String getAuthenticationScheme() {
            return AuthConstants.AUTH_SCHEME;
        }
    }

    private class CustomPrincipal implements Principal {
        private final String name;

        CustomPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
