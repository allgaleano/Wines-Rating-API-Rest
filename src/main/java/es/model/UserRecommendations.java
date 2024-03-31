package es.model;

import java.util.List;

public class UserRecommendations {
    private User userInfo;
    private Wine lastAddedWines[];
    private Wine topRatedWines[];
    private List<FriendWines> friendsWines;

    public UserRecommendations(User userInfo, Wine[] lastAddedWines, Wine[] topRatedWines, List<FriendWines> friendsWines) {
        this.userInfo = userInfo;
        this.lastAddedWines = lastAddedWines;
        this.topRatedWines = topRatedWines;
        this.friendsWines = friendsWines;
    }

    public UserRecommendations() {
    }

    public User getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(User userInfo) {
        this.userInfo = userInfo;
    }

    public Wine[] getLastAddedWines() {
        return lastAddedWines;
    }

    public void setLastAddedWines(Wine[] lastAddedWines) {
        this.lastAddedWines = lastAddedWines;
    }

    public Wine[] getTopRatedWines() {
        return topRatedWines;
    }

    public void setTopRatedWines(Wine[] topRatedWines) {
        this.topRatedWines = topRatedWines;
    }

    public List<FriendWines> getFriendsWines() {
        return friendsWines;
    }

    public void setFriendsWines(List<FriendWines> friendsWines) {
        this.friendsWines = friendsWines;
    }
}
