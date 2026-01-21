package ilp.samad.ilpcoursework1.data.geometry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Limits(
        Double lower,
        Double upper
) {}
