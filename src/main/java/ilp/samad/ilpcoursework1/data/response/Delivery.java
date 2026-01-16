package ilp.samad.ilpcoursework1.data.response;

import ilp.samad.ilpcoursework1.data.geometry.LngLat;

import java.util.List;

public record Delivery(
        Integer deliveryId,
        List<LngLat> flightPath
) {}
