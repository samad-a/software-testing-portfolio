package ilp.samad.ilpcoursework1.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class IlpRestServiceConfig {

    @Bean
    public String ilpServiceEndpoint() {
        String envEndpoint = System.getenv("ILP_ENDPOINT");

        if (envEndpoint != null && !envEndpoint.isEmpty()) {
            // incase automarker doesnt end url with /
            envEndpoint = envEndpoint.endsWith("/") ? envEndpoint : envEndpoint + "/";
            return envEndpoint;
        }
        // fallback URL
        return "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/";
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
