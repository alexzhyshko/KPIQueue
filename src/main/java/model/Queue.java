package model;

import java.time.DayOfWeek;

public class Queue {

	public DayOfWeek day;
	//false = once a week, true - once two weeks
	public boolean twoWeek = false;
	public boolean evenweek = true;
	public String name;
	
	
	public Queue(DayOfWeek day, boolean twoWeek, boolean evenweek, String name) {
		this.day = day;
		this.twoWeek = twoWeek;
		this.name = name;
		this.evenweek = evenweek;
	}
	
	
	
	
	
}
