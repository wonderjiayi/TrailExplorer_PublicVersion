package model.group;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.trail.Trail;

public class Group {
    private final String id;
    private final Trail trail;
    private final String title;
    private final LocalDateTime startTime;

    private final int capacity;
    private final List<Participant> participants = new ArrayList<>();

    public Group(String id, Trail trail, String title,
                 LocalDateTime startTime, int capacity) {
        this.id = id;
        this.trail = trail;
        this.title = title;
        this.startTime = startTime;
        this.capacity = Math.max(1, capacity);
    }

    public int getCurrentSize() {
        return participants.stream().mapToInt(Participant::getPartySize).sum();
    }
    public int getRemainingSlots() { return Math.max(0, capacity - getCurrentSize()); }
    public boolean isFull() { return getCurrentSize() >= capacity; }
    public boolean canJoin(int partySize) {
        int n = Math.max(1, partySize);
        return getCurrentSize() + n <= capacity;
    }
    public void join(UserProfile user, int partySize) {
        if (!canJoin(partySize)) throw new IllegalStateException("Group capacity exceeded");
        participants.add(new Participant(user, Math.max(1, partySize)));
    }

    // getters
    public String getId() { return id; }
    public Trail getTrail() { return trail; }
    public String getTitle() { return title; }
    public LocalDateTime getStartTime() { return startTime; }
    public int getCapacity() { return capacity; }
    public List<Participant> getParticipants() { return participants; }
}
