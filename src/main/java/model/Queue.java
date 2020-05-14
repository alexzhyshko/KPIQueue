package model;

import java.sql.Time;
import java.time.DayOfWeek;

public class Queue {

	public DayOfWeek day;
	// false = once a week, true - once two weeks
	public int id;
	public boolean twoWeek = false;
	public boolean evenweek = true;
	public String name;
	public int hour;
	public int minute;
	public int creator_id;

	public Queue(int id, DayOfWeek day, boolean twoWeek, boolean evenweek, int creator, String name, Time time) {
		this.id = id;
		this.day = day;
		this.twoWeek = twoWeek;
		this.name = name;
		this.evenweek = evenweek;
		this.hour = time.toLocalTime().getHour();
		this.minute = time.toLocalTime().getMinute();
	}

	public Queue(DayOfWeek day, boolean twoWeek, boolean evenweek, int creator, String name, String time) {
		this.day = day;
		this.twoWeek = twoWeek;
		this.name = name;
		this.evenweek = evenweek;
		this.hour = Integer.parseInt(time.split(":")[0]);
		this.minute = Integer.parseInt(time.split(":")[1]);
	}

}
