package ilp.samad.ilpcoursework1.data.request;

import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record NextPositionRequest (
        @Valid @NotNull LngLat start,
        @NotNull Double angle) {}
