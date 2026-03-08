package SCG.beyeah1211.config;

import SCG.beyeah1211.common.Constants;
import SCG.beyeah1211.security.ApiAuthInterceptor;
import SCG.beyeah1211.security.AdminAuthInterceptor;
import SCG.beyeah1211.security.AdminPermissionInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private ApiAuthInterceptor apiAuthInterceptor;
    @Resource
    private AdminAuthInterceptor adminAuthInterceptor;
    @Resource
    private AdminPermissionInterceptor adminPermissionInterceptor;

    @Value("${app.cors.allowed-origins:http://localhost:22223,http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiAuthInterceptor)
                .addPathPatterns(
                        "/api/v1/cart/**",
                        "/api/v1/orders/**",
                        "/api/v1/users/**",
                        "/api/v1/addresses/**",
                        "/api/v1/after-sales/**"
                );

        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/v1/admin/**")
                .excludePathPatterns("/api/v1/admin/auth/login");

        registry.addInterceptor(adminPermissionInterceptor)
                .addPathPatterns("/api/v1/admin/**")
                .excludePathPatterns("/api/v1/admin/auth/login");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(resolveAllowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Trace-Id")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/goods-img/**")
                .addResourceLocations(asFileResourceLocation(Constants.FILE_GOODS_IMG_DIC));
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(asFileResourceLocation(Constants.FILE_UPLOAD_DIC));
    }

    private String[] resolveAllowedOrigins() {
        Set<String> origins = new LinkedHashSet<>();
        Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(origin -> {
                    origins.add(origin);
                    addLoopbackAlias(origin, origins);
                });
        return origins.toArray(new String[0]);
    }

    private void addLoopbackAlias(String origin, Set<String> origins) {
        try {
            URI uri = new URI(origin);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return;
            }
            int port = uri.getPort();
            String portPart = port > 0 ? ":" + port : "";
            if ("localhost".equalsIgnoreCase(host)) {
                origins.add(uri.getScheme() + "://127.0.0.1" + portPart);
            } else if ("127.0.0.1".equals(host)) {
                origins.add(uri.getScheme() + "://localhost" + portPart);
            }
        } catch (URISyntaxException ignored) {
            // Keep original origin untouched for non-URI inputs.
        }
    }

    private String asFileResourceLocation(String relativeDir) {
        Path path = Paths.get(relativeDir).toAbsolutePath().normalize();
        String location = path.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
