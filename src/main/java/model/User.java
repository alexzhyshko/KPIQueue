package model;

public class User {

	public String username;
	public int userid;
	public long chatid;
	public int userstate = 0;
	public String name;
	
	public User(String username, String name, int userid, long chatid) {
		this.username = username;
		this.userid = userid;
		this.chatid = chatid;
		this.name = name;
	}
	
	
	public String getName() {
		if(username == null) {
			return name;
		}else {
			return username;
		}
	}
	
	
	
	
}
