package ilp.samad.ilpcoursework1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ilp.samad.ilpcoursework1.data.drone.Drone;
import ilp.samad.ilpcoursework1.data.drone.DroneAvailability;
import ilp.samad.ilpcoursework1.data.drone.Schedule;
import ilp.samad.ilpcoursework1.data.drone.ServicePointDrones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;


public class DataTransferObjectTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }


    // Standard Case: Valid Drone JSON
    @Test
    @DisplayName("JSON Deserialization - Standard: Valid Drone and Capability")
    void testDroneDeserialization() throws JsonProcessingException {
        String json = """
                {
                  "id": "Drone-01",
                  "name": "Standard Drone",
                  "capability": {
                    "cooling": true,
                    "heating": true,
                    "capacity": 500.0,
                    "maxMoves": 2000,
                    "costPerMove": 1.25,
                    "costInitial": 10.0,
                    "costFinal": 5.0
                  }
                }
                """;

        Drone drone = objectMapper.readValue(json, Drone.class);

        assertNotNull(drone);
        assertEquals("Drone-01", drone.id());
        assertEquals("Standard Drone", drone.name());
        assertTrue(drone.capability().cooling());
        assertEquals(500.0, drone.capability().capacity());
        assertEquals(2000, drone.capability().maxMoves());
    }


    // Robustness Case: Unknown Properties
    @Test
    @DisplayName("JSON Deserialization - Robustness: Ignore unknown JSON fields")
    void testUnknownFieldsAreIgnored() throws JsonProcessingException {
        // testing how it acts with extra fields
        String json = """
                {
                  "id": "Drone-02",
                  "name": "Advanced Drone",
                  "firmwareVersion": "v2.1.0",
                  "capability": {
                    "cooling": false,
                    "heating": true,
                    "sensorArray": ["LIDAR", "RADAR"]
                  }
                }
                """;

        Drone drone = objectMapper.readValue(json, Drone.class);

        assertNotNull(drone);
        assertEquals("Drone-02", drone.id());
    }


    // Complex Case: Nested Availability & Schedule
    @Test
    @DisplayName("JSON Deserialization - Standard: Complex Nested Availability Structure")
    void testAvailabilityDeserialization() throws JsonProcessingException {
        String json = """
                {
                  "servicePointId": 1,
                  "drones": [
                    {
                      "id": "Drone-01",
                      "availability": [
                        {
                          "dayOfWeek": "MONDAY",
                          "from": "09:00",
                          "until": "17:00"
                        }
                      ]
                    }
                  ]
                }
                """;

        ServicePointDrones spd = objectMapper.readValue(json, ServicePointDrones.class);

        assertNotNull(spd);
        assertEquals(1, spd.servicePointId());
        assertFalse(spd.drones().isEmpty());

        DroneAvailability da = spd.drones().getFirst();
        assertEquals("Drone-01", da.id());

        Schedule schedule = da.availability().getFirst();
        assertEquals(DayOfWeek.MONDAY, schedule.dayOfWeek());
        assertEquals(LocalTime.of(9, 0), schedule.from());
        assertEquals(LocalTime.of(17, 0), schedule.until());
    }


    // Negative Case: Malformed JSON
    @Test
    @DisplayName("JSON Deserialization - Negative: Handle malformed JSON syntax")
    void testMalformedJsonThrowsException() {
        String malformedJson = "{ \"id\": \"Drone-01\", \"capability\": { \"cooling\": true "; // Missing closing braces

        assertThrows(JsonProcessingException.class, () -> objectMapper.readValue(malformedJson, Drone.class));
    }
}
