package coms309.dineder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import coms309.dineder.entity.*;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * Session participant entity
 * boolean isReady
 * boolean finishedSwiping
 * Links to users
 *
 * Session participants solves the many to many problem with users and sessions. There
 * needs to be additional info stored with the participants in a session
 * (like finishedSwiping, isReady, etc) that is not in the users table.
 * This solves that problem and connects in the following ways:
 * Many to one with session
 * One to many with votes (a participant casts multiple votes)
 * Many to one with user (a user may be a participant in multiple sessions over time)
 *
 * @author Mason Gliege
 *
 */
@Entity
@Table(name = "sessionParticipant")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class SessionParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private boolean isReady = false;
    @Column(nullable = false)
    private boolean finishedSwiping = false;

    /**
     * Corresponding user to this session participant
     */
    @JsonIgnore // prevent infinite loops
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    /**
     * Corresponding session to this session participant
     */
    @JsonIgnore // prevent infinite loops
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionId", nullable = false)
    private Session session;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Vote> votes = new HashSet<>();

    public SessionParticipant() {}

    public SessionParticipant(User user, Session session) {
        this.user = user;
        this.session = session;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public boolean isReady() {
        return isReady;
    }
    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isFinishedSwiping() {
        return finishedSwiping;
    }

    public void setFinishedSwiping(boolean finishedSwiping) {
        this.finishedSwiping = finishedSwiping;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void addVote(Vote vote) {
        votes.add(vote);
        vote.setParticipant(this);
    }

    public void removeVote(Vote vote) {
        votes.remove(vote);
        vote.setParticipant(null);
    }

    public Set<Vote> getVotes() {
        return votes;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }
    //TODO: vote getters and setters(get, set, add, remove)
}
