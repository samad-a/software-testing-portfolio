package ilp.samad.ilpcoursework1.data.geometry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// adding this to ignore the 'alt' part of service point dto
@JsonIgnoreProperties(ignoreUnknown = true)
public record LngLat(
        // valid longitudes are between -180 and +180 as specified by Michael on Piazza
        @NotNull
        @Min(value = -180, message = "Longitude must be within +/-180")
        @Max(value = 180, message = "Longitude must be within +/-180")
        Double lng,
        // valid latitudes are always between -90 and +90 (whereas longitude is sometimes 0 to 360)
        @NotNull
        @Min(value = -90, message = "Latitude must be within +/-90")
        @Max(value = 90, message = "Latitude must be within +/-90")
        Double lat) {}
