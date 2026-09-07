package coms309.dineder.service;

import coms309.dineder.repository.*;
import coms309.dineder.entity.*;
import coms309.dineder.dto.*;
import coms309.dineder.websocket.LobbyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

/**
 * Service class for service endpoints and logic.
 * Takes the complexity from the controllers and puts it here
 *
 * @author Mason Gliege
 */
@Service
public class SessionService {
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionPreferenceRepository sessionPreferenceRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private SessionParticipantRepository sessionParticipantRepository;
    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    @Lazy
    private LobbyWebSocketHandler lobbyWebSocketHandler;

    /**
     * Gets a session by its id and returns a dto
     * @param id id to find by
     * @return session dto
     */
    @Transactional
    public SessionDTO getSessionByIdDTO(Long id) {
        // Find session
        Session session = sessionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return convertSessionToDTO(session);
    }

    /**
     * Gets a list of all sessions as dtos
     * @return list of session dtos
     */
    @Transactional
    public List<SessionDTO> getAllSessionsDTO() {
        List<Session> sessions = sessionRepository.findAll();
        List<SessionDTO> sessionDTOs = new ArrayList<>();
        for (Session session : sessions) {
            SessionDTO sessionDTO = convertSessionToDTO(session);
            sessionDTOs.add(sessionDTO);
        }
        return sessionDTOs;
    }

    /**
     * private helper method for converting a session to dto
     * @param session session to convert
     * @return converted session dto
     */
    private SessionDTO convertSessionToDTO(Session session) {
        // Create sessionDTO
        SessionDTO sessionDTO = new SessionDTO();
        // Assign values from real object in database
        sessionDTO.setId(session.getId());
        sessionDTO.setSessionName(session.getName());
        sessionDTO.setJoinCode(session.getJoinCode());
        sessionDTO.setLive(session.getIsLive());
        sessionDTO.setSwipingComplete(session.isSwipingComplete());
        sessionDTO.setStartTime(session.getStartTime());
        // NOTE: this is name, not username
        sessionDTO.setHostname(session.getHost().getName());
        sessionDTO.setHostId(session.getHost().getId());

        // Make new set
        List<SessionParticipantDTO> sessionParticipantDTOS = new ArrayList<>();
        // get participants
        List<SessionParticipant> sortedParticipants = new ArrayList<>(session.getParticipants());
        // sort
        sortedParticipants.sort(Comparator.comparing(SessionParticipant::getId));
        // Iterate over each participant in session
        for (SessionParticipant participant : sortedParticipants) {
            // Create DTO
            SessionParticipantDTO pDTO = new SessionParticipantDTO();
            pDTO.setParticipantId(participant.getId());
            pDTO.setUserId(participant.getUser().getId());
            pDTO.setName(participant.getUser().getName());
            pDTO.setReady(participant.isReady());
            pDTO.setFinishedSwiping(participant.isFinishedSwiping());

            sessionParticipantDTOS.add(pDTO);
        }
        // Assign this set to this dto
        sessionDTO.setParticipantsDTOList(sessionParticipantDTOS);

        // Make new arraylist
        List<RestaurantDTO> restaurantDTOs = new ArrayList<>();
        // Convert set to arraylist
        List<Restaurant> sortedRestaurants = new ArrayList<>(session.getRestaurants());

        // Sort the list by restaurant id so each user sees the same order
        sortedRestaurants.sort(Comparator.comparing(Restaurant::getId));
        // Iterate over each restaurant in session
        for (Restaurant restaurant : sortedRestaurants) {
            // Create DTO
            RestaurantDTO rDTO = new RestaurantDTO();
            rDTO.setId(restaurant.getId());
            rDTO.setName(restaurant.getName());
            rDTO.setAddress(restaurant.getAddress());
            rDTO.setRating(restaurant.getRating());
            rDTO.setPriceRange(restaurant.getPriceRange());
            rDTO.setPhone(restaurant.getPhone());
            rDTO.setWebsite(restaurant.getWebsite());

            // add cuisines
            Set<String> cuisineNames = new HashSet<>();
            if (restaurant.getCuisines() != null) {
                for  (Cuisine cuisine : restaurant.getCuisines()) {
                    cuisineNames.add(cuisine.getName());
                }
            }
            rDTO.setCuisines(cuisineNames);
            // add image
            if (restaurant.getImage() != null) { // check if there is an image
                rDTO.setImageUrl(restaurant.getImage().getUrl());
            }

            restaurantDTOs.add(rDTO);
        }
        // Assign set to dto
        sessionDTO.setRestaurantDTOList(restaurantDTOs);

        return sessionDTO;
    }

