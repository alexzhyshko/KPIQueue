package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class Controller {

	static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/kpiqueue";
	static final String USER = "postgres";
	static final String PASS = "root";

	Connection connection;

	public Controller() {
		try {
			//this.connection = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));
			this.connection = DriverManager.getConnection(DB_URL, USER, PASS);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public User getUser(int userid) {
		User user = null;
		String query = "SELECT username, userstate, name, surname, id, chatid  FROM Users WHERE id=?";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, userid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				user = new User(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getInt(5),
						rs.getLong(6));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

	public void createUser(String username, String name, String surname, int userid, long chatid) {
		String query0 = "SELECT EXISTS(SELECT id FROM Users where id=?)";
		boolean exists = true;
		try (PreparedStatement ps = connection.prepareStatement(query0)) {
			ps.setInt(1, userid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				exists = rs.getBoolean(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!exists) {
			String query = "INSERT INTO Users(username, name, id, chatid) VALUES(?,?,?,?) ON CONFLICT (id) DO NOTHING";
			try (PreparedStatement ps = connection.prepareStatement(query)) {
				ps.setString(1, username);
				ps.setString(2, name);
				ps.setInt(3, userid);
				ps.setLong(4, chatid);
				ps.executeUpdate();
			} catch (Exception e) {
				// if users exists - exception
				e.printStackTrace();
			}
		}

	}

	public List<Queue> getQueues() {
		List<Queue> queues = new ArrayList<>();
		String query = "SELECT dayofweek, twoweek, evenweek, creator_id, name, time, id FROM Queues";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				queues.add(new Queue(rs.getInt(7), DayOfWeek.valueOf(rs.getString(1).toUpperCase()), rs.getBoolean(2),
						rs.getBoolean(3), rs.getInt(4), rs.getString(5), rs.getTime(6)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return queues;
	}

	public void setUserState(User u, int state) {
		String query = "UPDATE Users SET userstate=? WHERE id=?;";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, state);
			ps.setInt(2, u.userid);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getUserState(User u) {
		int state = 0;
		String query = "SELECT userstate FROM Users WHERE id=?;";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, u.userid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				state = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return state;
	}

	public List<User> getUsersInQueue(Queue queue) {
		List<User> users = new ArrayList<>();
		String query = "SELECT Users.username, Users.userstate, Users.name, Users.surname, Users.id, Users.chatid FROM Users JOIN user_queue ON Users.id = user_queue.user_id JOIN Queues ON Queues.id = user_queue.queue_id WHERE queues.id=? ORDER BY user_queue.place ASC;";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, queue.id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				users.add(new User(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getInt(5),
						rs.getLong(6)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return users;
	}

	public boolean addUserToQueue(User user, Queue queue) {
		String query = "SELECT EXISTS(SELECT user_id FROM user_queue WHERE queue_id = ? AND user_id=?);";
		boolean userExists = true;
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, queue.id);
			ps.setInt(2, user.userid);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				userExists = rs.getBoolean(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (userExists) {
			return false;
		}
		query = "INSERT INTO user_queue(user_id, queue_id, place) VALUES(?,?,(CASE WHEN (SELECT place FROM user_queue WHERE queue_id=? ORDER BY place DESC LIMIT 1)>0 THEN (SELECT place FROM user_queue WHERE queue_id=? ORDER BY place DESC LIMIT 1)+1 ELSE 1 END));";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, user.userid);
			ps.setInt(2, queue.id);
			ps.setInt(3, queue.id);
			ps.setInt(4, queue.id);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;

	}

	public void createQueue(Queue q) {
		String query = "INSERT INTO Queues(dayOfweek, twoweek, evenweek, name, time, creator_id) VALUES(?,?,?,?,?,?);";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			Time time = Time.valueOf(q.hour + ":" + q.minute + ":00");
			ps.setString(1, q.day.name());
			ps.setBoolean(2, q.twoWeek);
			ps.setBoolean(3, q.evenweek);
			ps.setString(4, q.name);
			ps.setTime(5, time);
			ps.setInt(6, q.creator_id);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeQueue(Queue q) {
		String query = "DELETE FROM user_queue WHERE queue_id = ?;";
		String query2 = "DELETE FROM queues WHERE id = ?;";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			PreparedStatement ps2 = connection.prepareStatement(query2);
			ps.setInt(1, q.id);
			ps2.setInt(1, q.id);
			ps.executeUpdate();
			ps2.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public User getCreator(Queue q) {
		User user = null;
		String query = "SELECT Users.username, Users.userstate, Users.name, Users.surname, Users.id, Users.chatid FROM Users JOIN Queues ON Users.id = Queues.creator_id WHERE Queues.id = ?";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, q.id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				user = new User(rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getInt(5),
						rs.getLong(6));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

	public Notification removeUserFromQueue(User user, Queue queue) {
		String exists = "SELECT EXISTS(SELECT user_id FROM user_queue WHERE user_id=? AND queue_id=?)";
		boolean userExists = true;
		try (PreparedStatement ps = connection.prepareStatement(exists)) {
			ps.setInt(1, user.userid);
			ps.setInt(2, queue.id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				userExists = rs.getBoolean(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (userExists) {
			String check = "SELECT place FROM user_queue WHERE user_id=? AND queue_id=?";
			int place = 0;
			try (PreparedStatement ps = connection.prepareStatement(check)) {
				ps.setInt(1, user.userid);
				ps.setInt(2, queue.id);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					place = rs.getInt(1);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			String query = "DELETE FROM user_queue WHERE user_id=? AND queue_id=?";
			try (PreparedStatement ps = connection.prepareStatement(query)) {
				ps.setInt(1, user.userid);
				ps.setInt(2, queue.id);
				ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			String query2 = "UPDATE user_queue SET place = place-1 WHERE queue_id=? AND place>?";
			try (PreparedStatement ps = connection.prepareStatement(query2)) {
				ps.setInt(1, queue.id);
				ps.setInt(2, place);
				ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			if (place == 1) {
				String query3 = "SELECT Users.username, Users.userstate, Users.name, Users.surname, Users.id, Users.chatid FROM Users JOIN user_queue ON Users.id = user_queue.user_id WHERE user_queue.queue_id=? AND place=1";
				try (PreparedStatement ps = connection.prepareStatement(query3)) {
					ps.setInt(1, queue.id);
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						return new Notification(new User(rs.getString(1), rs.getInt(2), rs.getString(3),
								rs.getString(4), rs.getInt(5), rs.getLong(6)), true);
					}
					return new Notification(null, true);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			} else {
				return new Notification(null, true);
			}
		}else {
			return null;
		}

	}

}
