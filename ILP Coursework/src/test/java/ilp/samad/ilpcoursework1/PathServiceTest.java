package ilp.samad.ilpcoursework1;

import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import ilp.samad.ilpcoursework1.service.CalculationService;
import ilp.samad.ilpcoursework1.service.PathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PathServiceTest {
    @Mock
    private CalculationService calculationService;

    @InjectMocks
    private PathService pathService;

    List<LngLat> vertices;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vertices = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(0.0002, 0.0),
                new LngLat(0.0002, 0.0002),
                new LngLat(0.0, 0.0002),
                new LngLat(0.0, 0.0)
        );
    }


    // Unit Tests for doesLineIntersectPolygon()
    @Test
    @DisplayName("doesLineIntersectPolygon - Standard: Line intersects an edge")
    void testLineIntersectsEdge() {
        LngLat start = new LngLat(-0.0001, 0.0001);
        LngLat end = new LngLat(0.0001, 0.0001);

        assertTrue(pathService.doesLineIntersectPolygon(start, end, vertices),
                "The move should be flagged as intersecting the restricted area boundary.");
    }

    @Test
    @DisplayName("doesLineIntersectPolygon - Standard: Line is completely clear")
    void testLineIsClear() {
        LngLat start = new LngLat(-1.0, -1.0);
        LngLat end = new LngLat(-0.9, -0.9);

        assertFalse(pathService.doesLineIntersectPolygon(start, end, vertices),
                "A move outside of the restricted area should not trigger a collision.");
    }

    @Test
    @DisplayName("doesLineIntersectPolygon - Boundary: Line touches a vertex (Corner Cutting)")
    void testLineTouchesVertex() {
        LngLat start = new LngLat(-0.0001, -0.0001);
        LngLat end = new LngLat(0.0001, 0.0001);

        assertTrue(pathService.doesLineIntersectPolygon(start, end, vertices),
                "The move should be rejected to prevent corner cutting through a vertex.");
    }


    // 2. Unit Tests for reconstructPath()
    @Test
    @DisplayName("reconstructPath - Standard: Multiple steps in path")
    void testPathReconstruction() {
        LngLat p1 = new LngLat(0.0, 0.0);
        LngLat p2 = new LngLat(0.00015, 0.0);
        LngLat p3 = new LngLat(0.00030, 0.0);

        PathService.Node node1 = new PathService.Node(p1, 0.0, 0.0, null);
        PathService.Node node2 = new PathService.Node(p2, 0.00015, 0.0, node1);
        PathService.Node node3 = new PathService.Node(p3, 0.00030, 0.0, node2);

        List<LngLat> path = pathService.reconstructPath(node3);

        assertEquals(3, path.size(), "The reconstructed path should contain exactly 3 steps.");
        assertEquals(p1, path.get(0), "Path should start at the correct origin.");
        assertEquals(p3, path.get(2), "Path should end at the final node position.");
    }

    @Test
    @DisplayName("reconstructPath - Boundary: Path with only one point")
    void testSinglePointPath() {
        LngLat p1 = new LngLat(0.0, 0.0);
        PathService.Node node1 = new PathService.Node(p1, 0.0, 0.0, null);

        List<LngLat> path = pathService.reconstructPath(node1);

        assertEquals(1, path.size());
        assertEquals(p1, path.getFirst());
    }


    // Unit Tests for heuristic()
    @Test
    @DisplayName("heuristic - Standard: Delegates to calculation service")
    void testHeuristicDelegation() {
        LngLat a = new LngLat(55.944, -3.186);
        LngLat b = new LngLat(55.945, -3.187);

        when(calculationService.calculateDistance(a, b)).thenReturn(0.0014);

        double result = pathService.heuristic(a, b);

        assertEquals(0.0014, result, 1e-6);
        verify(calculationService, times(1)).calculateDistance(a, b);
    }
}
