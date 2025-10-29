package dev.saul.gijoneventos.config;

import static org.springframework.security.config.Customizer.withDefaults;
import dev.saul.gijoneventos.security.JpaUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${api-endpoint}")
    String endpoint;

    private final JpaUserDetailsService jpaUserDetailsService;

    public SecurityConfiguration(JpaUserDetailsService jpaUserDetailsService) {
        this.jpaUserDetailsService = jpaUserDetailsService;
    }

     @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Habilita CORS usando la configuración definida en corsConfigurationSource()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Deshabilita CSRF completamente para facilitar desarrollo
            .csrf(csrf -> csrf.disable())
            // Permite que H2 console se muestre en iframe del mismo dominio
            .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .formLogin(form -> form.disable())
            .logout(out -> out
                .logoutUrl(endpoint + "/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"))
            .authorizeHttpRequests(auth -> auth
                // IMPORTANTE: Las rutas más específicas primero, anyRequest() SIEMPRE al final
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(endpoint + "/public").permitAll()
                .requestMatchers(endpoint + "/register", endpoint + "/register/**").permitAll()
                .requestMatchers(HttpMethod.GET, endpoint + "/private").hasRole("ADMIN")
                .requestMatchers(endpoint + "/login").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, endpoint + "/events/**").permitAll()
                .requestMatchers(HttpMethod.POST, endpoint + "/events/**").authenticated()
                .requestMatchers(HttpMethod.PUT, endpoint + "/events/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, endpoint + "/events/**").authenticated()
                .anyRequest().authenticated())
            .userDetailsService(jpaUserDetailsService)
            .httpBasic(withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }
   /* Giaco
     @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Habilita CORS usando la configuración definida en corsConfigurationSource()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Deshabilita CSRF para la consola H2 y otras pruebas
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .disable())
            // Permite que H2 console se muestre en iframe del mismo dominio
            .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .formLogin(form -> form.disable())
            .logout(out -> out
                .logoutUrl(endpoint + "/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"))
            .authorizeHttpRequests(auth -> auth
                //claude
                .anyRequest().permitAll()
                // fin claude
                .requestMatchers("h2-console/**").permitAll()
                .requestMatchers(endpoint + "/public").permitAll()
                .requestMatchers(HttpMethod.GET, endpoint + "/private").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, endpoint + "/register").permitAll()
                .requestMatchers(endpoint + "/login").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated())
            .userDetailsService(jpaUserDetailsService)
            .httpBasic(withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    } */

    // Password encoder para almacenar contraseñas de forma segura (BCrypt)
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configuración global de CORS para permitir que el frontend se comunique con el backend
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cambia la URL si tu frontend corre en otro puerto o dominio
        // configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Necesario si usas cookies o auth headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}