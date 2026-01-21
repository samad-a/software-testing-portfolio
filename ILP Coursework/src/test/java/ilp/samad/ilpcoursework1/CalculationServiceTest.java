package ilp.samad.ilpcoursework1;

import ilp.samad.ilpcoursework1.data.geometry.LngLat;
import ilp.samad.ilpcoursework1.data.geometry.Region;
import ilp.samad.ilpcoursework1.service.CalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculationServiceTest {

    private CalculationService calculationService;
    // used for floating point tolerance in assertEquals() when comparing doubles
    private static final double EPSILON = 1e-9;
    // used for region calculations
    // initialised in @BeforeEach method to reduce repetition and make sure object is fresh
    Region squareRegion;

    @BeforeEach
    void setUp() {
        calculationService = new CalculationService();
        squareRegion = new Region("square", List.of(
                    new LngLat(0.0, 0.0), new LngLat(2.0, 0.0),
                new LngLat(2.0, 2.0), new LngLat(0.0, 2.0),
                new LngLat(0.0, 0.0)
        ));
    }

    // START OF DISTANCE CALCULATION TESTS
    @Test
    @DisplayName("Check that distance is calculated correctly (Pythagorean Triple 3-4-5)")
    void testDistanceCalculation() {
        LngLat pos1 = new LngLat(0.0, 0.0);
        LngLat pos2 = new LngLat(3.0, 4.0);

        assertEquals(5.0, calculationService.calculateDistance(pos1, pos2));
    }

    @Test
    @DisplayName("Check that distance is calculated correctly (both points at the same position)")
    void testZeroDistanceCalculation() {
        LngLat pos1 = new LngLat(1.0, 1.0);
        LngLat pos2 = new LngLat(1.0, 1.0);

        assertEquals(0.0, calculationService.calculateDistance(pos1, pos2));
    }

    @Test
    @DisplayName("Check that distance is calculated correctly with negative co-ordinates (Pythagorean Triple 3-4-5)")
    void testNegativeDistanceCalculation() {
        LngLat pos1 = new LngLat(-2.0, -3.0);
        LngLat pos2 = new LngLat(1.0, 1.0);

        assertEquals(5.0, calculationService.calculateDistance(pos1, pos2));
    }
    // END OF DISTANCE CALCULATION TESTS


    // START OF CLOSENESS CALCULATION TESTS
    @Test
    @DisplayName("Check that closeness is true for points at same position")
    void testTrueClosenessCalculation() {
        LngLat pos1 = new LngLat(1.0, 1.0);
        LngLat pos2 = new LngLat(1.0, 1.0);

        assertTrue(calculationService.calculateClose(pos1, pos2));
    }

    @Test
    @DisplayName("Check that closeness is true for points near but within closeness threshold")
    void testThresholdClosenessCalculation() {
        LngLat pos1 = new LngLat(1.0, 1.0);
        LngLat pos2 = new LngLat(1.0, 1.0001);

        assertTrue(calculationService.calculateClose(pos1, pos2));
    }

    @Test
    @DisplayName("Check that closeness is false for far-away points")
    void testFalseClosenessCalculation() {
        LngLat pos1 = new LngLat(1.0, 1.0);
        LngLat pos2 = new LngLat(1.1, 1.1);

        assertFalse(calculationService.calculateClose(pos1, pos2));
    }

    @Test
    @DisplayName("Check that closeness is false for points on the 0.00015 boundary")
    void testThresholdBoundaryClosenessCalculation() {
        LngLat pos1 = new LngLat(1.0, 1.0);
        LngLat pos2 = new LngLat(1.0, 1.00015);

        assertFalse(calculationService.calculateClose(pos1, pos2));
    }
    // END OF CLOSENESS CALCULATION TESTS


    // START OF MOVEMENT CALCULATION TESTS
    @Test
    @DisplayName("Check that giving an incorrect angle for movement throws an exception")
    void testIncorrectIncrementMovementAngleCalculation() {
        LngLat start = new LngLat(0.0, 0.0);
        double angle = 10.0;

        assertThrows(IllegalArgumentException.class, () -> calculationService.calculateNextPosition(start, angle));
    }

    @Test
    @DisplayName("Check that giving an incorrect angle for movement throws an exception")
    void testIncorrectLargeMovementAngleCalculation() {
        LngLat start = new LngLat(0.0, 0.0);
        double angle = 420.0;

        assertThrows(IllegalArgumentException.class, () -> calculationService.calculateNextPosition(start, angle));
    }

    @Test
    @DisplayName("Check that giving an incorrect angle for movement throws an exception")
    void testIncorrectNegativeMovementAngleCalculation() {
        LngLat start = new LngLat(0.0, 0.0);
        double angle = -180.0;

        assertThrows(IllegalArgumentException.class, () -> calculationService.calculateNextPosition(start, angle));
    }

    @Test
    @DisplayName("Check that giving a correct angle for movement does not an exception")
    void testCorrectMovementAngleCalculation() {
        LngLat start = new LngLat(0.0, 0.0);
        double angle = 22.5;

        assertDoesNotThrow(() -> calculationService.calculateNextPosition(start, angle));
    }

    // Important note for below tests (tests were failing without adding this)
    // For the assertions with doubles, added a 3rd parameter for floating point tolerance
    @Test
    @DisplayName("0.0° - East")
    void testEastMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 0.0);
        assertEquals(0.00015, result.lng(), EPSILON);
        assertEquals(0.0, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("22.5° - East-North-East")
    void testENEMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 22.5);
        assertEquals(0.000138581929877, result.lng(), EPSILON);
        assertEquals(0.000057402514855, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("45.0° - North-East")
    void testNEMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 45.0);
        assertEquals(0.000106066017178, result.lng(), EPSILON);
        assertEquals(0.000106066017178, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("67.5° - North-North-East")
    void testNNEMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 67.5);
        assertEquals(0.000057402514855, result.lng(), EPSILON);
        assertEquals(0.000138581929877, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("90.0° - North")
    void testNorthMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 90.0);
        assertEquals(0.0, result.lng(), EPSILON);
        assertEquals(0.00015, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("112.5° - North-North-West")
    void testNNWMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 112.5);
        assertEquals(-0.000057402514855, result.lng(), EPSILON);
        assertEquals(0.000138581929877, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("135.0° - North-West")
    void testNWMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 135.0);
        assertEquals(-0.000106066017178, result.lng(), EPSILON);
        assertEquals(0.000106066017178, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("157.5° - West-North-West")
    void testWNWMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 157.5);
        assertEquals(-0.000138581929877, result.lng(), EPSILON);
        assertEquals(0.000057402514855, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("180.0° - West")
    void testWestMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 180.0);
        assertEquals(-0.00015, result.lng(), EPSILON);
        assertEquals(0.0, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("202.5° - West-South-West")
    void testWSWMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 202.5);
        assertEquals(-0.000138581929877, result.lng(), EPSILON);
        assertEquals(-0.000057402514855, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("225.0° - South-West")
    void testSWMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 225.0);
        assertEquals(-0.000106066017178, result.lng(), EPSILON);
        assertEquals(-0.000106066017178, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("247.5° - South-South-West")
    void testSSWMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 247.5);
        assertEquals(-0.000057402514855, result.lng(), EPSILON);
        assertEquals(-0.000138581929877, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("270.0° - South")
    void testSouthMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 270.0);
        assertEquals(0.0, result.lng(), EPSILON);
        assertEquals(-0.00015, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("292.5° - South-South-East")
    void testSSEMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 292.5);
        assertEquals(0.000057402514855, result.lng(), EPSILON);
        assertEquals(-0.000138581929877, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("315.0° - South-East")
    void testSEMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 315.0);
        assertEquals(0.000106066017178, result.lng(), EPSILON);
        assertEquals(-0.000106066017178, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("337.5° - East-South-East")
    void testESEMovement() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat result = calculationService.calculateNextPosition(start, 337.5);
        assertEquals(0.000138581929877, result.lng(), EPSILON);
        assertEquals(-0.000057402514855, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("Check that movement is calculated correctly when start is non-zero (Positive)")
    void testPositiveStartMovementCalculation() {
        LngLat start = new LngLat(1.0, 5.0);
        double angle = 45.0;
        LngLat result = calculationService.calculateNextPosition(start, angle);

        assertEquals(1.0001060660171, result.lng(), EPSILON);
        assertEquals(5.0001060660171, result.lat(), EPSILON);
    }

    @Test
    @DisplayName("Check that movement is calculated correctly when start is non-zero (Negative)")
    void testNegativeStartMovementCalculation() {
        LngLat start = new LngLat(-5.0, -1.0);
        double angle = 45.0;
        LngLat result = calculationService.calculateNextPosition(start, angle);

        assertEquals(-4.9998939339828, result.lng(), EPSILON);
        assertEquals(-0.9998939339828, result.lat(), EPSILON);
    }
    // END OF MOVEMENT CALCULATION TESTS


    // START OF IS IN REGION CALCULATION TESTS
    @Test
    @DisplayName("Check that an exception is thrown when given an open region")
    void testOpenRegionCalculation() {
        Region openRegion = new Region("open-square", List.of(
                new LngLat(0.0, 0.0), new LngLat(2.0, 0.0),
                new LngLat(2.0, 2.0), new LngLat(0.0, 2.0)
        ));
        LngLat position = new LngLat(1.0, 1.0);

        assertThrows(IllegalArgumentException.class, () -> calculationService.calculateIsInRegion(position, openRegion));
    }

    @Test
    @DisplayName("Check true is returned if point is inside region")
    void testPointInsideRegionCalculation() {
        LngLat position = new LngLat(1.0, 1.0);

        assertTrue(calculationService.calculateIsInRegion(position, squareRegion));
    }

    @Test
    @DisplayName("Check false is returned if point is outside region")
    void testPointOutsideRegionCalculation() {
        LngLat position = new LngLat(3.0, 1.0);

        assertFalse(calculationService.calculateIsInRegion(position, squareRegion));
    }

    @Test
    @DisplayName("Check true is returned if point is on edge of region")
    void testPointOnEdgeRegionCalculation() {
        LngLat position = new LngLat(0.0, 1.0);

        assertTrue(calculationService.calculateIsInRegion(position, squareRegion));
    }

    @Test
    @DisplayName("Check false is returned if point is just outside edge of region")
    void testPointOutsideEdgeRegionCalculation() {
        LngLat position = new LngLat(-0.0000001, 1.0);

        assertFalse(calculationService.calculateIsInRegion(position, squareRegion));
    }

    @Test
    @DisplayName("Check true is returned if point is on a vertex/corner of region")
    void testPointOnCornerRegionCalculation() {
        LngLat position = new LngLat(2.0, 2.0);

        assertTrue(calculationService.calculateIsInRegion(position, squareRegion));
    }
    // END OF IS IN REGION CALCULATION TESTS
}
