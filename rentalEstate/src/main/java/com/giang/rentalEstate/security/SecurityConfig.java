package com.giang.rentalEstate.security;

import com.giang.rentalEstate.enums.Rolename;
import com.giang.rentalEstate.filter.JwtTokenFilter;
import com.giang.rentalEstate.model.Role;
import com.giang.rentalEstate.service.impl.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService; //là 1 interface trong Spring Security, load thông tin user từ CSDL dựa trên username
    private final JwtTokenFilter jwtTokenFilter;
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){ //hash password trước khi đưa vào CSDL. Khi user login, mật khẩu được nhập được băm, so sánh với hàm băm được lưu trữ
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                        "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/configuration/**",
                                "/webjars/**",
                                "/v2/api-docs"
                        ).permitAll()
                        .requestMatchers("/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/verify-email",
                                "/api/auth/refresh",
                                "/api/auth/resend-verification",
                                "/api/drive/upload"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/property").hasAnyRole(Rolename.OWNER.toString())
                        .requestMatchers(HttpMethod.PUT,"/api/property/{id}").hasAnyRole(Rolename.OWNER.toString())
                        .requestMatchers(HttpMethod.DELETE,"/api/property/{id}").hasAnyRole(Rolename.OWNER.toString())
                        .requestMatchers(HttpMethod.GET,"/api/property/me").hasAnyRole(Rolename.OWNER.toString(), Rolename.ADMIN.toString())
                        .requestMatchers(HttpMethod.PUT,"/api/property/{id}/status").hasAnyRole(Rolename.ADMIN.toString())

                        .requestMatchers(HttpMethod.POST,"/api/rental-request/{propertyId}").hasAnyRole(Rolename.CUSTOMER.toString())
                        .requestMatchers(HttpMethod.DELETE,"/api/rental-request/{propertyId}").hasAnyRole(Rolename.CUSTOMER.toString())
                        .requestMatchers(HttpMethod.GET,"/api/rental-request/{propertyId}").hasAnyRole(Rolename.OWNER.toString())
                        .requestMatchers(HttpMethod.GET,"/api/rental-request/{propertyId}/{customerId}").hasAnyRole(Rolename.CUSTOMER.toString())
                        .requestMatchers(HttpMethod.PUT,"/api/rental-request/{requestId}/status").hasAnyRole(Rolename.OWNER.toString())
                        .requestMatchers(HttpMethod.GET,"/api/rental-request/{propertyId}/customers").hasAnyRole(Rolename.CUSTOMER.toString())
                        .requestMatchers(HttpMethod.GET,"/api/rental-request/customer/properties").hasAnyRole(Rolename.CUSTOMER.toString())
                        .requestMatchers(HttpMethod.GET,"/api/rental-request/customer/{customerId}/properties").hasAnyRole(Rolename.OWNER.toString())

                        .requestMatchers(HttpMethod.GET,"/api/dashboard/owner").hasAnyRole(Rolename.OWNER.toString())
                        .requestMatchers(HttpMethod.GET,"/api/dashboard/customer").hasAnyRole(Rolename.CUSTOMER.toString())
                        .requestMatchers(HttpMethod.GET,"/api/dashboard/admin").hasAnyRole(Rolename.ADMIN.toString())

                        .requestMatchers(HttpMethod.GET,"/api/customers/owner/{ownerId}").hasAnyRole(Rolename.OWNER.toString())

                        .requestMatchers(HttpMethod.POST,"/api/saved-properties/**").hasAnyRole(Rolename.CUSTOMER.toString())

                        .requestMatchers(HttpMethod.GET,"/get-all-users").hasAnyRole(Rolename.ADMIN.toString())
                        .requestMatchers(HttpMethod.DELETE,"/api/user/{id}").hasAnyRole(Rolename.ADMIN.toString())
                        .requestMatchers(HttpMethod.PUT,"/api/user/{id}/role").hasAnyRole(Rolename.ADMIN.toString())


//                        .requestMatchers("/user/{id}").hasAnyRole(Rolename.ADMIN.toString())
//                        .requestMatchers("/admin/**").hasAnyRole(Rolename.ADMIN.toString())
//                        .requestMatchers("/customer/**").hasAnyRole(Rolename.CUSTOMER.toString())
//                        .requestMatchers("/owner/**").hasAnyRole(Rolename.OWNER.toString())

                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); //DaoAuthenticationProvider: authentication provider mặc định trong Spring Security, làm việc với UserDetailsService để load thông tin user từ CSDL
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder()); //so sánh mật khẩu được băm dùng BCryptPasswordEncoder trong quá trình xác thức


        return provider;
    }
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5001", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
