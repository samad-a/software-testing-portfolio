package ilp.samad.ilpcoursework1.data.geometry;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// minimum 4 vertices as polygon has min 3 vertices, and last vertex needs to close the polygon to be valid
public record Region(
        @NotNull String name,
        @Valid @NotNull @Size(min = 4) List<LngLat> vertices) {}
