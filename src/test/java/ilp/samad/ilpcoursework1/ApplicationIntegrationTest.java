package ilp.samad.ilpcoursework1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;


    // - /actuator/health
    @Test
    @DisplayName("GET /actuator/health should return 200 OK and status: UP")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }


    // - the rest are all under /api/v1
    // - /uid
    @Test
    @DisplayName("GET /api/v1/uid should return 200 OK and the correct student ID")
    void testUidEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2544386"));
    }



    // - /distanceTo
    @Test
    @DisplayName("POST /api/v1/distanceTo (with correct body) should return 200 OK and the correct distance")
    void testCorrectDistanceTo() throws Exception {
        String validJson = """
        {
            "position1": {"lng": 0.0, "lat": 0.0},
            "position2": {"lng": 3.0, "lat": 4.0}
        }
        """;
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string("5.0"));
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo (with a missing field) should return 400 Bad Request")
    void testMissingFieldDistanceTo() throws Exception {
        String invalidJson = """
        {
            "position1": {"lng": 0.0, "lat": 0.0}
        }
        """; // position2 is missing
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/distanceTo (with incorrect JSON) should return 400 Bad Request")
    void testIncorrectJsonDistanceTo() throws Exception {
        String malformedJson = """
        {
            "position1": {"lng": 0.0, "lat": 0.0},
            "position2": {"lng": 3.0, "lat": 4.0}
        """;
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }



    // - /isCloseTo
    @Test
    @DisplayName("POST /api/v1/isCloseTo (with correct body) should return true when close")
    void testTrueIsCloseTo() throws Exception {
        String validJson = """
        {
            "position1": {"lng": 0.0, "lat": 0.0},
            "position2": {"lng": 0.0, "lat": 0.0001}
        }
        """;
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo (with correct body) should return false when far")
    void testFalseIsCloseTo() throws Exception {
        String validJson = """
        {
            "position1": {"lng": 0.0, "lat": 0.0},
            "position2": {"lng": 0.0, "lat": 0.002}
        }
        """;
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo (with correct body) should return false when on the 0.00015 distance boundary")
    void testFalseBoundaryIsCloseTo() throws Exception {
        String validJson = """
        {
            "position1": {"lng": 0.0, "lat": 0.0},
            "position2": {"lng": 0.0, "lat": 0.00015}
        }
        """;
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo (with a missing field) should return 400 Bad Request")
    void testMissingFieldIsCloseTo() throws Exception {
        String invalidJson = """
        {
            "position1": {"lng": 0.0, "lat": 0.0}
        }
        """;
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/isCloseTo (with invalid JSON) should return 400 Bad Request")
    void testInvalidJsonIsCloseTo() throws Exception {
        String malformedJson = """
        {
            "position1": {"lng": 0.0, "lat": 0.0},
            "position2": {"lng": 3.0, "lat": 4.0}
        """;
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }



    // - /nextPosition
    @Test
    @DisplayName("POST /api/v1/nextPosition (with correct body) should return 200 OK and the correct new position")
    void testCorrectNextPosition() throws Exception {
        String validJson = """
        {
            "start": {"lng": -5.0, "lat": 1.0},
            "angle": 45.0
        }
        """;
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").value(-4.999893933982822))
                .andExpect(jsonPath("$.lat").value(1.0001060660171779));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition (with incorrect angle) should return 400 Bad Request")
    void testInvalidAngleNextPosition() throws Exception {
        String invalidJson = """
        {
            "start": {"lng": 0.0, "lat": 0.0},
            "angle": 50.0
        }
        """;
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition (with invalid JSON) should return 400 Bad Request")
    void testInvalidJsonNextPosition() throws Exception {
        String malformedJson = """
        {
            "start": {"lng": 0.0, "lat": 0.0}
            "angle": 50.0
        }
        """;
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/nextPosition (with missing field) should return 400 Bad Request")
    void testMissingFieldNextPosition() throws Exception {
        String invalidJson = """
        {
            "start": {"lng": 0.0},
            "angle": 50.0
        }
        """;
        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }



    // - /isInRegion
    @Test
    @DisplayName("POST /api/v1/isInRegion (with correct body) should return true for point inside")
    void testTrueIsInRegion() throws Exception {
        String validJson = """
        {
            "position": {"lng": 1.0, "lat": 1.0},
            "region": {
                "name": "square",
                "vertices": [
                    {"lng": 0.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 0.0}
                ]
            }
        }
        """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion (with correct body) should return true for point on edge")
    void testEdgeTrueIsInRegion() throws Exception {
        String validJson = """
        {
            "position": {"lng": 1.0, "lat": 0.0},
            "region": {
                "name": "square",
                "vertices": [
                    {"lng": 0.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 0.0}
                ]
            }
        }
        """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion (with correct body) should return true for point on corner")
    void testCornerTrueIsInRegion() throws Exception {
        String validJson = """
        {
            "position": {"lng": 0.0, "lat": 0.0},
            "region": {
                "name": "square",
                "vertices": [
                    {"lng": 0.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 0.0}
                ]
            }
        }
        """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }



    @Test
    @DisplayName("POST /api/v1/isInRegion (with correct body) should return false for point outside")
    void testFalseIsInRegion() throws Exception {
        String validJson = """
        {
            "position": {"lng": 3.0, "lat": 3.0},
            "region": {
                "name": "square",
                "vertices": [
                    {"lng": 0.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 0.0}
                ]
            }
        }
        """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion (with open region) should return 400 Bad Request")
    void testOpenRegionIsInRegion() throws Exception {
        String invalidJson = """
        {
            "position": {"lng": 1.0, "lat": 1.0},
            "region": {
                "name": "open-square",
                "vertices": [
                    {"lng": 0.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 2.0}
                ]
            }
        }
        """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion (with invalid JSON) should return 400 Bad Request")
    void testInvalidJSONIsInRegion() throws Exception {
        String invalidJson = """
        {
            "position": {"lng": 1.0, "lat": 1.0},
            "region": {
                "name": "open-square",
                "vertices": [
                    {"lng": 0.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 2.0
                ]
            }
        }
        """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/isInRegion (with missing field) should return 400 Bad Request")
    void testMissingFieldIsInRegion() throws Exception {
        String invalidJson = """
        {
            "position": {"lng": 1.0},
            "region": {
                "name": "open-square",
                "vertices": [
                    {"lng": 0.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 0.0},
                    {"lng": 2.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 2.0},
                    {"lng": 0.0, "lat": 0.0}
                ]
            }
        }
        """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }
}
