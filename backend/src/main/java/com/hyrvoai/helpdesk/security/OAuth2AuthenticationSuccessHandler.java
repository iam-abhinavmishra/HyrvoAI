package com.hyrvoai.helpdesk.security;

import com.hyrvoai.helpdesk.entity.User;
import com.hyrvoai.helpdesk.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendRedirect(
                    frontendUrl + "/login?error=invalid_oauth_authentication"
            );
            return;
        }

        String registrationId =
                oauthToken.getAuthorizedClientRegistrationId();

        OAuth2User oauthUser =
                (OAuth2User) authentication.getPrincipal();

        String email = null;
        String name = null;
        String providerUserId = null;

        /*
         * OpenID Connect providers such as Google and LinkedIn
         * normally arrive as OidcUser.
         */
        if (oauthUser instanceof OidcUser oidcUser) {

            email = oidcUser.getEmail();
            name = oidcUser.getFullName();
            providerUserId = oidcUser.getSubject();

        } else {

            /*
             * Fallback for providers that return OAuth2User
             * instead of OidcUser.
             */
            email = getAttribute(
                    oauthUser,
                    "email"
            );

            name = getAttribute(
                    oauthUser,
                    "name"
            );

            providerUserId = getAttribute(
                    oauthUser,
                    "sub"
            );

            if (providerUserId == null) {
                providerUserId =
                        oauthUser.getName();
            }
        }

        if (email == null || email.isBlank()) {

            response.sendRedirect(
                    frontendUrl + "/login?error=no_email"
            );

            return;
        }

        email = email.trim().toLowerCase();

        Optional<User> existingUser =
                userRepository.findByEmail(email);

        User user;

        if (existingUser.isPresent()) {

            user = existingUser.get();

        } else {

            user = new User();

            user.setEmail(email);

            user.setName(
                    name != null && !name.isBlank()
                            ? name.trim()
                            : email
            );

            /*
             * A general OAuth user is not automatically
             * associated with a company.
             *
             * Company context will be established separately.
             */
            user.setCompany(null);

            user.setRole("USER");

            /*
             * OAuth-only account:
             * no local password.
             */
            user.setPassword(null);

            user.setAuthProvider(
                    registrationId.toUpperCase()
            );

            user.setProviderUserId(
                    providerUserId
            );

            user.setDepartment("GENERAL");

            user =
                    userRepository.save(user);

        }

        /*
         * Update provider information for an existing account.
         */
        user.setAuthProvider(
                registrationId.toUpperCase()
        );

        user.setProviderUserId(
                providerUserId
        );

        /*
         * If the provider returned a better name,
         * keep the existing name if one already exists.
         */
        if ((user.getName() == null
                || user.getName().isBlank())
                && name != null
                && !name.isBlank()) {

            user.setName(name.trim());
        }

        userRepository.save(user);

        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getName(),
                        user.getRole()
                );

        String redirectUrl =
                frontendUrl
                        + "/oauth/callback#token="
                        + token;

        response.sendRedirect(redirectUrl);
    }

    private String getAttribute(
            OAuth2User user,
            String attributeName
    ) {

        Object value =
                user.getAttributes()
                        .get(attributeName);

        if (value == null) {
            return null;
        }

        String result =
                value.toString().trim();

        return result.isBlank()
                ? null
                : result;
    }
}