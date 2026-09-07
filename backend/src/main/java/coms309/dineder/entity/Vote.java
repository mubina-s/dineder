package coms309.dineder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * A vote object represents one singular vote:
 * specific to a session participant (user + session), a restaurant, and a round
 * value - 0 or 1 for yes or no
 * Round - decides if the vote is for the initial swiping or final results (starts at 1)
 * timestamp - when the vote was cast
 *
 * Many to one with participant - a participant casts multiple votes
 * Many to one with restaurant - multiple votes associated with a restaurant
 * NOTE: no direct link with session needed, since sessionParticipants are in sessions
 *
 * @author Mason Gliege
 */
@Entity
@Table(name = "votes")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Vote value, 0 for no, 1 for yes. Must be enforced on creation
     */
    @Column(nullable = false)
    private int value;

    /**
     * Voting round. Should be enforced to be 1 by default
     */
    @Column(nullable = false)
    private int round ;

    /**
     * Time the vote was cast
     */
    @Column(nullable = false)
    private Instant timestamp;

    @JsonIgnore // prevent infinite loops
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionParticipantId", nullable = false)
    private SessionParticipant participant;

    @JsonIgnore // prevent infinite loops
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurantId", nullable = false)
    private Restaurant restaurant;

    public Vote() {}

    public Vote(SessionParticipant participant, Restaurant restaurant, int value, int round) {
        this.participant = participant;
        this.restaurant = restaurant;
        this.value = value;
        this.round = round;
        this.timestamp = Instant.now(); // Set time stamp when a vote is created (cast)
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public SessionParticipant getParticipant() {
        return participant;
    }

    public void setParticipant(SessionParticipant participant) {
        this.participant = participant;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
    // This may not be needed
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }


}
