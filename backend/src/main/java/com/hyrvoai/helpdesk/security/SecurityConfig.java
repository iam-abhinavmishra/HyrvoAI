package com.hyrvoai.helpdesk.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

import org.springframework.security.oauth2.core.endpoint.DefaultMapOAuth2AccessTokenResponseConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import org.springframework.web.client.RestClient;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final OAuth2AuthenticationSuccessHandler
            oAuth2AuthenticationSuccessHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            OAuth2AuthenticationSuccessHandler
                    oAuth2AuthenticationSuccessHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2AuthenticationSuccessHandler =
                oAuth2AuthenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2AuthorizationRequestResolver
                    authorizationRequestResolver,
            RestClientAuthorizationCodeTokenResponseClient
                    linkedinTokenResponseClient
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                /*
                 * OAuth2 login needs a temporary HTTP session
                 * during the authorization-code flow.
                 *
                 * After OAuth login succeeds, HyrvoAI still uses
                 * JWT authentication for API requests.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/health",
                                "/api/auth/**",
                                "/api/public/chat",
                                "/oauth2/**",
                                "/login/**"
                        ).permitAll()

                        .requestMatchers("/api/admin/**")
                        .hasAuthority("ROLE_ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .oauth2Login(oauth2 ->
                        oauth2

                                /*
                                 * LinkedIn requires a provider-specific
                                 * authorization request resolver because
                                 * LinkedIn's ID token in this flow does not
                                 * provide the nonce claim expected by
                                 * Spring Security.
                                 *
                                 * Google remains on the normal OIDC flow.
                                 */
                                .authorizationEndpoint(endpoint ->
                                        endpoint.authorizationRequestResolver(
                                                authorizationRequestResolver
                                        )
                                )

                                .successHandler(
                                        oAuth2AuthenticationSuccessHandler
                                )

                                /*
                                 * LinkedIn's token response does not include
                                 * token_type in this setup.
                                 *
                                 * Add "Bearer" while preserving id_token
                                 * and every other response parameter.
                                 */
                                .tokenEndpoint(tokenEndpoint ->
                                        tokenEndpoint.accessTokenResponseClient(
                                                linkedinTokenResponseClient
                                        )
                                )
                )

                .addFilterAfter(
                        jwtAuthenticationFilter,
                        SecurityContextHolderFilter.class
                );

        return http.build();
    }

    /*
     * LinkedIn-only OAuth2 authorization request resolver.
     *
     * Google continues using Spring Security's normal resolver,
     * including the normal OIDC nonce.
     *
     * LinkedIn uses a resolver whose nonce is removed.
     */
    @Bean
    public OAuth2AuthorizationRequestResolver
    authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository
    ) {

        DefaultOAuth2AuthorizationRequestResolver
                defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository
                );

        DefaultOAuth2AuthorizationRequestResolver
                linkedInResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository
                );

        /*
         * Remove nonce only for the LinkedIn resolver.
         *
         * Spring Security normally creates:
         *
         *   attributes["nonce"]
         *
         * and
         *
         *   additionalParameters["nonce"]
         *
         * when "openid" is in the scope.
         *
         * We remove both so that:
         *
         *   1. LinkedIn does not receive nonce
         *   2. Spring does not later try to validate a nonce
         *      that LinkedIn does not return.
         */
        linkedInResolver.setAuthorizationRequestCustomizer(
                builder -> builder

                        .additionalParameters(
                                parameters ->
                                        parameters.remove(
                                                OidcParameterNames.NONCE
                                        )
                        )

                        .attributes(
                                attributes ->
                                        attributes.remove(
                                                OidcParameterNames.NONCE
                                        )
                        )
        );

        return new OAuth2AuthorizationRequestResolver() {

            @Override
            public OAuth2AuthorizationRequest resolve(
                    HttpServletRequest request
            ) {

                if (isLinkedInRequest(request)) {
                    return linkedInResolver.resolve(request);
                }

                return defaultResolver.resolve(request);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(
                    HttpServletRequest request,
                    String clientRegistrationId
            ) {

                if ("linkedin".equals(clientRegistrationId)) {
                    return linkedInResolver.resolve(
                            request,
                            clientRegistrationId
                    );
                }

                return defaultResolver.resolve(
                        request,
                        clientRegistrationId
                );
            }
        };
    }

    /*
     * Detect the LinkedIn authorization endpoint:
     *
     * /oauth2/authorization/linkedin
     */
    private boolean isLinkedInRequest(
            HttpServletRequest request
    ) {

        String requestUri = request.getRequestURI();

        return requestUri != null
                && requestUri.endsWith(
                "/oauth2/authorization/linkedin"
        );
    }

    /*
     * LinkedIn token response client.
     *
     * LinkedIn's token response in our current setup does not provide
     * token_type, while Spring Security requires it.
     *
     * We add:
     *
     *     token_type = Bearer
     *
     * without throwing away id_token or any other response fields.
     */
    @Bean
    public RestClientAuthorizationCodeTokenResponseClient
    linkedinTokenResponseClient() {

        RestClientAuthorizationCodeTokenResponseClient client =
                new RestClientAuthorizationCodeTokenResponseClient();

        OAuth2AccessTokenResponseHttpMessageConverter converter =
                new OAuth2AccessTokenResponseHttpMessageConverter();

        converter.setAccessTokenResponseConverter(parameters -> {

            java.util.Map<String, Object> updatedParameters =
                    new java.util.HashMap<>(parameters);

            updatedParameters.putIfAbsent(
                    "token_type",
                    "Bearer"
            );

            return new DefaultMapOAuth2AccessTokenResponseConverter()
                    .convert(updatedParameters);
        });

        client.setRestClient(
                RestClient.builder()
                        .configureMessageConverters(converters -> {
                            converters.addCustomConverter(converter);
                        })
                        .build()
        );

        return client;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "http://localhost:5174"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
    jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter
    ) {

        FilterRegistrationBean<JwtAuthenticationFilter>
                registration =
                new FilterRegistrationBean<>(filter);

        /*
         * SecurityFilterChain already manages this filter.
         * Prevent Spring Boot from registering it a second time.
         */
        registration.setEnabled(false);

        return registration;
    }
}