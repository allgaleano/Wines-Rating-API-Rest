package es.model;

public class UserWine {
    private Integer userId;
    private Integer wineId;
    private Short rating;

    public UserWine() {
    }

    public UserWine(Integer userId, Integer wineId, Short rating) {
        this.userId = userId;
        this.wineId = wineId;
        this.rating = rating;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getWineId() {
        return wineId;
    }

    public Short getRating() {
        return rating;
    }


    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setWineId(Integer wineId) {
        this.wineId = wineId;
    }

    public void setRating(Short rating) {
        this.rating = rating;
    }

}
