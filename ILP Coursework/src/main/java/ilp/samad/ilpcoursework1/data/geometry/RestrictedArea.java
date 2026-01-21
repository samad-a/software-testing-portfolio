package ilp.samad.ilpcoursework1.data.geometry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RestrictedArea(
        String name,
        Integer id,
        Limits limits,
        List<LngLat> vertices
) {}
