package com.myMinistry.model;

public class QuickLiterature {
    private final int id;
    private long placedID;
    private int count;
    private final String name;
    private int typeID;

    public QuickLiterature(int _id, String _name) {
        id = _id;
        name = _name;
        count = 0;
        placedID = 0;
    }

    public QuickLiterature(int _id, String _name, int _count, int typeID) {
        id = _id;
        name = _name;
        count = _count;
        placedID = 0;
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

    public void setCount(int _count) {
        count = _count;
    }

    public long getPlacedID() {
        return placedID;
    }

    public void setPlacedID(long _id) {
        placedID = _id;
    }
}