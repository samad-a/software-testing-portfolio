package ilp.samad.ilpcoursework1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ilp.samad.ilpcoursework1.data.drone.*;
import ilp.samad.ilpcoursework1.data.geometry.RestrictedArea;
import ilp.samad.ilpcoursework1.data.request.MedDispatchRec;
import ilp.samad.ilpcoursework1.data.request.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DroneService {
    private final String ilpServiceEndpoint;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DroneService(String ilpServiceEndpoint, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.ilpServiceEndpoint = ilpServiceEndpoint;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<String> getDronesWithCooling(boolean state) {
        String url = ilpServiceEndpoint + "drones";

        Drone[] allDrones = restTemplate.getForObject(url, Drone[].class);

        if (allDrones == null) {
            return List.of();
        }

        // only keep drones which match state, and return collected IDs as a list
        return Arrays.stream(allDrones)
                .filter(drone -> drone.capability().cooling() == state)
                .map(Drone::id)
                .toList();
    }

    public Optional<Drone> getDrone(String id) {
        String url = ilpServiceEndpoint + "drones";

        Drone[] allDrones = restTemplate.getForObject(url, Drone[].class);

        if (allDrones == null) {
            return Optional.empty();
        }

        return Arrays.stream(allDrones)
                .filter(drone -> drone.id().equals(id))
                .findFirst();
    }

    public List<String> getDronesByQuery(List<Query> queries) {
        String url = ilpServiceEndpoint + "drones";
        Drone[] allDrones = restTemplate.getForObject(url, Drone[].class);

        if (allDrones == null) {
            return List.of();
        }

        return Arrays.stream(allDrones)
                .filter(drone -> {
                    for (Query q : queries) {
                        if (!checkCondition(drone, q.attribute(), q.operator(), q.value())) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(Drone::id)
                .toList();
    }

    private boolean checkCondition(Drone drone, String attribute, String operator, String value) {
        JsonNode root = objectMapper.valueToTree(drone);
        JsonNode node = root.findValue(attribute);

        if (node == null || node.isMissingNode()) {
            return false;
        }

        if (node.isNumber()) {
            try {
                double nodeValue = node.asDouble();
                double queryValue = Double.parseDouble(value);

                return switch (operator) {
                    case "<" -> nodeValue < queryValue;
                    case ">" -> nodeValue > queryValue;
                    case "!=" -> nodeValue != queryValue;
                    case "=" -> nodeValue == queryValue;
                    default -> false;
                };
            } catch (NumberFormatException e) {
                return false;
            }
        }
        else if (node.isBoolean()) {
            boolean nodeValue = node.asBoolean();
            boolean queryValue = Boolean.parseBoolean(value);

            return switch (operator) {
                case "=" -> nodeValue == queryValue;
                case "!=" -> nodeValue != queryValue;
                default -> false;
            };
        }
        else {
            String nodeValue = node.asText();
            return switch (operator) {
                case "=" -> nodeValue.equals(value);
                case "!=" -> !nodeValue.equals(value);
                default -> false;
            };
        }
    }

    // task 3a reuses task 3b for cleaner code
    public List<String> getDronesByAttribute(String attribute, String value) {
        return getDronesByQuery(List.of(new Query(attribute, "=", value)));
    }



    public List<String> getAvailableDrones(List<MedDispatchRec> orders) {
        if (orders == null || orders.isEmpty()) return List.of();

        String dronesUrl = ilpServiceEndpoint + "drones";
        String availabilityUrl = ilpServiceEndpoint + "drones-for-service-points";

        Drone[] allDronesArr = restTemplate.getForObject(dronesUrl, Drone[].class);
        ServicePointDrones[] allAvailability = restTemplate.getForObject(availabilityUrl, ServicePointDrones[].class);

        if (allDronesArr == null || allAvailability == null) return List.of();

        List<Drone> candidates = Arrays.asList(allDronesArr);

        for (MedDispatchRec order : orders) {
            candidates = candidates.stream()
                    .filter(drone -> canHandleOrder(drone, order, allAvailability))
                    .toList();
            if (candidates.isEmpty()) {
                return List.of();
            }
        }

        return candidates.stream()
                .map(Drone::id)
                .toList();
    }


    private boolean canHandleOrder(Drone drone, MedDispatchRec order, ServicePointDrones[] allAvailability) {
        // heating/cooling check
        if (Boolean.TRUE.equals(order.requirements().cooling()) && !drone.capability().cooling()) return false;
        if (Boolean.TRUE.equals(order.requirements().heating()) && !drone.capability().heating()) return false;

        // capacity check
        if (order.requirements().capacity() > drone.capability().capacity()) return false;

        // availability check
        return isDroneAvailableAtTime(drone.id(), order.date(), order.time(), allAvailability);
    }

    public boolean isDroneAvailableAtTime(String droneId, LocalDate date, LocalTime time, ServicePointDrones[] allAvailability) {
        if (date == null || time == null) return true;

        DayOfWeek day = date.getDayOfWeek();

        for (ServicePointDrones sp : allAvailability) {
            for (DroneAvailability da : sp.drones()) {
                if (da.id().equals(droneId)) {
                    for (Schedule s : da.availability()) {
                        if (s.dayOfWeek() == day) {
                            if (!time.isBefore(s.from()) && !time.isAfter(s.until())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public List<RestrictedArea> getNoFlyZones() {
        String url = ilpServiceEndpoint + "restricted-areas";
        RestrictedArea[] zones = restTemplate.getForObject(url, RestrictedArea[].class);
        if (zones == null) return List.of();
        return Arrays.asList(zones);
    }

    public Optional<ServicePoint> getServicePointForDrone(String droneId) {
        String availabilityUrl = ilpServiceEndpoint + "drones-for-service-points";
        ServicePointDrones[] allAvailability = restTemplate.getForObject(availabilityUrl, ServicePointDrones[].class);

        if (allAvailability == null) return Optional.empty();

        Integer servicePointId = null;

        for (ServicePointDrones sp : allAvailability) {
            for (DroneAvailability da : sp.drones()) {
                if (da.id().equals(droneId)) {
                    servicePointId = sp.servicePointId();
                    break;
                }
            }
            if (servicePointId != null) break;
        }

        if (servicePointId == null) {
            return Optional.empty();
        }

        return getServicePointById(servicePointId);
    }

    public Optional<ServicePoint> getServicePointById(Integer id) {
        String url = ilpServiceEndpoint + "service-points";
        ServicePoint[] points = restTemplate.getForObject(url, ServicePoint[].class);

        if (points == null) return Optional.empty();

        return Arrays.stream(points)
                .filter(sp -> id.equals(sp.id()))
                .findFirst();
    }
}
