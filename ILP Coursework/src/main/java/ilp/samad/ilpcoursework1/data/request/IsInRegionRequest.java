package ilp.samad.ilpcoursework1.data.request;

import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import ilp.samad.ilpcoursework1.data.geometry.Region;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record IsInRegionRequest(
        @Valid @NotNull LngLat position,
        @Valid @NotNull Region region
){}
