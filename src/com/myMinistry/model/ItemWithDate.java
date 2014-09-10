package com.myMinistry.model;

public class ItemWithDate {
	public int id;
	public String date;
	public String title;
	
	public ItemWithDate(int id, String title, String date) {
		this.id = id;
		this.date = date;
		this.title = title;
	};
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String toString() {
		return this.title;
	}
	
	public int getID() {
		return this.id;
	}
}
