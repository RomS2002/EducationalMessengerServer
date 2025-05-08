package ru.roms2002.messenger.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private JwtWebFilter jwtFilter;

	@Value("${infoserver.ip}")
	private String LOCAL_NETWORK_CIDR;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable())
				.addFilterBefore(jwtFilter, BasicAuthenticationFilter.class).authorizeHttpRequests(
						(requests) -> requests.requestMatchers("/mail/**", "/notification/**")
								.access(new WebExpressionAuthorizationManager(
										"hasIpAddress('" + LOCAL_NETWORK_CIDR + "')"))
								.requestMatchers("/auth", "/auth/register", "/error", "/uploads/**")
								.permitAll().anyRequest().authenticated());
		return http.build();
	}
}
