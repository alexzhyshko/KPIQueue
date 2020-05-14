package model;

public class Notification {

	public User user;
	public boolean needToNotify;
	
	
	public Notification(User user, boolean needToNotify) {
		this.user = user;
		this.needToNotify = needToNotify;
	}
	
	
	
}
