package model.group;

public class Participant {
    private final UserProfile user;
    private final int partySize; // Number of people joined in this entry

    public Participant(UserProfile user, int partySize) {
        this.user = user;
        this.partySize = Math.max(1, partySize);
    }
    public UserProfile getUser() { return user; }
    public int getPartySize() { return partySize; }
}
