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

	@Autowired
	private AccountEnabledFilter accountEnabledFilter;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
//        configuration.setAllowedMethods(List.of("*"));
//        configuration.setAllowedHeaders(List.of("*"));
//        configuration.setAllowCredentials(Boolean.TRUE);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

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

//    protected void configure(HttpSecurity http) throws Exception {
//        http.cors().and()
//                .csrf()
//                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                .and()
//                .authorizeRequests()
//                .antMatchers("/api/auth").permitAll()
//                .antMatchers("/api/user/register").permitAll()
//                .antMatchers("/ws").permitAll()
//                .antMatchers("/static/**").permitAll()
//                .antMatchers("/images/**").permitAll()
//                .antMatchers("/").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
//                .and()
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        http.addFilterBefore(jwtWebConfig, UsernamePasswordAuthenticationFilter.class);
//    }
}
