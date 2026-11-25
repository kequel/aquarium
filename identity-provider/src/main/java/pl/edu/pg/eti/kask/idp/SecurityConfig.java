package pl.edu.pg.eti.kask.idp;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import pl.edu.pg.eti.kask.idp.password.PasswordGrantAuthenticationConverter;
import pl.edu.pg.eti.kask.idp.password.PasswordGrantAuthenticationProvider;
import pl.edu.pg.eti.kask.idp.password.PasswordGrantAuthenticationToken;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

/**
 * Configuration of the identity provider:
 * <ul>
 *     <li>users come from the {@code users} table of the aquarium database (login, BCrypt password hash, role),</li>
 *     <li>one client application: {@code aquarium} with secret {@code secret},</li>
 *     <li>tokens are JWTs signed with an RSA key; the role of the user is put into the {@code groups} claim,</li>
 *     <li>standard OpenID Connect endpoints: /.well-known/openid-configuration, /oauth2/authorize, /oauth2/token,
 *     /oauth2/jwks, /userinfo and a login page,</li>
 *     <li>additionally the "password" grant on /oauth2/token, so a token can be obtained with a single HTTP request
 *     (used in request.http).</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    public static final String ISSUER = "http://localhost:9000";

    /** Security of the provider's own endpoints (tokens, keys, OpenID Connect). */
    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerFilterChain(HttpSecurity http, UserDetailsService users,
                                                       PasswordEncoder passwordEncoder,
                                                       OAuth2AuthorizationService authorizationService,
                                                       DelegatingOAuth2TokenGenerator tokenGenerator) throws Exception {
        DaoAuthenticationProvider userAuthentication = new DaoAuthenticationProvider(users);
        userAuthentication.setPasswordEncoder(passwordEncoder);

        OAuth2AuthorizationServerConfigurer authorizationServer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        http.securityMatcher(authorizationServer.getEndpointsMatcher())
                .with(authorizationServer, server -> server
                        .oidc(Customizer.withDefaults())
                        .tokenEndpoint(token -> token
                                .accessTokenRequestConverter(new PasswordGrantAuthenticationConverter())
                                .authenticationProvider(new PasswordGrantAuthenticationProvider(
                                        userAuthentication, authorizationService, tokenGenerator))))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                // A browser without a session is sent to the login page.
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                // /userinfo accepts the provider's own access tokens.
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /** Everything else (the login page): a form login. */
    @Bean
    @Order(2)
    SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Users from the aquarium database. Every user has one role (ADMIN or USER), it becomes the user's authority.
     */
    @Bean
    UserDetailsService users(DataSource dataSource) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        users.setUsersByUsernameQuery("select login, password, true from users where login = ?");
        users.setAuthoritiesByUsernameQuery("select login, role from users where login = ?");
        return users;
    }

    /** The aquarium application stores BCrypt hashes, so they are checked with BCrypt. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** The only client application allowed to ask for tokens. */
    @Bean
    RegisteredClientRepository clients(PasswordEncoder passwordEncoder) {
        RegisteredClient aquarium = RegisteredClient.withId("aquarium")
                .clientId("aquarium")
                .clientSecret(passwordEncoder.encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(PasswordGrantAuthenticationToken.PASSWORD)
                .redirectUri("http://localhost:8080/lab7/callback")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofHours(1)).build())
                .build();
        return new InMemoryRegisteredClientRepository(aquarium);
    }

    /** Puts the user's roles into every token as the {@code groups} claim. */
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> groupsClaim() {
        return context -> context.getClaims().claim("groups",
                AuthorityUtils.authorityListToSet(context.getPrincipal().getAuthorities()));
    }

    /** Creates tokens: JWTs (access and ID tokens) and refresh tokens. */
    @Bean
    DelegatingOAuth2TokenGenerator tokenGenerator(JWKSource<SecurityContext> keys,
                                                  OAuth2TokenCustomizer<JwtEncodingContext> groupsClaim) {
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(keys));
        jwtGenerator.setJwtCustomizer(groupsClaim);
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, new OAuth2RefreshTokenGenerator());
    }

    /** Issued tokens (kept in memory). */
    @Bean
    OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    /**
     * RSA key pair signing the tokens, generated at startup. The public key is published at /oauth2/jwks,
     * the aquarium application downloads it from there to check signatures.
     */
    @Bean
    JWKSource<SecurityContext> keys() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        RSAKey key = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(UUID.randomUUID().toString())
                .build();
        return new ImmutableJWKSet<>(new JWKSet(key));
    }

    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> keys) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(keys);
    }

    /** Fixed issuer, the same as {@code jwtIssuer} in web.xml of the aquarium application. */
    @Bean
    AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().issuer(ISSUER).build();
    }
}
