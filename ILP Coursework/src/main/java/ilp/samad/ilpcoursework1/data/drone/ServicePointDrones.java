package ilp.samad.ilpcoursework1.data.drone;

import java.util.List;

public record ServicePointDrones(
        Integer servicePointId,
        List<DroneAvailability> drones
) {}