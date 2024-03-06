package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {
	
	@Bean
	public UserDetailsService getUserDetailService() {

		return new UserDetailsServiceImpl();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticcationProvider() {

		DaoAuthenticationProvider daoauthenticationprovider = new DaoAuthenticationProvider();
		daoauthenticationprovider.setUserDetailsService(this.getUserDetailService());
		daoauthenticationprovider.setPasswordEncoder(passwordEncoder());

		return daoauthenticationprovider;
	}

	
	
	
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticcationProvider());// here we are using database that's why DB authenticator otherwise inmemory Authentication
	}
	
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	http
        .authorizeRequests((requests) ->
            requests
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasRole("USER")
                .requestMatchers("/**").permitAll()
                .anyRequest().authenticated()
        )
        .formLogin((form) ->
            form
                .loginPage("/signin")
                .defaultSuccessUrl("/user/index", true)
                .loginProcessingUrl("/dologin")
                .permitAll()
        )
        .logout((logout) ->
            logout
                .permitAll()
        )
        .csrf().disable();

    return http.build();
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user =
//             User.withDefaultPasswordEncoder()
//             .username("user")
//             .password("password")
//             .roles("USER")
//             .build();
//
//        return new InMemoryUserDetailsManager(user);
//    }
}
