package ilp.samad.ilpcoursework1.data.request;

import jakarta.validation.constraints.NotNull;

public record Requirements(
        @NotNull Double capacity,

        // optional reqs, null if missing
        Boolean cooling,
        Boolean heating,
        Double maxCost
) {}
