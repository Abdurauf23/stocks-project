package com.stocks.project.security;

import com.stocks.project.model.Role;
import static com.stocks.project.model.Role.ADMIN;
import static com.stocks.project.model.Role.USER;

import com.stocks.project.model.User;
import com.stocks.project.security.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SpringSecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // authentication and registration
                        .requestMatchers(HttpMethod.POST,"/authentication").permitAll()
                        .requestMatchers(HttpMethod.POST,"/register").permitAll()

                        // getting stocks
                        .requestMatchers(HttpMethod.GET,"/stocks").permitAll()
                        .requestMatchers(HttpMethod.GET,"/stocks/**").permitAll()

                        // interacting with User
                        .requestMatchers(HttpMethod.GET,"/users/**").hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers(HttpMethod.PUT,"/users/**").hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers(HttpMethod.DELETE,"/users/**").hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers(HttpMethod.GET,"/users").hasRole(ADMIN.name())
                        .requestMatchers(HttpMethod.POST,"/users").hasRole(ADMIN.name())

                        // interacting with security of info of the User
                        .requestMatchers("/security-info/**").hasRole(ADMIN.name())
                        .requestMatchers("/security-info").hasRole(ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/security-info/**").hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers(HttpMethod.PUT, "/security-info/**").hasAnyRole(ADMIN.name(), USER.name())

                        // getting, adding and deleting from the list of favourite stocks
                        .requestMatchers("/fav-stocks/**").hasAnyRole(ADMIN.name(), USER.name())

                        // related to swagger
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/**").permitAll()

                        .anyRequest().authenticated()
                ).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder bcrypt() {
        return new BCryptPasswordEncoder();
    }
}
