package ilp.samad.ilpcoursework1.data.drone;

import java.util.List;

public record DroneAvailability(
        String id,
        List<Schedule> availability
) {}