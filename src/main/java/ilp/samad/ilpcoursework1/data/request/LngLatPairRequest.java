package ilp.samad.ilpcoursework1.data.request;

import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LngLatPairRequest(
        @Valid @NotNull LngLat position1,
        @Valid @NotNull LngLat position2) {}
