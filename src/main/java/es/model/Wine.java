package es.model;

import java.sql.Timestamp;
import java.util.List;



public class Wine {
    private Integer id;
    private String name;
    private String winery;
    private String origin;
    private String type;
    private List<String> grapes;
    private Short vintage;
    private Timestamp incorporation;
    private Timestamp dateAdded;
    private Short rating;

    // Constructor
    public Wine(Integer id, String name, String winery, String origin, String type, List<String> grapes, short vintage, Timestamp incorporation, Timestamp dateAdded, Short rating) {
        this.id = id;
        this.name = name;
        this.winery = winery;
        this.origin = origin;
        this.type = type;
        this.grapes = grapes;
        this.vintage = vintage;
        this.incorporation = incorporation;
        this.dateAdded = dateAdded;
        this.rating = rating;
    }

    public Wine() {
	}

	// Getters y setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWinery() {
        return winery;
    }

    public void setWinery(String winery) {
        this.winery = winery;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getGrapes() {
        return grapes;
    }

    public void setGrapes(List<String> grapes) {
        this.grapes = grapes;
    }

    public Short getVintage() {
        return vintage;
    }

    public void setVintage(Short vintage) {
        this.vintage = vintage;
    }

    public Timestamp getIncorporation() {
        return incorporation;
    }

    public void setIncorporation(Timestamp incorporation) {
        this.incorporation = incorporation;
    }

    public Timestamp getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Timestamp dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Short getRating() {
        return rating;
    }

    public void setRating(Short rating) {
        this.rating = rating;
    }
}
