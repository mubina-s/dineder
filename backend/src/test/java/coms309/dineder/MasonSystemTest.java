package coms309.dineder;

import coms309.dineder.dto.*;
import coms309.dineder.entity.AdminLog;
import coms309.dineder.entity.User;
import coms309.dineder.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class MasonSystemTest {
    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    // Static variables for consistency between tests
    private static Long adminUserId;
    private static Long normalUserId; // For checking "Sad Paths"
    private static Long restaurantId;
    private static Long sessionId;
    private static String joinCode;
    private static Long participantId;

    // Set up the port and base uri before each test
    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    // helper method for creating users
    private Long createUser(String name, boolean isAdmin){
        // String containing json request
        String json = "{\"username\":\"" + name + "\", \"password\":\"pass\", \"email\":\"" + name + "@test.com\", \"name\":\"" + name + "\"}";
        // Get id from response (newly created user)
        int id = given()
                .contentType(ContentType.JSON)
                .body(json)
                .post("/users/signup")
                .then()
                .statusCode(200)
                .extract().path("id");
        //conver to long
        Long userId = Long.valueOf(id);
        // Get the user associated with this id
        User user = userRepository.findById(userId).orElseThrow();
        // Change admin status to true (and save) if we are creating an admin
        if (isAdmin){
            user.setIsAdmin(true);
            userRepository.save(user);
        }

        // Check to make sure important attributes are not null
        assertNotNull(user.getUsername());
        assertNotNull(user.getEmail());
        assertEquals(name, user.getName());

        // Return the id of the user created
        return userId;
    }

    /**
     * Test creating 2 new users (simple test for setup)
     */
    @Test
    @Order(1)
    public void test1UserSetup(){
        adminUserId = createUser("AdminUser", true);
        normalUserId = createUser("NormalUser", false);
    }

    /**
     * Test creating and updating a restaurant
     */
    @Test
    @Order(2)
    public void test2RestaurantTesting(){
        // Create a restaurant using the CreateRestaurantRequest dto
        CreateRestaurantRequest req =  new CreateRestaurantRequest();
        req.setName("Five Guys");
        req.setAddress("123 Main  St");
        req.setRating(5);
        req.setPriceRange("$$");
        req.setPhone("1234567890");
        req.setWebsite("FiveGuys.com");

        // Make a post request and store the dto return
        RestaurantDTO created = given()
                .contentType(ContentType.JSON)
                .queryParam("adminId",adminUserId)
                .body(req)
                .when().post("/restaurants")
                .then().statusCode(200)
                .extract().as(RestaurantDTO.class);
        // Get the id
        restaurantId = created.getId();
        //Check fields to be sure it saved correctly
        assertEquals("Five Guys", created.getName());
        assertEquals("123 Main  St", created.getAddress());
        assertEquals(5, created.getRating());
        assertEquals("$$", created.getPriceRange());
        assertEquals("1234567890", created.getPhone());
        assertEquals("FiveGuys.com", created.getWebsite());
        // Now we update the fields using a map because of the different request type
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", "Updated Five Guys");
        updateMap.put("address", "456 Main  St");
        updateMap.put("rating", 4);
        updateMap.put("priceRange", "$");
        updateMap.put("phone", "0987654321");
        updateMap.put("website", "FiveGuys.com New");
        // Pass in the update and check
        given()
                .contentType(ContentType.JSON)
                .queryParam("adminId",adminUserId)
                .body(updateMap)
                .when().put("/restaurants/" + restaurantId)
                .then().statusCode(200)
                .body("name", equalTo("Updated Five Guys"))
                .body("address", equalTo("456 Main  St"))
                .body("priceRange", equalTo("$"))
                .body("rating", equalTo(4))
                .body("phone", equalTo("0987654321"))
                .body("website", equalTo("FiveGuys.com New"));
        // Also confirm that get get all restaurants works
        given().get("/restaurants").then().statusCode(200).body("size()", greaterThan(0));
    }

    /**
     * Testing that the admin logging is performing correctly
     */
    @Test
    @Order(3)
    public void test3AdminLogs(){
        // Do a get request
        // Check for creation logs, updating logs, and that the admin id is correct
        given().get("/admin/logs").then()
                .assertThat().statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].actionType", equalTo("CREATE_RESTAURANT"))
                .body("[1].actionType", equalTo("UPDATE_RESTAURANT"))
                .body("[0].adminId", equalTo(adminUserId.intValue()))
                .body("[1].adminId", equalTo(adminUserId.intValue()));


    }



    /**
     * Test for a host creating a session
     */
    @Test
    @Order(4)
    public void test4CreateSession(){
        // First create a session preference
        Map<String, Object> prefMap = new HashMap<>();
        prefMap.put("preferenceName", "LateNight");
        prefMap.put("rating", "1");
        prefMap.put("priceRange", "$");
        prefMap.put("hostId", adminUserId);

        //Post it and store the id
        int prefId = given()
                .contentType(ContentType.JSON)
                .body(prefMap)
                .post("/sessionprefs")
                .then().statusCode(200).extract().path("id");

        // Create a session with the dto
        CreateSessionRequest req = new CreateSessionRequest();
        req.setHostId(adminUserId);
        req.setPreferenceId((long) prefId);

        // Create a session with post and store the response as a dto
        SessionDTO session = given()
                .contentType(ContentType.JSON)
                .body(req)
                .when().post("/sessions")
                .then().statusCode(200)
                .extract().as(SessionDTO.class);

        // Store the session id and its join code
        sessionId = session.getId();
        assertNotNull(session.getJoinCode());
        joinCode = session.getJoinCode();
        // Check that hosts id matches
        assertEquals(adminUserId, session.getHostId());
        // Checking functionality of other endpoints
        // Get by join code
        given().get("/sessions/" + joinCode).then().statusCode(200).body("joinCode", equalTo(joinCode));
        // Get all sessions
        given().get("/sessions").then().statusCode(200).body("size()", greaterThan(0));
        // Specifics
        // Check that there is 1 restaurant and its Updated Five Guys
        assertEquals(1, session.getRestaurantDTOList().size());
        assertEquals("Updated Five Guys", session.getRestaurantDTOList().get(0).getName());
        // Session should be live
        assertTrue(session.isLive());
        // Swiping complete should be false
        assertFalse(session.isSwipingComplete());
        // Check the participants
        // 1 participant
        assertEquals(1, session.getParticipantsDTOList().size());
        // Check the name
        assertEquals("AdminUser", session.getParticipantsDTOList().get(0).getName());
        // Check ready status and finished swiping status
        assertFalse(session.getParticipantsDTOList().get(0).isReady());
        assertFalse(session.getParticipantsDTOList().get(0).isFinishedSwiping());


    }

    @Test
    @Order(5)
    public void test5JoinSession(){
        // join session request of another user (not host)
        JoinSessionRequest req = new JoinSessionRequest();
        req.setUserId(normalUserId);
        req.setJoinCode(joinCode);

        // Post to join the session, store the participant dto from response
        SessionParticipantDTO part = given()
                .contentType(ContentType.JSON)
                .body(req)
                .when().post("/sessions/join")
                .then().statusCode(200)
                .extract().as(SessionParticipantDTO.class);

        // Store the id
        participantId = part.getParticipantId();

        // Checking ready endpoints
        // Set Ready (True)
        given().put("/participants/" + participantId + "/ready").then().statusCode(200).body("ready", equalTo(true));

        // Set Unready (False)
        given().put("/participants/" + participantId + "/unready").then().statusCode(200).body("ready", equalTo(false));

        // Set Ready Again
        given().put("/participants/" + participantId + "/ready").then().statusCode(200).body("ready", equalTo(true));
    }

    /**
     * Testing various elements of the lobby websocket
     * This was a bit difficult to figure out, and isnt as good as my tests using http but was the best I could do for websocket
     */
    @Test
    @Order(6)
    public void test6LobbyWebsocket() throws Exception {
        // Add a "Blocker" Participant (someone who prevents a session from starting
        String blockerJson = "{\"username\":\"Blocker\", \"password\":\"123\", \"email\":\"block@test.com\", \"name\":\"Blocker\"}";
        // The blcokers id (use a get request for signup and store the id)
        int blockerId = given().contentType(ContentType.JSON).body(blockerJson).post("/users/signup").then().statusCode(200).extract().path("id");
        // Alternative to using a join session request
        String joinJson = "{\"userId\":" + blockerId + ", \"joinCode\":\"" + joinCode + "\"}";
        // Join the session
        given().contentType(ContentType.JSON).body(joinJson).post("/sessions/join").then().statusCode(200);

        // Get Host Participant ID
        List<Map<String, Object>> parts = given().get("/sessions/id/" + sessionId).path("participantsDTOList");
        Integer hostPartId = null;
        for (Map<String, Object> p : parts) {
            if (Long.valueOf((Integer)p.get("userId")).equals(adminUserId)) {
                hostPartId = (Integer) p.get("participantId");
                break;
            }
        }
        if (hostPartId == null) throw new RuntimeException("Host not found");

        // Connect to the websocket (this is the best I could do since restassured doenst work for websockets)
        WebSocketClient client = new StandardWebSocketClient();
        TextWebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            public void handleTextMessage(WebSocketSession s, TextMessage m) { }
        };
        String url = "ws://localhost:" + port + "/ws/lobby/" + sessionId + "/" + hostPartId;
        WebSocketSession session = client.doHandshake(handler, url).get(1, TimeUnit.SECONDS);

        // Send Messages
        session.sendMessage(new TextMessage("{\"action\": \"SET_READY\", \"isReady\": true}"));
        Thread.sleep(500);
        session.sendMessage(new TextMessage("{\"action\": \"FORCE_START\", \"hostId\": " + adminUserId + "}"));
        Thread.sleep(500);

        if (session.isOpen()) session.close();
    }

    /**
     * Testing other misc services including cuisine controllers, image controllers, and finishing swiping (not really connected to the rest of the tests)
     */
    @Test
    @Order(7)
    public void test7OtherServices() {
        // Cuisine testing
        String cJson = "{\"name\": \"TestCuisine\"}";
        // Create a cuisine and store the id
        int cId = given().contentType(ContentType.JSON).body(cJson).post("/cuisines").then().statusCode(200).extract().path("id");
        // Confirm there is only 1 cuisine and that the name is correct
        given()
                .get("/cuisines")
                        .then().assertThat().statusCode(200)
                        .body("size()", equalTo(1)).body("[0].name", equalTo("TestCuisine"));
        //Delete
        given().delete("/cuisines/" + cId).then().statusCode(200);

        //Image testing
        String iJson = "{\"url\": \"http://img.com/1.jpg\"}";
        // Create the image and store the id
        int iId = given().contentType(ContentType.JSON).body(iJson).post("/images").then().statusCode(200).extract().path("id");
        //Delete
        given().delete("/images/" + iId).then().statusCode(200);

        // Finish Swiping Endpoint (used later on after voting is done (round 1))
        given()
                .post("/participants/"+participantId+"/finish-swiping")
                .then().statusCode(200);
        //TODO: check if participant is finished swiping
    }

    @Test
    @Order(8)
    public void test8MiscErrors(){
        // Error 403, trying to delete a restaurant with a normal users id instead of admin
        given().queryParam("adminId", normalUserId)
                .delete("/restaurants/" + restaurantId)
                .then().statusCode(403);
        // Error 404 (not found), searching for a restaurant with fake id
        given().get("/restaurants/99999").then().statusCode(404);
        // Error 400 (bad request), posting a restaurant without admin id
        given().contentType(ContentType.JSON).body("{}").post("/restaurants").then().statusCode(400);
    }

    /**
     * Simple test to make sure the dashboard service methods work
     * NOTE: I was not responsible for voting, but the dashboard relies upon votes for
     * its data. This test is mostly trivial, but checking to make sure it doesnt
     * give errors was better than not testing it at all.
     */
    @Test
    @Order(9)
    public void test9DashboardCheck() {
        // Use a get request to get the dashboard
        DashboardDTO dash = given()
                .when().get("/dashboard")
                .then().statusCode(200)
                .extract().as(DashboardDTO.class);

        assertNotNull(dash.getLeaderboard());
        assertTrue(dash.getTotalDecisions() >= 0);

        if (!dash.getLeaderboard().isEmpty()) {
            DashboardDTO.LeaderboardEntry entry = dash.getLeaderboard().get(0);
            assertNotNull(entry.getName());
            assertTrue(entry.getScore() >= 0);
        }
    }

    /**
     * Cleanup & kicking participants
     */
    @Test
    @Order(10)
    public void test10Cleanup() {
        // Kick User
        // Note: adminUserId is the host
        given().delete("/participants/" + participantId + "/" + adminUserId).then().statusCode(200);

        // Delete Restaurant
        given().queryParam("adminId", adminUserId)
                .delete("/restaurants/" + restaurantId)
                .then().statusCode(200);

        // Deleting users
        //Test with a fake id first
        int fakeId = 999;
        String returnVal = given().delete("/users/"+fakeId).then().statusCode(200).extract().asString();
        assertTrue(returnVal.isEmpty());
        // Test with a real user's id
        int realId = 1; //admin
        returnVal = given().delete("/users/"+adminUserId).then().extract().asString();
        assertEquals("AdminUser", returnVal);
        realId = 2; //other user
        returnVal = given().delete("/users/"+normalUserId).then().extract().asString();
        assertEquals("NormalUser", returnVal);
    }





}
