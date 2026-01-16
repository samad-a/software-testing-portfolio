package ilp.samad.ilpcoursework1;

import com.fasterxml.jackson.databind.ObjectMapper;
import ilp.samad.ilpcoursework1.controller.ServiceController;
import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import ilp.samad.ilpcoursework1.data.request.LngLatPairRequest;
import ilp.samad.ilpcoursework1.data.request.NextPositionRequest;
import ilp.samad.ilpcoursework1.data.geometry.Region;
import ilp.samad.ilpcoursework1.service.CalculationService;
import ilp.samad.ilpcoursework1.service.DroneService;
import ilp.samad.ilpcoursework1.service.FlightService;
import ilp.samad.ilpcoursework1.service.PathService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
public class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    @MockBean
    private DroneService droneService;

    @MockBean
    private PathService pathService;

    @MockBean
    private CalculationService calculationService;

    // to convert DTOs to JSON strings
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/uid should return correct student ID")
    public void testUidEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2544386"));
    }


    @Test
    @DisplayName("POST /api/v1/distanceTo should call the service and return the result")
    void testDistanceToEndpoint() throws Exception {
        when(calculationService.calculateDistance(any(LngLat.class), any(LngLat.class))).thenReturn(10.0);

        LngLatPairRequest request = new LngLatPairRequest(new LngLat(0.0, 0.0), new LngLat(3.0, 4.0));

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("10.0"));
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo should return 400 Bad Request for invalid input")
    void testDistanceToEndpoint_InvalidInput() throws Exception {
        String invalidJson = "{\"position1\": {\"lng\": 0.0, \"lat\": 0.0}}";

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo should call the service and return the result")
    void testIsCloseToEndpoint() throws Exception {
        when(calculationService.calculateClose(any(LngLat.class), any(LngLat.class))).thenReturn(true);

        LngLatPairRequest request = new LngLatPairRequest(new LngLat(0.0, 0.0), new LngLat(0.0, 0.0001));

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition should return the position from the service")
    void testNextPositionEndpoint() throws Exception {
        LngLat expectedPosition = new LngLat(2.0, 4.0);
        when(calculationService.calculateNextPosition(any(LngLat.class), anyDouble())).thenReturn(expectedPosition);

        NextPositionRequest request = new NextPositionRequest(new LngLat(0.0, 0.0), 45.0);

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").value(2.0))
                .andExpect(jsonPath("$.lat").value(4.0));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion should return the boolean from the service")
    void testIsInRegionEndpoint() throws Exception {
        when(calculationService.calculateIsInRegion(any(LngLat.class), any(Region.class))).thenReturn(true);

        String validJson = """
        {
            "position": {"lng": 1.0, "lat": 1.0},
            "region": {
                "name": "square",
                "vertices": [
                    {"lng": 0.0, "lat": 0.0}, {"lng": 2.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 2.0}, {"lng": 0.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 0.0}
                ]
            }
        }
        """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
