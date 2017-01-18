package com.lena.tj.dataobjects;

import java.util.HashMap;

public class DOSight {
    private long id;
    private String description;
    private double latitude;
    private double longitude;
    private String icon;
    private Long order;
    private Long travelId;
    private HashMap<Long, String> photos;// <Id, Uri>

    public DOSight() {

    }

    public DOSight(long id, String description, double latitude, double longitude, String icon) {
        this.id = id;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.icon = icon;
    }

    public DOSight(long id, String description, double latitude, double longitude, String icon, Long travelId,
                   Long order, HashMap<Long, String> photos) {
        this(id, description, latitude, longitude, icon);
        this.order = order;
        this.travelId = travelId;

        if (photos == null) {
            this.photos = new HashMap<>();
        } else {
            this.photos = photos;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public Long getTravelId() {
        return travelId;
    }

    public void setTravelId(Long travelId) {
        this.travelId = travelId;
    }

    public HashMap<Long, String> getPhotos() {
        return photos;
    }

    public void setPhotos(HashMap<Long, String> photos) {
        this.photos = photos;
    }

    public void addPhoto(Long id, String uri) {
        this.photos.put(id, uri);
    }
}
