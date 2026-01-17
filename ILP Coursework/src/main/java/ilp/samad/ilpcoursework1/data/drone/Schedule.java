package ilp.samad.ilpcoursework1.data.drone;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record Schedule(
        DayOfWeek dayOfWeek,
        LocalTime from,
        LocalTime until
) {}
