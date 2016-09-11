package com.myMinistry.model;

import java.util.ArrayList;

public class HouseholderForTime {
    private int id;
    private String name;
    private String notes;
    private int notesID;
    private boolean return_visit;

    private int timeHouseholderPK;

    private ArrayList<QuickLiterature> literature = null;

    public HouseholderForTime(int id, String name, int timeHouseholderPK) {
        this.id = id;
        this.name = name;
        this.timeHouseholderPK = timeHouseholderPK;
        this.notes = "";
        this.notesID = 0;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String val) {
        notes = val;
    }

    public int getID() {
        return id;
    }

    public void setNotesID(int val) {
        notesID = val;
    }

    public int getNotesID() {
        return notesID;
    }

    public long getTimeHouseholderPK() {
        return timeHouseholderPK;
    }

    public void setTimeHouseholderPK(long val) {
        timeHouseholderPK = (int) val;
    }

    public boolean isCountedForReturnVisit() {
        return return_visit;
    }

    public void setCountedForReturnVisit(boolean return_visit) {
        this.return_visit = return_visit;
    }

    public void setCountedForReturnVisit(int return_visit) {
        this.return_visit = return_visit != 0;
    }

    public void toggleCountedForReturnVisit() {
        return_visit = !return_visit;
    }

    public ArrayList<QuickLiterature> getLit() {
        if (literature == null) {
            literature = new ArrayList<>();
        }

        return literature;
    }

    public int addLit(QuickLiterature lit) {
        int retVal = -1;
        boolean shouldAdd = true;

        if (getLit() == null) {
            literature = new ArrayList<>();
        } else {
            for (QuickLiterature qlit : literature) {
                retVal++;
                if (qlit.getID() == lit.getID()) {
                    //qlit = lit;
                    shouldAdd = false;
                    break;
                }
            }
        }

        if (shouldAdd) {
            literature.add(lit);
            retVal = literature.size() - 1;
        }

        return retVal;
    }
}