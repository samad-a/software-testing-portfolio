package ilp.samad.ilpcoursework1.service;

import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import ilp.samad.ilpcoursework1.data.geometry.RestrictedArea;
import ilp.samad.ilpcoursework1.data.geometry.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.geom.Line2D;
import java.util.*;

@Service
public class PathService {
    private static final Logger logger = LoggerFactory.getLogger(PathService.class);

    private static final double MOVE_DISTANCE = 0.00015;
    private final CalculationService calculationService;

    public PathService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    public List<LngLat> findPath(LngLat start, LngLat end, List<RestrictedArea> noFlyZones) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, Double> gScore = new HashMap<>();

        Node startNode = new Node(start, 0.0, heuristic(start, end), null);
        openSet.add(startNode);
        gScore.put(key(start), 0.0);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            logger.info("A* currently exploring -> G-score: {}, F-score: {} at [{}, {}]",
                    current.gScore,
                    current.fScore,
                    current.position.lng(),
                    current.position.lat());

            if (calculationService.calculateClose(current.position, end)) {
                return reconstructPath(current);
            }

            for (double angle = 0; angle < 360; angle += 22.5) {
                LngLat neighborPos = calculationService.calculateNextPosition(current.position, angle);
                String neighborKey = key(neighborPos);

                if (isMoveInvalid(current.position, neighborPos, noFlyZones)) {
                    continue;
                }

                double tentativeGScore = gScore.getOrDefault(key(current.position), Double.MAX_VALUE) + MOVE_DISTANCE;

                if (tentativeGScore < gScore.getOrDefault(neighborKey, Double.MAX_VALUE)) {
                    Node neighbor = new Node(
                            neighborPos,
                            tentativeGScore,
                            tentativeGScore + heuristic(neighborPos, end),
                            current // Parent is stored directly in the Node
                    );

                    gScore.put(neighborKey, tentativeGScore);
                    openSet.add(neighbor);
                }
            }
        }

        return List.of();
    }

    public double heuristic(LngLat a, LngLat b) {
        return calculationService.calculateDistance(a, b);
    }

    private boolean isMoveInvalid(LngLat start, LngLat end, List<RestrictedArea> noFlyZones) {
        for (RestrictedArea zone : noFlyZones) {
            Region tempRegion = new Region(zone.name(), zone.vertices());
            if (calculationService.calculateIsInRegion(end, tempRegion)) {
                return true;
            }

            if (doesLineIntersectPolygon(start, end, zone.vertices())) {
                return true;
            }
        }
        return false;
    }

    // avoids corner cutting
    public boolean doesLineIntersectPolygon(LngLat start, LngLat end, List<LngLat> vertices) {
        for (int i = 0; i < vertices.size(); i++) {
            LngLat p1 = vertices.get(i);
            LngLat p2 = vertices.get((i + 1) % vertices.size());

            if (Line2D.linesIntersect(
                    start.lng(), start.lat(),
                    end.lng(), end.lat(),
                    p1.lng(), p1.lat(),
                    p2.lng(), p2.lat())) {
                return true;
            }
        }
        return false;
    }

    public List<LngLat> reconstructPath(Node current) {
        List<LngLat> path = new ArrayList<>();
        while (current != null) {
            path.addFirst(current.position);
            current = current.parent;
        }
        return path;
    }

    private String key(LngLat pos) {
        return pos.lng() + "," + pos.lat();
    }

    public static class Node {
        LngLat position;
        double gScore;
        double fScore;
        Node parent;

        public Node(LngLat position, double gScore, double fScore, Node parent) {
            this.position = position;
            this.gScore = gScore;
            this.fScore = fScore;
            this.parent = parent;
        }
    }
}
