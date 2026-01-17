package ilp.samad.ilpcoursework1.data.drone;

public record Capability(
        boolean cooling,
        boolean heating,
        double capacity,
        int maxMoves,
        double costPerMove,
        double costInitial,
        double costFinal
) {}
