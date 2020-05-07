package main;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import model.Queue;
import model.Stack;
import model.User;

public class Bot extends TelegramLongPollingBot {

	private final String name = "kpiqueue228_bot";
	private final String token = "1169641439:AAGg2EvlSjYaUAM4DT5B5x07brfEb5s3QYQ";
	// users
	private HashMap<Integer, User> users = new HashMap<>();
	// queue
	private HashMap<Queue, Stack> queues = new HashMap<>();
	// session
	private HashMap<Integer, HashMap<String, Object>> sessions = new HashMap<>();
	// queue first
	private HashMap<String, User> first = new HashMap<>();
	// creators
	private HashMap<Queue, Integer> creators = new HashMap<>();

	int weekNum = LocalDate.now().getDayOfYear() / 7;

	public Bot() {
		try {
			new Thread(() -> {
				try {
					while (true) {
						for (Entry e : queues.entrySet()) {
							Stack st = (Stack) e.getValue();
							Queue q = (Queue) e.getKey();
							String name = q.name;
							User u = st.getFirst();
							if (first.get(name) == null) {
								if (u != null) {
									first.put(name, u);
									SendMessage not = new SendMessage();
									not.enableMarkdown(true);
									not.setChatId(u.chatid);
									not.setText("You are first now in queue: " + name + ", It's your turn");
									execute(not);
								}
							} else if (!first.get(name).equals(u)) {
								if (u != null) {
									first.put(name, u);
									SendMessage not = new SendMessage();
									not.enableMarkdown(true);
									not.setChatId(u.chatid);
									not.setText("You are first now in queue: " + name + ", It's your turn");
									execute(not);
								}
							}

						}

						Thread.sleep(2000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onUpdateReceived(Update update) {
		try {

			SendMessage response = new SendMessage();
			long chatid = update.getMessage().getChatId();
			int userid = update.getMessage().getFrom().getId();
			String username = update.getMessage().getFrom().getUserName();
			String name = update.getMessage().getFrom().getFirstName();
			response.setChatId(Long.toString(chatid));
			String message = update.getMessage().getText();
			if (sessions.get(userid) == null) {
				sessions.put(userid, new HashMap<String, Object>());
			}
			HashMap<String, Object> session = sessions.get(userid);
			if (!users.containsKey(userid)) {
				users.put(userid, new User(username, name, userid, chatid));
			}
			User user = users.get(userid);

			switch (user.userstate) {
			case 0:
				response.setText("Choose queue");
				List<String> btn = queues.keySet().stream().map(e -> e.name).collect(Collectors.toList());
				btn.add("Add queue");
				setButtons(btn, response);
				user.userstate = 1;
				break;
			case 1:
				if (message.equals("Add queue")) {
					response.setText("Enter name");
					setButtons(Arrays.asList("Back"), response);
					user.userstate = 3;
				} else if (message.equals("Back")) {
					response.setText("Choose queue");
					List<String> btns = queues.keySet().stream().map(e -> e.name).collect(Collectors.toList());
					btns.add("Add queue");
					btns.add("Back");
					setButtons(btns, response);
					user.userstate = 1;
				} else {
					List<String> names = queues.keySet().stream().map(e -> e.name).collect(Collectors.toList());
					if (names.contains(message)) {
						session.put("queuename", message);
						response.setText("Ok, what's doin'");
						setButtons(
								Arrays.asList("Sign in", "Show queue", "Remove me", "I passed", "Delete queue", "Back"),
								response);
						user.userstate = 2;
					} else {
						response.setText("No such queue");
					}
				}

				break;
			case 2:
				Stack stack = null;
				Queue queue = null;
				for (Entry e : queues.entrySet()) {
					if (((Queue) (e.getKey())).name.equals(session.get("queuename"))) {
						stack = (Stack) e.getValue();
						queue = (Queue) e.getKey();
						break;
					}
				}
				if(queue==null) {
					response.setText("No such queue anymore, choose another");
					response.setText("Choose queue");
					List<String> buttons = queues.keySet().stream().map(e -> e.name).collect(Collectors.toList());
					setButtons(buttons, response);
					user.userstate = 1;
					break;
				}
				if (message.equals("Back")) {
					response.setText("Choose queue");
					List<String> buttons = queues.keySet().stream().map(e -> e.name).collect(Collectors.toList());
					buttons.add("Add queue");
					setButtons(buttons, response);
					user.userstate = 1;
				} else if (message.equals("Sign in")) {
					boolean twoWeek = queue.twoWeek;
					boolean evenweek = queue.evenweek;
					setButtons(Arrays.asList("Sign in", "Show queue", "Remove me", "I passed", "Delete queue", "Back"),
							response);
					if (twoWeek) {
						if (weekNum % 2 == 0 && evenweek) {
							if ((LocalTime.now().isAfter(LocalTime.of(18, 0, 0))
									&& LocalDate.now().getDayOfWeek().compareTo(queue.day.minus(2)) == 0)
									|| (LocalDate.now().getDayOfWeek().compareTo(queue.day.minus(2)) > 0
											&& LocalDate.now().getDayOfWeek().compareTo(queue.day) <= 0 && LocalTime.now()
													.isBefore(LocalTime.of(queue.hour + 1, queue.minute + 30, 0)))) {
								if (stack.push(user)) {
									response.setText("Ok, you are " + (stack.indexOf(user) + 1) + " in the queue");
								} else {
									response.setText("You are already in queue");
								}
							} else {
								response.setText("Signing in is locked");
							}
						} else if (weekNum % 2 == 1 && !evenweek) {
							if ((LocalTime.now().isAfter(LocalTime.of(18, 0, 0))
									&& LocalDate.now().getDayOfWeek().compareTo(queue.day.minus(2)) == 0)
									|| (LocalDate.now().getDayOfWeek().compareTo(queue.day.minus(2)) > 0
											&& LocalDate.now().getDayOfWeek().compareTo(queue.day) <= 0 && LocalTime.now()
													.isBefore(LocalTime.of(queue.hour + 1, queue.minute + 30, 0)))) {
								if (stack.push(user)) {
									response.setText("Ok, you are " + (stack.indexOf(user) + 1) + " in the queue");
								} else {
									response.setText("You are already in queue");
								}
							} else {
								response.setText("Signing in is locked");
							}
						} else {
							response.setText("Signing in is locked");
						}

					} else {
						if ((LocalTime.now().isAfter(LocalTime.of(18, 0, 0))
								&& LocalDate.now().getDayOfWeek().compareTo(queue.day.minus(2)) == 0)
								|| (LocalDate.now().getDayOfWeek().compareTo(queue.day.minus(2)) > 0
										&& LocalDate.now().getDayOfWeek().compareTo(queue.day) <= 0 && LocalTime.now()
												.isBefore(LocalTime.of(queue.hour + 1, queue.minute + 30, 0)))) {
							if (stack.push(user)) {
								response.setText("Ok, you are " + (stack.indexOf(user) + 1) + " in the queue");
							} else {
								response.setText("You are already in queue");
							}
						} else {
							response.setText("Signing in is locked");
						}
					}

				} else if (message.equals("Show queue")) {
					setButtons(Arrays.asList("Sign in", "Show queue", "Remove me", "I passed", "Delete queue", "Back"),
							response);
					List<User> users = stack.getAll();
					String text = "";
					for (int i = 0; i < users.size(); i++) {
						text += (i + 1) + " - " + users.get(i).getName() + "\n";
					}
					if (text.isBlank()) {
						text = "Queue empty";
					}
					response.setText(text);
				} else if (message.equals("Remove me")) {
					setButtons(Arrays.asList("Sign in", "Show queue", "Remove me", "I passed", "Delete queue", "Back"),
							response);
					if (stack.remove(user)) {
						if (first.get((String) session.get("queuename")).equals(user))
							first.put((String) session.get("queuename"), null);
						response.setText("Ok");
					} else {
						response.setText("You are not in queue");
					}
				} else if (message.equals("I passed")) {
					setButtons(Arrays.asList("Sign in", "Show queue", "Remove me", "I passed", "Delete queue", "Back"),
							response);
					if (stack.remove(user)) {
						if (first.get((String) session.get("queuename")).equals(user))
							first.put((String) session.get("queuename"), null);
						response.setText("Nice");
					} else {
						response.setText("You are not in queue");
					}
				} else if (message.equals("Delete queue")) {
					for (Entry e : queues.entrySet()) {
						if (((Queue) (e.getKey())).name.equals(session.get("queuename"))) {
							Queue tmp = (Queue) e.getKey();
							if (creators.get(tmp).equals(userid)) {
								queues.remove(tmp);
								creators.remove(tmp);
								first.remove(tmp.name);
								response.setText("Deleted");
								List<String> buttons = queues.keySet().stream().map(o -> o.name)
										.collect(Collectors.toList());
								buttons.add("Add queue");
								setButtons(buttons, response);
								user.userstate = 1;
								break;
							} else {
								response.setText("You are not allowed to delete this queue");
								break;
							}

						}
					}
				} else {
					response.setText("Don't know such commands");
				}
				break;
			case 3:
				if (message.equals("Back")) {
					response.setText("Choose queue");
					List<String> buttons = queues.keySet().stream().map(e -> e.name).collect(Collectors.toList());
					buttons.add("Add queue");
					setButtons(buttons, response);
					user.userstate = 1;
				} else {
					boolean queueExists = false;
					for (Entry e : queues.entrySet()) {
						if (((Queue) (e.getKey())).name.equals(message)) {
							queueExists = true;
							break;
						}
					}
					if (queueExists) {
						response.setText("This queue exists");
					} else {
						session.put("newqueuename", message);
						response.setText("Now choose day of week");
						List<String> buttons = Arrays.asList(DayOfWeek.values()).stream().map(e -> e.name())
								.collect(Collectors.toList());
						buttons.add("Back");
						setButtons(buttons, response);
						user.userstate = 4;
					}
				}
				break;
			case 4:
				if (message.equals("Back")) {
					response.setText("Enter name");
					setButtons(Arrays.asList("Back"), response);
					user.userstate = 3;
				} else {
					List<String> buttons = Arrays.asList(DayOfWeek.values()).stream().map(e -> e.name())
							.collect(Collectors.toList());
					if (!buttons.contains(message)) {
						response.setText("This queue exists");
					} else {
						session.put("newqueueday", message);
						response.setText("Now choose period");
						List<String> button = Arrays.asList("Once a week", "Once two weeks", "Back");
						setButtons(button, response);
						user.userstate = 5;
					}
				}
				break;
			case 5:
				if (message.equals("Back")) {
					List<String> buttons = Arrays.asList(DayOfWeek.values()).stream().map(e -> e.name())
							.collect(Collectors.toList());
					buttons.add("Back");
					setButtons(buttons, response);
					user.userstate = 4;
				} else {
					if (!Arrays.asList("Once a week", "Once two weeks").contains(message)) {
						response.setText("This queue exists");
					} else {
						session.put("newqueuerepeat", message);
						if (message.equals("Once two weeks")) {
							setButtons(Arrays.asList("Even", "Odd"), response);
							response.setText("Choose week on which to repeat");
							user.userstate = 6;
						} else {
							response.setText("Enter time (HH:mm)");
							user.userstate = 7;

						}
					}
				}
				break;
			case 6:
				if (Arrays.asList("Even", "Odd").contains(message)) {
					session.put("newqueueweek", message);
					response.setText("Enter time (HH:mm)");
					user.userstate = 7;

				} else {
					response.setText("Don't know such commands");
				}
				break;
			case 7:
				String t = (String) session.get("newqueueweek");
				Queue q = null;
				if (t != null) {
					q = new Queue(DayOfWeek.valueOf((String) session.get("newqueueday")),
							session.get("newqueuerepeat").equals("Once two weeks"),
							session.get("newqueueweek").equals("Even"), (String) session.get("newqueuename"), message);
				} else {
					q = new Queue(DayOfWeek.valueOf((String) session.get("newqueueday")),
							session.get("newqueuerepeat").equals("Once two weeks"), false,
							(String) session.get("newqueuename"), message);
				}
				creators.put(q, userid);
				queues.put(q, new Stack());
				first.put(q.name, null);
				List<String> buttons = queues.keySet().stream().map(e -> e.name).collect(Collectors.toList());
				buttons.add("Add queue");
				setButtons(buttons, response);
				user.userstate = 1;
				response.setText("Ok");

				break;

			}

			execute(response);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getBotUsername() {
		return name;
	}

	@Override
	public String getBotToken() {
		return token;
	}

	public synchronized void setButtons(List<String> texts, SendMessage response) {
		setButtonsWhithColumnCount(texts, response, 2);
	}

	public synchronized void setButtonsWhithColumnCount(List<String> texts, SendMessage response, int columnCount) {
		response.enableMarkdown(true);
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboard(true);
		int rowsCount = (int) Math.ceil(texts.size() / (double) columnCount);
		List<KeyboardRow> keyboard = new ArrayList<>(rowsCount);
		int buttonNum = 0;
		int totalButtons = texts.size();
		for (int i = 0; i < rowsCount; i++) {
			KeyboardRow row = new KeyboardRow();
			int iter = 0;
			for (int j = buttonNum; j < totalButtons; j++) {
				KeyboardButton btn = new KeyboardButton(texts.get(buttonNum));
				row.add(btn);
				buttonNum++;
				if (iter == columnCount - 1) {
					break;
				}
				iter++;
			}
			keyboard.add(row);
		}
		replyKeyboardMarkup.setKeyboard(keyboard);
		response.setReplyMarkup(replyKeyboardMarkup);
	}

}
