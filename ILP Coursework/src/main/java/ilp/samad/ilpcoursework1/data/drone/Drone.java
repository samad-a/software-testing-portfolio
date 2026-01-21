package ilp.samad.ilpcoursework1.data.drone;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Drone(
        String id,
        String name,
        Capability capability
) {}
