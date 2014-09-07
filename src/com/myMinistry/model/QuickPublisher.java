package com.myMinistry.model;

public class QuickPublisher {
	private final int id;
	private final String name;
	
	public QuickPublisher(int _id, String _name) {
		id = _id;
		name = _name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public int getID() {
		return id;
	}
}
