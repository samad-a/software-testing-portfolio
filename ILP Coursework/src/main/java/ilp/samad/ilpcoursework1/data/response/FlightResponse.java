package ilp.samad.ilpcoursework1.data.response;

import java.util.List;

public record FlightResponse(
        Double totalCost,
        Integer totalMoves,
        List<DronePath> dronePaths
) {}

