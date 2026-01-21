package ilp.samad.ilpcoursework1;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import ilp.samad.ilpcoursework1.data.request.Query;
import ilp.samad.ilpcoursework1.service.DroneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestClientTest
public class DroneServiceIntegrationTest {

    private DroneService droneService;
    private MockRestServiceServer server;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = restTemplateBuilder.build();
        server = MockRestServiceServer.createServer(restTemplate);
        droneService = new DroneService("http://test-api/", restTemplate, objectMapper);
    }

    @Test
    @DisplayName("External Integration - Correctly handle valid REST response")
    void testExternalApiSuccess() {
        String mockJsonResponse = """
        [
          {"id": "Drone-01", "name": "Test Drone", "capability": {"cooling": true, "heating": false}}
        ]
        """;

        this.server.expect(requestTo(containsString("/drones")))
                .andRespond(withSuccess(mockJsonResponse, MediaType.APPLICATION_JSON));

        List<String> ids = droneService.getDronesByQuery(List.of(new Query("cooling", "=", "true")));

        assertEquals(1, ids.size());
        assertEquals("Drone-01", ids.getFirst());
    }

    @Test
    @DisplayName("Robustness - Handle 500 Internal Server Error")
    void testExternalApiFailure() {
        // Simulating the external server crashing
        this.server.expect(requestTo(containsString("/drones")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        List<String> result = droneService.getDronesByQuery(List.of(new Query("cooling", "=", "true")));

        assertTrue(result.isEmpty(), "Service should handle external 500 errors gracefully by returning empty results.");
    }
}
