package ilp.samad.ilpcoursework1.service;

import ilp.samad.ilpcoursework1.data.drone.Drone;
import ilp.samad.ilpcoursework1.data.drone.ServicePoint;
import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import ilp.samad.ilpcoursework1.data.geometry.RestrictedArea;
import ilp.samad.ilpcoursework1.data.request.MedDispatchRec;
import ilp.samad.ilpcoursework1.data.response.Delivery;
import ilp.samad.ilpcoursework1.data.response.DronePath;
import ilp.samad.ilpcoursework1.data.response.FlightResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class FlightService {
    private final DroneService droneService;
    private final PathService pathService;
    private final CalculationService calculationService;

    public FlightService(DroneService droneService, PathService pathService, CalculationService calculationService) {
        this.droneService = droneService;
        this.pathService = pathService;
        this.calculationService = calculationService;
    }

    public FlightResponse calculateDeliveryPath(List<MedDispatchRec> orders) {
        List<RestrictedArea> noFlyZones = droneService.getNoFlyZones();

        Map<LocalDate, List<MedDispatchRec>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(MedDispatchRec::date));

        List<DronePath> allDronePaths = new ArrayList<>();
        double totalCost = 0;
        int totalMoves = 0;

        for (Map.Entry<LocalDate, List<MedDispatchRec>> entry : ordersByDate.entrySet()) {
            List<MedDispatchRec> pendingOrders = new ArrayList<>(entry.getValue());

            List<Integer> failedOrderIds = new ArrayList<>();

            while (!pendingOrders.isEmpty()) {
                if (pendingOrders.stream().allMatch(o -> failedOrderIds.contains(o.id()))) {
                    break;
                }

                List<String> availableDroneIds = droneService.getAvailableDrones(pendingOrders);

                if (availableDroneIds.isEmpty()) {
                    MedDispatchRec nextOrder = pendingOrders.stream()
                            .filter(o -> !failedOrderIds.contains(o.id()))
                            .findFirst()
                            .orElse(null);

                    if (nextOrder != null) {
                        availableDroneIds = droneService.getAvailableDrones(List.of(nextOrder));
                    }
                }

                if (availableDroneIds.isEmpty()) {
                    if (!pendingOrders.isEmpty()) {
                        System.err.println("No drone available for order " + pendingOrders.getFirst().id());
                        pendingOrders.removeFirst();
                    }
                    continue;
                }

                String droneId = availableDroneIds.getFirst();
                Drone drone = droneService.getDrone(droneId).orElseThrow();
                ServicePoint startServicePoint = droneService.getServicePointForDrone(droneId).orElse(null);

                if (startServicePoint == null) {
                    pendingOrders.removeFirst();
                    continue;
                }
                List<MedDispatchRec> optimizedQueue = optimizeRoute(startServicePoint.location(), pendingOrders);

                List<Delivery> flightDeliveries = new ArrayList<>();
                List<MedDispatchRec> packedOrders = new ArrayList<>();

                LngLat currentLocation = startServicePoint.location();
                int currentFlightMoves = 0;
                double currentPayload = 0;

                for (MedDispatchRec order : optimizedQueue) {
                    if (currentPayload + order.requirements().capacity() > drone.capability().capacity()) {
                        break;
                    }

                    List<LngLat> rawPath = pathService.findPath(currentLocation, order.delivery(), noFlyZones);
                    List<LngLat> path = new ArrayList<>(rawPath);

                    if (path.isEmpty()) {
                        System.err.println("Path not found from " + currentLocation + " to " + order.delivery());
                        break;
                    }

                    List<LngLat> returnPathRaw = pathService.findPath(order.delivery(), startServicePoint.location(), noFlyZones);

                    int legMoves = (path.size() - 1) + 2;
                    int returnMoves = (returnPathRaw.isEmpty() ? 0 : returnPathRaw.size() - 1);

                    // battery Check
                    if (currentFlightMoves + legMoves + returnMoves > drone.capability().maxMoves()) {
                        break;
                    }

                    currentFlightMoves += legMoves;
                    currentPayload += order.requirements().capacity();

                    // add extra move for hovering (delivered)
                    LngLat hoverLocation = path.getLast();
                    path.add(hoverLocation);

                    flightDeliveries.add(new Delivery(order.id(), path));
                    packedOrders.add(order);

                    currentLocation = hoverLocation;
                }

                if (packedOrders.isEmpty()) {
                    System.err.println("Drone " + droneId + " cannot handle first order " + optimizedQueue.getFirst().id());
                    pendingOrders.remove(optimizedQueue.getFirst());
                    continue;
                }

                List<LngLat> finalReturnPath = pathService.findPath(currentLocation, startServicePoint.location(), noFlyZones);
                int returnLegMoves;
                if (!finalReturnPath.isEmpty()) {
                    flightDeliveries.add(new Delivery(null, finalReturnPath));
                    returnLegMoves = (finalReturnPath.size() - 1);
                    currentFlightMoves += returnLegMoves;
                }

                double flightCost = drone.capability().costInitial() + drone.capability().costFinal() +
                        (currentFlightMoves * drone.capability().costPerMove());

                double costPerOrder = flightCost / packedOrders.size();
                List<MedDispatchRec> ordersToRemoveFromFlight = new ArrayList<>();

                for (MedDispatchRec order : packedOrders) {
                    if (order.requirements().maxCost() != null && costPerOrder > order.requirements().maxCost()) {
                        ordersToRemoveFromFlight.add(order);
                        failedOrderIds.add(order.id());
                    }
                }

                if (!ordersToRemoveFromFlight.isEmpty()) {
                    packedOrders.removeAll(ordersToRemoveFromFlight);
                    pendingOrders.removeAll(ordersToRemoveFromFlight);

                    continue;
                }


                totalCost += flightCost;
                totalMoves += currentFlightMoves;
                allDronePaths.add(new DronePath(droneId, flightDeliveries));

                pendingOrders.removeAll(packedOrders);
            }
        }

        return new FlightResponse(totalCost, totalMoves, allDronePaths);
    }

    public Map<String, Object> calculateDeliveryPathAsGeoJson(List<MedDispatchRec> orders) {
        FlightResponse response = calculateDeliveryPath(orders);

        // although order doesn't affect the geo json structure working, using a linked hashmap
        // to make sure the order is correct
        if (!response.dronePaths().isEmpty()) {
            List<Map<String, Object>> features = new ArrayList<>();

            for (DronePath dronePath : response.dronePaths()) {
                List<List<Double>> tripCoordinates = new ArrayList<>();
                for (Delivery delivery : dronePath.deliveries()) {
                    for (LngLat point : delivery.flightPath()) {
                        tripCoordinates.add(List.of(point.lng(), point.lat()));
                    }
                }

                Map<String, Object> geometry = new LinkedHashMap<>();
                geometry.put("type", "LineString");
                geometry.put("coordinates", tripCoordinates);

                Map<String, Object> feature = new LinkedHashMap<>();
                feature.put("type", "Feature");
                feature.put("properties", Map.of("name", "Drone Flight Path"));
                feature.put("geometry", geometry);

                features.add(feature);
            }

            Map<String, Object> featureCollection = new LinkedHashMap<>();
            featureCollection.put("type", "FeatureCollection");
            featureCollection.put("features", features);

            return featureCollection;
        }

        // fallback
        Map<String, Object> featureCollection = new LinkedHashMap<>();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", List.of());
        return featureCollection;
    }


    private List<MedDispatchRec> optimizeRoute(LngLat startLocation, List<MedDispatchRec> orders) {
        List<MedDispatchRec> remaining = new ArrayList<>(orders);
        List<MedDispatchRec> route = new ArrayList<>();
        LngLat current = startLocation;

        while (!remaining.isEmpty()) {
            MedDispatchRec nearest = null;
            double minDist = Double.MAX_VALUE;

            for (MedDispatchRec order : remaining) {
                double dist = calculationService.calculateDistance(current, order.delivery());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = order;
                }
            }

            if (nearest != null) {
                route.add(nearest);
                remaining.remove(nearest);
                current = nearest.delivery();
            } else {
                break; // shouldn't happen but IDE warned about not having null check
            }
        }
        return route;
    }
}