package ilp.samad.ilpcoursework1.data.drone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ilp.samad.ilpcoursework1.data.geometry.LngLat;

// not sure if ignore properties is needed here (the alt property should be filtered out by
// the ignore properties in LngLat, but thought this is safer)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ServicePoint(
        Integer id,
        String name,
        LngLat location
) {}
