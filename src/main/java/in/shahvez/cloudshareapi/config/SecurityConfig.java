package in.shahvez.cloudshareapi.config;

import in.shahvez.cloudshareapi.security.ClerkJwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ClerkJwtAuthFilter clerkJwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
         httpSecurity.cors(Customizer.withDefaults())
                 .csrf(AbstractHttpConfigurer::disable)
                 .authorizeHttpRequests(auth -> auth.requestMatchers("/webhooks/**","/files/public/**","/files/download/**").permitAll()
                         .requestMatchers("/payments/**").authenticated()
                         .anyRequest().authenticated())
                 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                 .addFilterBefore(clerkJwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
         return httpSecurity.build();

    }
     @Bean
    public CorsFilter corsFilter(){
       return new CorsFilter(corsConfigurationSource());
    }

    private UrlBasedCorsConfigurationSource  corsConfigurationSource(){
        CorsConfiguration confing = new CorsConfiguration();
        confing.setAllowedOrigins(List.of("https://cloudsharingfile.netlify.app","http://localhost:5173"));
        confing.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTION"));
        confing.setAllowedHeaders(List.of("Authorization","Content-Type"));
        confing.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",confing);
        return source;


    }
}
