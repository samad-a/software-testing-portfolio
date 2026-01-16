package ilp.samad.ilpcoursework1.data.request;

import jakarta.validation.constraints.NotNull;

public record Query(
        @NotNull String attribute,
        @NotNull String operator,
        @NotNull String value
) {}
