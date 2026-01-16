package ilp.samad.ilpcoursework1.controller;

import ilp.samad.ilpcoursework1.data.drone.Drone;
import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import ilp.samad.ilpcoursework1.data.request.*;
import ilp.samad.ilpcoursework1.data.response.FlightResponse;
import ilp.samad.ilpcoursework1.service.CalculationService;
import ilp.samad.ilpcoursework1.service.DroneService;
import ilp.samad.ilpcoursework1.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ServiceController {

    private final CalculationService calculationService;
    private final DroneService droneService;
    private final FlightService flightService;

    public ServiceController(CalculationService calculationService, DroneService droneService, FlightService flightService) {
        this.calculationService = calculationService;
        this.droneService = droneService;
        this.flightService = flightService;
    }

    @GetMapping("/uid")
    public String uid() {
        return "s2544386";
    }

    @PostMapping("/distanceTo")
    public double distanceTo(@Valid @RequestBody LngLatPairRequest request) {
        return calculationService.calculateDistance(request.position1(), request.position2());
    }

    @PostMapping("/isCloseTo")
    public boolean closeTo(@Valid @RequestBody LngLatPairRequest request) {
        return calculationService.calculateClose(request.position1(), request.position2());
    }

    @PostMapping("/nextPosition")
    public LngLat nextPosition(@Valid @RequestBody NextPositionRequest request) {
        return calculationService.calculateNextPosition(request.start(), request.angle());
    }

    @PostMapping("/isInRegion")
    public boolean isInRegion(@Valid @RequestBody IsInRegionRequest request) {
        return calculationService.calculateIsInRegion(request.position(), request.region());
    }

    @GetMapping("dronesWithCooling/{state}")
    public List<String> dronesWithCooling(@PathVariable boolean state) {
        return droneService.getDronesWithCooling(state);
    }

    @GetMapping("droneDetails/{id}")
    public Drone droneDetails(@PathVariable String id) {
        return droneService.getDrone(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Drone with ID " + id + " not found"
                ));
    }

    @GetMapping("/queryAsPath/{attribute}/{value}")
    public List<String> queryAsPath(@PathVariable String attribute, @PathVariable String value) {
        return droneService.getDronesByAttribute(attribute, value);
    }

    @PostMapping("/query")
    public List<String> query(@RequestBody List<Query> queries) {
        return droneService.getDronesByQuery(queries);
    }

    @PostMapping("/queryAvailableDrones")
    public List<String> queryAvailableDrones(@Valid @RequestBody List<MedDispatchRec> orders) {
        return droneService.getAvailableDrones(orders);
    }

    @PostMapping("/calcDeliveryPath")
    public FlightResponse calcDeliveryPath(@Valid @RequestBody List<MedDispatchRec> orders) {
        return flightService.calculateDeliveryPath(orders);
    }

    @PostMapping("/calcDeliveryPathAsGeoJson")
    public Map<String, Object> calcDeliveryPathAsGeoJson(@Valid @RequestBody List<MedDispatchRec> orders) {
        return flightService.calculateDeliveryPathAsGeoJson(orders);
    }
}
