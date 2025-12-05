package com.example.universidadeESN3.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permitir credenciais
        config.setAllowCredentials(true);

        // Permitir todas as origens (para desenvolvimento)
        config.addAllowedOriginPattern("*");

        // Permitir todos os headers
        config.addAllowedHeader("*");

        // Permitir todos os métodos HTTP
        config.addAllowedMethod("*");

        // Aplicar configuração para todas as rotas
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
