package ilp.samad.ilpcoursework1.data.request;

import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record MedDispatchRec(
        @NotNull Integer id,  // id as a String based on Michael's advice

        // Jackson will automatically parse strings to LocalDate/LocalTime
        LocalDate date,
        LocalTime time,

        @Valid @NotNull Requirements requirements,

        @Valid @NotNull LngLat delivery
) {}
