package model;

public class User {

	public String username;
	public int userid;
	public int queue = -1;
	public long chatid;
	public int userstate = 0;
	
	
	public User(String username, int userid, long chatid) {
		this.username = username;
		this.userid = userid;
		this.chatid = chatid;
	}
	
}
