package pl.edu.pg.eti.kask.idp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Identity provider (OpenID Connect) for the aquarium application. Users log in with the login and password
 * stored in the aquarium database and get a signed JWT token, which the aquarium application checks.
 */
@SpringBootApplication
public class IdentityProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityProviderApplication.class, args);
    }
}
