package com.lena.tj.dataobjects;

import java.util.ArrayList;

public class DOTravel {
    private long id;
    private String name;
    private ArrayList<DOSight> sights;

    public DOTravel(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public DOTravel(long id, String name, ArrayList<DOSight> sights) {
        this(id, name);

        this.sights = sights == null ? new ArrayList<DOSight>() : sights;
    }

    public void addSight(DOSight sight){
        this.sights.add(sight);
    }

    public void addPhotoToTheLastSight(Long id, String uri){
        DOSight sight = sights.get(sights.size() - 1);
        sight.addPhoto(id, uri);
        sights.add(sights.size() - 1, sight);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<DOSight> getSights() {
        return sights;
    }

    public void setSights(ArrayList<DOSight> sights) {
        this.sights = sights;
    }
}
