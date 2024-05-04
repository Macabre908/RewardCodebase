package config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.net.http.HttpRequest;

@Configuration
@EnableMethodSecurity
public class RestSecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		// @formatter:off
        http.authorizeHttpRequests((authz) -> authz

                // Deny any request that doesn't match any authorization rule
				.requestMatchers(HttpMethod.GET, "/accounts/**").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
				.requestMatchers(HttpMethod.POST, "/accounts/**").hasAnyRole("ADMIN", "SUPERADMIN")
				.requestMatchers(HttpMethod.POST, "/accounts/**").hasAnyRole("ADMIN", "SUPERADMIN")
				.requestMatchers(HttpMethod.DELETE, "/accounts/**").hasRole("SUPERADMIN")
				.requestMatchers(HttpMethod.GET, "/authorities").hasAnyRole("USER", "ADMIN", "SUPERADMIN")
                .anyRequest().denyAll())
        .httpBasic(withDefaults())
        .csrf(CsrfConfigurer::disable);
        // @formatter:on

        return http.build();
	}

	//@Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {

		UserDetails user = User.withUsername("user").password(passwordEncoder.encode("user")).roles("USER").build();
		UserDetails admin = User.withUsername("admin").password(passwordEncoder.encode("admin")).roles("USER", "ADMIN").build();
		UserDetails superadmin = User.withUsername("superadmin").password(passwordEncoder.encode("superadmin")).roles("USER", "ADMIN", "SUPERADMIN").build();


		return new InMemoryUserDetailsManager(user, admin, superadmin);
	}
    
    @Bean
    public PasswordEncoder passwordEncoder() {
    	return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
