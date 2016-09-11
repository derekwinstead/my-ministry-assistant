package com.myMinistry.model;

public class QuickLiterature {
    private final int id;
    private long placedID;
    private int count;
    private final String name;
    private int typeID;

    /*
        public QuickLiterature(int _id, String _name) {
            id = _id;
            name = _name;
            count = 0;
            placedID = 0;
        }
    */
    public QuickLiterature(int id, String name, int count, int typeID) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.placedID = 0;
        this.typeID = typeID;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getID() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public int getTypeID() {
        return this.typeID;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getPlacedID() {
        return placedID;
    }

    public void setPlacedID(long id) {
        this.placedID = id;
    }
}