package ilp.samad.ilpcoursework1.data.geometry;

import java.util.List;

public record RestrictedArea(
        String name,
        Integer id,
        Limits limits,
        List<LngLat> vertices
) {}
