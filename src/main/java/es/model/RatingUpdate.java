package es.model;

public class RatingUpdate {
    private Short rating;

    public RatingUpdate() {
    }

    public RatingUpdate(Short rating) {
        this.rating = rating;
    }

    public Short getRating() {
        return rating;
    }

    public void setRating(Short rating) {
        this.rating = rating;
    }
}
