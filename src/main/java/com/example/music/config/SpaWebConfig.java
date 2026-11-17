package com.example.music.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.List;

/**
 * Serves the Angular build (copied into classpath:/static/ at packaging time).
 * Unknown front-end routes such as /playlists/3 fall back to index.html so that
 * deep links and page refreshes work. Backend paths are never rewritten, so an
 * unknown /api/... URL still returns 404.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    private static final List<String> BACKEND_PREFIXES =
            List.of("api/", "actuator", "v3/", "swagger-ui", "h2-console");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        boolean backendPath = BACKEND_PREFIXES.stream().anyMatch(resourcePath::startsWith);
                        boolean looksLikeFile = resourcePath.contains(".");
                        if (backendPath || looksLikeFile) {
                            return null;
                        }
                        Resource index = new ClassPathResource("/static/index.html");
                        return index.exists() ? index : null;
                    }
                });
    }
}
