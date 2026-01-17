package ilp.samad.ilpcoursework1.data.response;

import java.util.List;

public record DronePath(
        String droneId,
        List<Delivery> deliveries
) {}
