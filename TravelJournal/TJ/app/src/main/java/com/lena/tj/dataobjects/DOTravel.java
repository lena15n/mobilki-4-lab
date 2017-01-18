package com.lena.tj.dataobjects;

import java.util.ArrayList;

public class DOTravel {
    private long id;
    private String name;
    private int color;
    private ArrayList<DOSight> sights;

    public DOTravel(long id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public DOTravel(long id, String name, int color, ArrayList<DOSight> sights) {
        this(id, name, color);

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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
