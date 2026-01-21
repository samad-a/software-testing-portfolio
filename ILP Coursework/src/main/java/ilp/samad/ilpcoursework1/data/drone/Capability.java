package ilp.samad.ilpcoursework1.data.drone;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Capability(
        boolean cooling,
        boolean heating,
        double capacity,
        int maxMoves,
        double costPerMove,
        double costInitial,
        double costFinal
) {}
