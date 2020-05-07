package main;

import java.util.ArrayList;
import java.util.Arrays;
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

import model.Stack;
import model.User;

public class Bot extends TelegramLongPollingBot {

	private final String name = "kpiqueue228_bot";
	private final String token = "1169641439:AAGg2EvlSjYaUAM4DT5B5x07brfEb5s3QYQ";
	// users
	private HashMap<Integer, User> users = new HashMap<>();
	// queue
	private HashMap<String, Stack> queues = new HashMap<>();
	// session
	private HashMap<Integer, HashMap<String, Object>> sessions = new HashMap<>();
	// queue first
	private HashMap<String, User> first = new HashMap<>();

	public Bot() {
		try {
			new Thread(() -> {
				try {
					while (true) {
						for (Entry e : queues.entrySet()) {
							Stack st = (Stack) e.getValue();
							String name = (String) e.getKey();
							User u = st.getFirst();
							if (first.get(name) == null) {
								if (u != null) {
									first.put(name, u);
									SendMessage not = new SendMessage();
									not.enableMarkdown(true);
									not.setChatId(u.chatid);
									not.setText("You are first now in queue: "+name+", It's your turn");
									execute(not);
								}
							} else if (!first.get(name).equals(u)) {
								if (u != null) {
									first.put(name, u);
									SendMessage not = new SendMessage();
									not.enableMarkdown(true);
									not.setChatId(u.chatid);
									not.setText("You are first now in queue: "+name+", It's your turn");
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
			response.enableMarkdown(true);
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
				List<String> btn = queues.keySet().stream().collect(Collectors.toList());
				btn.add("Add queue");
				btn.add("Back");
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
					List<String> btns = queues.keySet().stream().collect(Collectors.toList());
					btns.add("Add queue");
					btns.add("Back");
					setButtons(btns, response);
					user.userstate = 1;
				} else {
					if (queues.containsKey(message)) {
						session.put("queuename", message);
						response.setText("Ok, what's doin'");
						setButtons(
								Arrays.asList("Sign in", "Show queue", "Remove me", "I passed", "Back"),
								response);
						user.userstate = 2;
					} else {
						response.setText("No such queue");
					}
				}

				break;
			case 2:
				Stack queue = queues.get(session.get("queuename"));
				if (message.equals("Back")) {
					response.setText("Choose queue");
					List<String> buttons = queues.keySet().stream().collect(Collectors.toList());
					buttons.add("Add queue");
					setButtons(buttons, response);
					user.userstate = 1;
				} else if (message.equals("Sign in")) {
					if (queue.push(user)) {
						response.setText("Ok, you are " + (queue.indexOf(user) + 1) + " in the queue");
					} else {
						response.setText("You are already in queue");
					}
				} else if (message.equals("Show queue")) {
					
					List<User> users = queue.getAll();
					System.out.println(users);
					String text = "";
					for (int i = 0; i < users.size(); i++) {
						text += (i + 1) + " - " + users.get(i).getName() + "\n";
					}
					if (text.isBlank()) {
						text = "Queue empty";
					}
					response.setText(text);
				} else if (message.equals("Remove me")) {
					if (queue.remove(user)) {
						if(first.get((String)session.get("queuename")).equals(user))
							first.put((String)session.get("queuename"), null);
						response.setText("Ok");
					} else {
						response.setText("You are not in queue");
					}
				} else if (message.equals("I passed")) {
					if (queue.remove(user)) {
						if(first.get((String)session.get("queuename")).equals(user))
							first.put((String)session.get("queuename"), null);
						response.setText("Nice");
					} else {
						response.setText("You are not in queue");
					}
				} else {
					System.out.println("Don't know such commands");
				}
				break;
			case 3:
				if (message.equals("Back")) {
					response.setText("Choose queue");
					List<String> buttons = queues.keySet().stream().collect(Collectors.toList());
					buttons.add("Add queue");
					setButtons(buttons, response);
					user.userstate = 1;
				} else {
					if (queues.containsKey(message)) {
						response.setText("This queue exists");
					} else {
						queues.put(message, new Stack());
						response.setText("Ok");
						first.put(message, null);
						List<String> buttons = queues.keySet().stream().collect(Collectors.toList());
						buttons.add("Add queue");
						setButtons(buttons, response);
						user.userstate = 1;
					}
				}
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
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboard(false);
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
