package ilp.samad.ilpcoursework1.data.drone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.DayOfWeek;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Schedule(
        DayOfWeek dayOfWeek,
        LocalTime from,
        LocalTime until
) {}
