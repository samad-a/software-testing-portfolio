package ilp.samad.ilpcoursework1.service;

import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import ilp.samad.ilpcoursework1.data.geometry.Region;
import org.springframework.stereotype.Service;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.List;

@Service
public class CalculationService {

    private static final double MOVE_DISTANCE = 0.00015;
    private static final double VALID_ANGLE_INCREMENT = 22.5;
    // tolerance for floating-point comparisons
    private static final double EPSILON = 1e-9;

    public double calculateDistance(LngLat position1, LngLat position2){
        double lngDiff = Math.pow(position1.lng() - position2.lng(), 2);
        double latDiff = Math.pow(position1.lat() - position2.lat(), 2);
        return Math.sqrt(lngDiff + latDiff);
    }

    public boolean calculateClose(LngLat position1, LngLat position2){
        return calculateDistance(position1, position2) < MOVE_DISTANCE;
    }

    public LngLat calculateNextPosition(LngLat start, double angle){

        if (angle < 0 || angle > 360) {
            throw new IllegalArgumentException("Invalid angle, must be between 0 and 360 degrees.");
        }

        double remainder = Math.abs(angle % VALID_ANGLE_INCREMENT);

        // if it is not one of the 16 allowed directions, exception handler will return a 400 error
        if (remainder > EPSILON && Math.abs(remainder - VALID_ANGLE_INCREMENT) > EPSILON) {
            throw new IllegalArgumentException("Angle must be a multiple of 22.5 degrees.");
        }

        // converting as Math.cos/sin use radians, not degrees
        double angleInRadians = Math.toRadians(angle);

        // cosine = east/west
        double newLng = start.lng() + (MOVE_DISTANCE * Math.cos(angleInRadians));
        // sine = north/south
        double newLat = start.lat() + (MOVE_DISTANCE * Math.sin(angleInRadians));

        return new LngLat(newLng, newLat);
    }

    public boolean calculateIsInRegion(LngLat position, Region region){
        List<LngLat> vertices = region.vertices();

        LngLat firstVertex = vertices.getFirst();
        LngLat lastVertex = vertices.getLast();

        // if the polygon does not close at the end, exception handler will return 400
        if (!firstVertex.equals(lastVertex)) {
            throw new IllegalArgumentException("Region is not closed. (last vertex must be same as first vertex)");
        }

        Path2D.Double polygon = new Path2D.Double();
        polygon.moveTo(firstVertex.lng(), firstVertex.lat());

        for(int i = 1; i < vertices.size(); i++){
            LngLat vertex = vertices.get(i);
            polygon.lineTo(vertex.lng(), vertex.lat());
        }

        if (polygon.contains(position.lng(), position.lat())) {
            return true;
        }

        // polygon.contains is inconsistent with points on the boundary (e.g. directly on a vertex)
        // so needed to add additional checks
        for (int i = 0; i < vertices.size() - 1; i++) {
            LngLat p1 = vertices.get(i);
            LngLat p2 = vertices.get(i + 1);
            Line2D.Double edge = new Line2D.Double(p1.lng(), p1.lat(), p2.lng(), p2.lat());

            // using epsilon squared because distance is being squared too so need stricter boundary
            if (edge.ptSegDistSq(position.lng(), position.lat()) < EPSILON*EPSILON) {
                return true;
            }
        }

        return false;
    }
}