    /**
     * A host creates a session
     * @param hostId id of joining host
     * @param preferenceId id of session preference (for filters)
     * @return return dto of a session object
     */
    @Transactional
    public SessionDTO createSession(Long hostId, Long preferenceId){
        //Find appropriate user and session preference
        User host = userRepository.findById(hostId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        SessionPreference pref = sessionPreferenceRepository.findById(preferenceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session preference not found"));

        // List of restaurants based on filters
        List<Restaurant> restaurants = restaurantService.findRestaurantsByPreferences(pref);

        // New session
        Session session = new Session();
        session.setHost(host);
//        session.setName(); not used for now
        session.setIsLive(true);
        String code = newJoinCode();
        while (sessionRepository.existsByJoinCode(code)){
            code = newJoinCode();
        }
        session.setJoinCode(code);
        session.setRestaurants(new HashSet<>(restaurants));
        // Save
        sessionRepository.save(session);
        // Add host
        SessionParticipant participant = new SessionParticipant();
        participant.setUser(host);
        participant.setSession(session);
        participant.setReady(false);
        sessionParticipantRepository.save(participant);
        session.addParticipant(participant);

        return convertSessionToDTO(session);
    }

    /**
     * Used to join a session. adds a new session participant object
     * @param request
     * @return
     */
    @Transactional
    public SessionParticipantDTO joinSession(JoinSessionRequest request) {
        Long userId = request.getUserId();
        String joinCode = request.getJoinCode();
        // Get user
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        // Get session by join code
        Session session = sessionRepository.findByJoinCode(joinCode).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        // Check if user already exists in this session
        if (sessionParticipantRepository.existsByUserAndSession(user, session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Participant is already in this session");
        }

        // Create new session participant
        SessionParticipant participant = new SessionParticipant(user, session);
        sessionParticipantRepository.save(participant);

        lobbyWebSocketHandler.broadcastLobbyUpdate(session.getId());


        return convertParticipantToDTO(participant);
    }

    /**
     * Sets a participant as ready
     * @param participantId participant id
     * @param ready boolean for ready
     * @return dto of updated session participant
     */
    @Transactional
    public SessionParticipantDTO setParticipantReady(Long participantId, boolean ready) {
        // Get participant from the repository by id
        SessionParticipant p = sessionParticipantRepository.findById(participantId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session participant not found"));
        // Set the ready status
        p.setReady(ready);
        SessionParticipant savedP =  sessionParticipantRepository.save(p);


        Session session = savedP.getSession();

        checkAndStart(session, false); // False because the host is not force starting the session
        lobbyWebSocketHandler.broadcastLobbyUpdate(session.getId()); // broadcast update
        return convertParticipantToDTO(savedP);
    }

    /**
     * Allows a host to kick a participant from a session
     * @param hostId id of the the host (for verification)
     * @param pId participant id to kick
     */
    @Transactional
    public void kick(Long hostId, Long pId) {
        SessionParticipant p = sessionParticipantRepository.findById(pId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session participant not found"));
        Session s = p.getSession();
        // Authenticate the host id
        if (!p.getSession().getHost().getId().equals(hostId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the host can start the session. User with this id is not a host: "+hostId);
        }
        // Host cannot kick themselves
        if (p.getUser().getId().equals(hostId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"A host cannot kick themself");
        }
        s.removeParticipant(p);
        sessionRepository.save(s);
        // NOTE: because orphan removal = true in session, the corresponding entry in session participants should be removed

        lobbyWebSocketHandler.broadcastLobbyUpdate(s.getId()); // broadcast update to SESSION p
    }

    /**
     * Force start session (a host does this)
     * @param hostId hostId (hosts user id) must be required for verification. only a host can start
     * @param sessionId id of the session to start
     */
    @Transactional
    public void startSession(Long hostId, Long sessionId) {
        Session s = sessionRepository.findById(sessionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        if (!s.getHost().getId().equals(hostId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the host can start the session. Host id given: "+hostId+", host id needed: "+s.getHost().getId());
        }

        checkAndStart(s, true); // Force starts a session

    }

    /**
     * Private helper method that checks if all participants in a session are ready.
     * Sets live to false and calls on the websocket handler to start the session
     * if all participants are ready, or if the host is force starting it
     * @param session session to start
     * @param hostStart boolean indicating if the host is force starting the session
     */
    @Transactional
    private void checkAndStart(Session session, boolean hostStart){
        // Check if session is live
        if (!session.getIsLive()){
            return;
        }
        // Check if all users are ready
        Set<SessionParticipant> participants = session.getParticipants();
        boolean allReady = true;
        if (!participants.isEmpty()) { // dont divide by 0
            for  (SessionParticipant participant : participants) { // loop through all session participants
                if (!participant.isReady()) {
                    allReady = false; // Set false if any single user is not ready
                }
            }
        }

        // Start session if host starts or if all users are ready
        if (hostStart || allReady) {
            session.setIsLive(false);
            sessionRepository.save(session);
            lobbyWebSocketHandler.broadcastLobbyUpdate(session.getId()); // broadcast update
        }
    }

    /**
     * Converts a session participant to a dto object
     * @param p participant
     * @return session participant dto
     */
    private SessionParticipantDTO convertParticipantToDTO(SessionParticipant p) {
        SessionParticipantDTO spDTO = new SessionParticipantDTO();
        spDTO.setParticipantId(p.getId());
        spDTO.setUserId(p.getUser().getId());
        spDTO.setName(p.getUser().getName());
        spDTO.setReady(p.isReady());
        spDTO.setFinishedSwiping(p.isFinishedSwiping());
        return spDTO;
    }

    /**
     * Creates a new join code string. 4 characters and unique
     * @return 4 character join code (unique)
     */
    private String newJoinCode() {
        // Initialize character array
        char[] chars = new char[26];
        chars[0] = 'A';
        chars[1] = 'B';
        chars[2] = 'C';
        chars[3] = 'D';
        chars[4] = 'E';
        chars[5] = 'F';
        chars[6] = 'G';
        chars[7] = 'H';
        chars[8] = 'I';
        chars[9] = 'J';
        chars[10] = 'K';
        chars[11] = 'L';
        chars[12] = 'M';
        chars[13] = 'N';
        chars[14] = 'O';
        chars[15] = 'P';
        chars[16] = 'Q';
        chars[17] = 'R';
        chars[18] = 'S';
        chars[19] = 'T';
        chars[20] = 'U';
        chars[21] = 'V';
        chars[22] = 'W';
        chars[23] = 'X';
        chars[24] = 'Y';
        chars[25] = 'Z';

        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            sb.append(chars[random.nextInt(26)]);
        }
        return sb.toString();
    }

    /**
     * marks a participant as finished swiping
     * @param participantId participant id to mark
     * @param isFinished boolean to mark by
     * @return
     */
    @Transactional
    public SessionParticipantDTO setParticipantFinishedSwiping(Long participantId, boolean isFinished) {
        // Get participant by id
        SessionParticipant p = sessionParticipantRepository.findById(participantId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session participant not found"));
        // Set status
        p.setFinishedSwiping(isFinished);
        SessionParticipant savedParticipant = sessionParticipantRepository.save(p);

        // Check if all participants are done swiping (this was the last one in the session)
        checkIfAllFinishedSwiping(p.getSession());
        // Return an updated DTO
        return convertParticipantToDTO(savedParticipant);
    }

    /**
     * Check if all participants are done swiping (this was the last one in the session). Triggers "move to results" logic
     * @param session session to check
     */
    @Transactional
    private void checkIfAllFinishedSwiping(Session session) {
        Set<SessionParticipant> participants = session.getParticipants();
        // do nothing if no participants
        if (participants.isEmpty()) {
            return;
        }

        // check each session participant in session
        for (SessionParticipant p : participants) {
            if (!p.isFinishedSwiping()) {
                return; // This means that if any singular participant has isFinishedSwiping = false, then don't do anything. stop here
            }
        }

        // If we don't return, that means all participants are finished swiping
        System.out.println("All participants in session: " + session.getId()+" have finished swiping");
        session.setSwipingComplete(true);
        sessionRepository.save(session);
    }

    /**
     * Return a session dto by a code
     * @param code
     * @return session dto
     */
    public SessionDTO getSessionByCodeDTO(String code) {
        System.out.println("Trying to find by code "+code);
        Session session = sessionRepository.findByJoinCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        return convertSessionToDTO(session);
    }
}
