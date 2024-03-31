package sos.model;

import java.util.List;

public class WineUpdate {
    private String name;
    private String winery;
    private String origin;
    private String type;
    private List<String> grapes;
    private Short vintage;

    public WineUpdate() {
    }

    public WineUpdate(String name, String winery, String origin, String type, List<String> grapes, Short vintage) {
        this.name = name;
        this.winery = winery;
        this.origin = origin;
        this.type = type;
        this.grapes = grapes;
        this.vintage = vintage;
    }

    public String getName() {
        return name;
    }

    public String getWinery() {
        return winery;
    }

    public String getOrigin() {
        return origin;
    }

    public String getType() {
        return type;
    }

    public List<String> getGrapes() {
        return grapes;
    }

    public Short getVintage() {
        return vintage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWinery(String winery) {
        this.winery = winery;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGrapes(List<String> grapes) {
        this.grapes = grapes;
    }

    public void setVintage(Short vintage) {
        this.vintage = vintage;
    }
}
