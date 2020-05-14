package model;

public class User {

	public String username;
	public int userid;
	public long chatid;
	public int userstate;
	public String name;
	public String surname;
	
	public User(String username, int userstate, String name, String surname, int userid, long chatid) {
		this.username = username;
		this.userid = userid;
		this.chatid = chatid;
		this.name = name;
		this.surname = surname;
	}
	
	
	public String getName() {
		if(username == null) {
			return name+" "+(surname!=null?surname:"");
		}else {
			return "@"+username;
		}
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (chatid != other.chatid)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (userid != other.userid)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (userstate != other.userstate)
			return false;
		return true;
	}
	
	
	
	
}
