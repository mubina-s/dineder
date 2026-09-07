package coms309.dineder.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * This is the "core" entity that ties several main features together.
 * A session is what ties the users, restaurants, images, cuisines, votes, final results together.
 * Name of session may or may not be used
 * joinCode is how users join
 * isLive = true means users can join
 * startTime: instant of when it was created
 * swipingComplete: boolean for if all participants are done swiping
 *
 * A session is mapped to users by a host (Many to one)
 * SESSION PARTICIPANT: solves the issue with many to many users and session. More attributes need
 * to be stored with the users like finishedSwiping, ready, etc that the users table does not have.
 * Thus, this table was needed.
 * One to many with session participant
 * Many to many with restaurants
 * One to one  with result
 *
 * @author Mason Gliege
 *
 */
@Entity
@Table(name = "session")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String joinCode;
    private boolean isLive = true;
    // Marks when sessionRepository.save(session) was called, not when using the constructor
    @CreationTimestamp
    private Instant startTime;

    /**
     * Host of the session.
     * There can be one host per session.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostId", nullable = false)
    private User host;

    /**
     * Participants of the session.
     * There are multiple participants mapped to one session.
     * Update all session participants associated with a session
     * Remove any orphan session participants (specific to session)
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SessionParticipant> participants = new HashSet<>();

    /**
     * Restaurants in a session.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sessionRestaurants", joinColumns = @JoinColumn(name = "sessionId"), inverseJoinColumns = @JoinColumn(name = "restaurantId"))
    private Set<Restaurant> restaurants = new HashSet<>();


    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    private SessionResult result;

    // This dictates if the swiping is still going, or if its transitioning to the final results
    @Column
    private Boolean swipingComplete = false;

    public Session() {

    }

    public Session(User host, String name){
        this.host = host;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public Boolean getIsLive() {
        return isLive;
    }

    public void setIsLive(Boolean isLive) {
        this.isLive = isLive;
    }

    public Instant getStartTime() {
        return startTime;
    }

    // NOTE: we should not be making a setStartTime method

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public Set<SessionParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<SessionParticipant> participants) {
        this.participants = participants;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    // TODO: getter and setter for results

    public void addParticipant(SessionParticipant participant) {
        this.participants.add(participant);
        participant.setSession(this);
    }

    public void removeParticipant(SessionParticipant participant) {
        this.participants.remove(participant);
        participant.setSession(null);
    }

    public void addRestaurant(Restaurant restaurant) {
        this.restaurants.add(restaurant);
    }

    public void removeRestaurant(Restaurant restaurant) {
        this.restaurants.remove(restaurant);
    }

    public SessionResult getResult() {
        return result;
    }

    public void setResult(SessionResult result) {
        this.result = result;
    }

    public Boolean isSwipingComplete() {
        return swipingComplete;
    }

    public void setSwipingComplete(Boolean swipingComplete) {
        this.swipingComplete = swipingComplete;
    }



}
