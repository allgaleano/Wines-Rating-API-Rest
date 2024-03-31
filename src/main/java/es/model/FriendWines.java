package es.model;

public class FriendWines {
    private User friend;
    private Wine[] wines;

    public FriendWines(User friend, Wine[] wines) {
        this.friend = friend;
        this.wines = wines;
    }

    public FriendWines() {
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public Wine[] getWines() {
        return wines;
    }

    public void setWines(Wine[] wines) {
        this.wines = wines;
    }
}
