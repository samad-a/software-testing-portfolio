package ilp.samad.ilpcoursework1;

import com.fasterxml.jackson.databind.ObjectMapper;
import ilp.samad.ilpcoursework1.data.drone.*;
import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import ilp.samad.ilpcoursework1.data.request.Query;
import ilp.samad.ilpcoursework1.service.DroneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class DroneServiceTest {
    @Mock
    private RestTemplate restTemplate;

    private DroneService droneService;

    private ServicePoint mockServicePoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        String endpoint = "http://test-url/";
        droneService = new DroneService(endpoint, restTemplate, objectMapper);

        mockServicePoint = new ServicePoint(1, "Appleton Tower", new LngLat(-3.186, 55.944));
    }

    // Unit Tests for isDroneAvailableAtTime()
    @Test
    @DisplayName("isDroneAvailableAtTime - Boundary: Exactly at start of shift")
    void testAvailableAtShiftStart() {
        String droneId = "Drone-01";
        LocalDate date = LocalDate.of(2025, 1, 20); // A Monday
        LocalTime requestTime = LocalTime.of(9, 0);

        // Mock nested availability data structure
        List<Schedule> schedules = List.of(new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        DroneAvailability droneAvailability = new DroneAvailability(droneId, schedules);
        ServicePointDrones servicePointData = new ServicePointDrones(1, List.of(droneAvailability));
        ServicePointDrones[] allAvailability = new ServicePointDrones[] { servicePointData };

        boolean isAvailable = droneService.isDroneAvailableAtTime(droneId, date, requestTime, allAvailability);
        assertTrue(isAvailable, "Drone should be available exactly at the start of its shift.");
    }

    @Test
    @DisplayName("isDroneAvailableAtTime - Negative: One minute after shift ends")
    void testUnavailableAfterShiftEnd() {
        String droneId = "Drone-01";
        LocalDate date = LocalDate.of(2025, 1, 20); // A Monday
        LocalTime requestTime = LocalTime.of(17, 1);

        List<Schedule> schedules = List.of(new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        DroneAvailability droneAvailability = new DroneAvailability(droneId, schedules);
        ServicePointDrones servicePointData = new ServicePointDrones(1, List.of(droneAvailability));
        ServicePointDrones[] allAvailability = new ServicePointDrones[] { servicePointData };

        boolean isAvailable = droneService.isDroneAvailableAtTime(droneId, date, requestTime, allAvailability);
        assertFalse(isAvailable, "Drone should be unavailable after its shift ends.");
    }

    @Test
    @DisplayName("isDroneAvailableAtTime - Negative: Wrong day of the week")
    void testUnavailableOnWrongDay() {
        String droneId = "Drone-01";
        LocalDate date = LocalDate.of(2025, 1, 21); // A Tuesday
        LocalTime requestTime = LocalTime.of(12, 0);

        List<Schedule> schedules = List.of(new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        DroneAvailability droneAvailability = new DroneAvailability(droneId, schedules);
        ServicePointDrones servicePointData = new ServicePointDrones(1, List.of(droneAvailability));
        ServicePointDrones[] allAvailability = new ServicePointDrones[] { servicePointData };

        boolean isAvailable = droneService.isDroneAvailableAtTime(droneId, date, requestTime, allAvailability);
        assertFalse(isAvailable, "Drone should be unavailable if the day of the week does not match its schedule.");
    }


    // Unit Tests for getDronesByQuery()

    @Test
    @DisplayName("getDronesByQuery - Standard: Correctly filters by cooling capability")
    void testGetDronesByQueryCooling() {
        Drone coolingDrone = new Drone("Cool-Drone", "Cooler", new Capability(true, false, 500.0, 2000, 1.0, 10.0, 5.0));
        Drone standardDrone = new Drone("Basic-Drone", "Basic", new Capability(false, false, 500.0, 2000, 1.0, 10.0, 5.0));
        Drone[] fleet = new Drone[] { coolingDrone, standardDrone };
        when(restTemplate.getForObject(anyString(), eq(Drone[].class))).thenReturn(fleet);

        List<Query> queries = List.of(new Query("cooling", "=", "true"));
        List<String> result = droneService.getDronesByQuery(queries);

        assertEquals(1, result.size());
        assertEquals("Cool-Drone", result.getFirst());
    }

    @Test
    @DisplayName("getDronesByQuery - Negative: Returns empty when no drone matches requirements")
    void testGetDronesByQueryNoMatch() {
        Drone standardDrone = new Drone("Basic-Drone", "Basic", new Capability(false, false, 500.0, 2000, 1.0, 10.0, 5.0));
        Drone[] fleet = new Drone[] { standardDrone };
        when(restTemplate.getForObject(anyString(), eq(Drone[].class))).thenReturn(fleet);

        List<Query> queries = List.of(
                new Query("cooling", "=", "true")
        );

        List<String> result = droneService.getDronesByQuery(queries);

        assertTrue(result.isEmpty(), "Should return empty if no drone meets cooling needs.");
    }


    // Unit Tests for getServicePointForDrone()
    @Test
    @DisplayName("getServicePointForDrone - Standard: Correctly retrieves home base")
    void testGetServicePointForDrone() {
        String droneId = "Drone-01";

        // Mocking the availability call
        List<Schedule> schedules = List.of(new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        DroneAvailability da = new DroneAvailability(droneId, schedules);
        ServicePointDrones spd = new ServicePointDrones(1, List.of(da));
        when(restTemplate.getForObject(contains("drones-for-service-points"), eq(ServicePointDrones[].class)))
                .thenReturn(new ServicePointDrones[]{spd});

        // Mocking the service point lookup
        when(restTemplate.getForObject(contains("service-points"), eq(ServicePoint[].class)))
                .thenReturn(new ServicePoint[]{mockServicePoint});

        Optional<ServicePoint> result = droneService.getServicePointForDrone(droneId);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().id());
    }


    // Unit Tests for getServicePointById()
    @Test
    @DisplayName("getServicePointById - Standard: Finds existing service point")
    void testGetServicePointByIdSuccess() {
        Integer targetId = 1;
        when(restTemplate.getForObject(anyString(), eq(ServicePoint[].class)))
                .thenReturn(new ServicePoint[]{mockServicePoint});

        Optional<ServicePoint> result = droneService.getServicePointById(targetId);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().id());
    }

    @Test
    @DisplayName("getServicePointById - Negative: Returns empty Optional for non-existent ID")
    void testGetServicePointByIdNotFound() {
        Integer targetId = 99;
        when(restTemplate.getForObject(anyString(), eq(ServicePoint[].class)))
                .thenReturn(new ServicePoint[]{mockServicePoint});

        Optional<ServicePoint> result = droneService.getServicePointById(targetId);

        assertFalse(result.isPresent(), "Should return empty Optional if ID is not found.");
    }
}
