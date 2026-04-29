package com.ufund.api.ufundapi.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.ufund.api.ufundapi.services.DetailsService;
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
        DetailsService detailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(detailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
    @Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler reqHandler = new CsrfTokenRequestAttributeHandler();
        reqHandler.setCsrfRequestAttributeName(null);
		http
            .authorizeHttpRequests(auth -> auth
			.requestMatchers("/index.html", "/",
				"/home", "/*.js", "/*.css", "/*.ico", "/user/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/user/create").permitAll()
                .requestMatchers("/login").anonymous()
				.anyRequest().permitAll()
			).exceptionHandling(exp -> exp
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) -> response.sendError(403),
                        new AntPathRequestMatcher("/**")
                    )
            ).httpBasic(basic -> basic
                .securityContextRepository(new HttpSessionSecurityContextRepository())
            ).csrf(csrf ->
				csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(reqHandler)
                .ignoringRequestMatchers("/user/create")
            ).logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            ).cors(Customizer.withDefaults());
		return http.build();
	}
}
