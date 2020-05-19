package main;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import model.Controller;
import model.Notification;
import model.Queue;
import model.User;

public class Bot extends TelegramLongPollingBot {

	private final String name = "kpiqueue228_bot";
	private final String token = "1169641439:AAGg2EvlSjYaUAM4DT5B5x07brfEb5s3QYQ";
	// session
	private HashMap<Integer, HashMap<String, Object>> sessions = new HashMap<>();

	private Controller controller = new Controller();

	int weekNum = LocalDate.now().getDayOfYear() / 7;

	

	public void onUpdateReceived(Update update) {
		try {

			SendMessage response = new SendMessage();
			long chatid = update.getMessage().getChatId();
			int userid = update.getMessage().getFrom().getId();
			String username = update.getMessage().getFrom().getUserName();
			String name = update.getMessage().getFrom().getFirstName();
			String surname = update.getMessage().getFrom().getLastName();
			response.setChatId(Long.toString(chatid));
			String message = update.getMessage().getText();
			if (sessions.get(userid) == null) {
				sessions.put(userid, new HashMap<String, Object>());
			}
			
			
			HashMap<String, Object> session = sessions.get(userid);

			User user = controller.getUser(userid);
			
			if(message.equals("/start")) {
				response.setText("Choose queue");
				List<String> btn = controller.getQueues().stream().map(e -> e.name).collect(Collectors.toList());
				setButtons(btn, response);
				controller.setUserState(user, 1);
			}
			
			switch (controller.getUserState(user)) {
			case 0:
				try {
					controller.createUser(username, name, surname, userid, chatid);
					user = controller.getUser(userid);
					response.setText("Choose queue");
					List<String> btn = controller.getQueues().stream().map(e -> e.name).collect(Collectors.toList());
					btn.add("Add queue");
					setButtons(btn, response);
					controller.setUserState(user, 1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 1:
				if (message.equals("Add queue")) {
					response.setText("Enter name");
					setButtons(Arrays.asList("Back"), response);
					controller.setUserState(user, 3);
				} else if (message.equals("Back")) {
					response.setText("Choose queue");
					List<String> btns = controller.getQueues().stream().map(e -> e.name).collect(Collectors.toList());
					btns.add("Add queue");
					setButtons(btns, response);
					controller.setUserState(user, 1);
				} else {
					List<String> names = controller.getQueues().stream().map(e -> e.name).collect(Collectors.toList());
					if (names.contains(message)) {
						session.put("queuename", message);
						response.setText("Ok, what's doin'");
						setButtons(
								Arrays.asList("Sign in", "Show queue", "Remove me", "I passed", "Delete queue", "Back"),
								response);
						controller.setUserState(user, 2);
					} else {
						response.setText("No such queue");
					}
				}

				break;
			case 2:
				Queue queue = null;
				for (Queue e : controller.getQueues()) {
					if (e.name.equals(session.get("queuename"))) {
						queue = (Queue) e;
						break;
					}
				}
				if (queue == null) {
					response.setText("No such queue anymore, choose another");
					response.setText("Choose queue");
					List<String> buttons = controller.getQueues().stream().map(e -> e.name)
							.collect(Collectors.toList());
					setButtons(buttons, response);
					controller.setUserState(user, 1);
					break;
				}
				List<User> stack = controller.getUsersInQueue(queue);
				if (message.equals("Back")) {
					response.setText("Choose queue");
					List<String> buttons = controller.getQueues().stream().map(e -> e.name)
							.collect(Collectors.toList());
					buttons.add("Add queue");
					setButtons(buttons, response);
					controller.setUserState(user, 1);
				} else if (message.equals("Sign in")) {
					boolean twoWeek = queue.twoWeek;
					boolean evenweek = queue.evenweek;
					setButtons(Arrays.asList("Sign in", "Show queue", "Remove me", "I passed", "Delete queue", "Back"),
							response);
					LocalDateTime heroku = LocalDateTime.now();
					LocalDateTime urkaine = heroku.plusHours(3);
					LocalDate ukraineDate = urkaine.toLocalDate();
					LocalTime ukraineTime = urkaine.toLocalTime();
					if (twoWeek) {
						if (weekNum % 2 == 0 && evenweek) {
							if (ukraineTime.isAfter(LocalTime.of(18, 0, 0)) && (ukraineDate.getDayOfWeek().compareTo(queue.day.minus(2)) == 0)
									|| (ukraineDate.getDayOfWeek().compareTo(queue.day.minus(2)) > 0 && ukraineDate.getDayOfWeek().compareTo(queue.day) < 0)
									|| (ukraineDate.getDayOfWeek().compareTo(queue.day) == 0 && ukraineTime.isBefore(LocalTime.of(queue.hour + 1, queue.minute + 30, 0)))) 
							{

								if (controller.addUserToQueue(user, queue)) {
									response.setText("Ok");
								} else {
									response.setText("You are already in queue");
								}
							} else {
								response.setText("Signing in is locked");
							}
						} else if (weekNum % 2 == 1 && !evenweek) {
							if (ukraineTime.isAfter(LocalTime.of(18, 0, 0)) && (ukraineDate.getDayOfWeek().compareTo(queue.day.minus(2)) == 0)
									|| (ukraineDate.getDayOfWeek().compareTo(queue.day.minus(2)) > 0 && ukraineDate.getDayOfWeek().compareTo(queue.day) < 0)
									|| (ukraineDate.getDayOfWeek().compareTo(queue.day) == 0 && ukraineTime.isBefore(LocalTime.of(queue.hour + 1, queue.minute + 30, 0)))) 
							{

								if (controller.addUserToQueue(user, queue)) {
									response.setText("Ok");
								} else {
									response.setText("You are already in queue");
								}
							} else {
								response.setText("Signing in is locked");
							}
						} else if((weekNum % 2 == 1 && evenweek) || (weekNum % 2 == 0 && !evenweek)){
							if((LocalTime.now().isAfter(LocalTime.of(18, 0, 0))
									&& (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY) && queue.day.equals(DayOfWeek.MONDAY)) 
									|| LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY) && queue.day.equals(DayOfWeek.MONDAY))) 
							{
								if (controller.addUserToQueue(user, queue)) {
									response.setText("Ok");
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
						if (ukraineTime.isAfter(LocalTime.of(18, 0, 0)) && (ukraineDate.getDayOfWeek().compareTo(queue.day.minus(2)) == 0)
								|| (ukraineDate.getDayOfWeek().compareTo(queue.day.minus(2)) > 0 && ukraineDate.getDayOfWeek().compareTo(queue.day) < 0)
								|| (ukraineDate.getDayOfWeek().compareTo(queue.day) == 0 && ukraineTime.isBefore(LocalTime.of(queue.hour + 1, queue.minute + 30, 0)))) 
						{

							if (controller.addUserToQueue(user, queue)) {
								response.setText("Ok");
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
					List<User> users = stack;
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
					// TODO remove user from queue
					Notification notific = controller.removeUserFromQueue(user, queue);
					if (notific != null) {
						if (notific.needToNotify && notific.user!=null) {
							SendMessage not = new SendMessage();
							not.enableMarkdown(true);
							not.setChatId(notific.user.chatid);
							not.setText("You are first now in queue: " + queue.name + ", It's your turn");
							execute(not);
						}
						response.setText("Ok");
					} else {
						response.setText("You are not in queue");
					}
				} else if (message.equals("I passed")) {
					setButtons(Arrays.asList("Sign in", "Show queue", "Remove me", "I passed", "Delete queue", "Back"),
							response);
					// TODO remove user from queue
					Notification notific = controller.removeUserFromQueue(user, queue);
					if (notific != null) {
						if (notific.needToNotify && notific.user!=null) {
							SendMessage not = new SendMessage();
							not.enableMarkdown(true);
							not.setChatId(notific.user.chatid);
							not.setText("You are first now in queue: " + queue.name + ", It's your turn");
							execute(not);
						}
						response.setText("Ok");
					} else {
						response.setText("You are not in queue");
					}
				} else if (message.equals("Delete queue")) {
					if (controller.getCreator(queue).equals(user)) {
						controller.removeQueue(queue);
						response.setText("Deleted");
						List<String> buttons = controller.getQueues().stream().map(o -> o.name)
								.collect(Collectors.toList());
						buttons.add("Add queue");
						setButtons(buttons, response);
						controller.setUserState(user, 1);
						break;
					} else {
						response.setText("You are not allowed to delete this queue");
					}

				} else {
					response.setText("Don't know such commands");
				}
				break;
			case 3:
				if (message.equals("Back")) {
					response.setText("Choose queue");
					List<String> buttons = controller.getQueues().stream().map(e -> e.name)
							.collect(Collectors.toList());
					buttons.add("Add queue");
					setButtons(buttons, response);
					controller.setUserState(user, 1);
				} else {
					boolean queueExists = false;
					for (Queue e : controller.getQueues()) {
						if (e.name.equals(message)) {
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
						controller.setUserState(user, 4);
					}
				}
				break;
			case 4:
				if (message.equals("Back")) {
					response.setText("Enter name");
					setButtons(Arrays.asList("Back"), response);
					controller.setUserState(user, 3);
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
						controller.setUserState(user, 5);
					}
				}
				break;
			case 5:
				if (message.equals("Back")) {
					List<String> buttons = Arrays.asList(DayOfWeek.values()).stream().map(e -> e.name())
							.collect(Collectors.toList());
					buttons.add("Back");
					response.setText("Choose day");
					setButtons(buttons, response);
					controller.setUserState(user, 4);
				} else {
					if (!Arrays.asList("Once a week", "Once two weeks").contains(message)) {
						response.setText("This queue exists");
					} else {
						session.put("newqueuerepeat", message);
						if (message.equals("Once two weeks")) {
							setButtons(Arrays.asList("Even", "Odd"), response);
							response.setText("Choose week on which to repeat");
							controller.setUserState(user, 6);
						} else {
							response.setText("Enter time (HH:mm)");
							controller.setUserState(user, 7);

						}
					}
				}
				break;
			case 6:
				if (Arrays.asList("Even", "Odd").contains(message)) {
					session.put("newqueueweek", message);
					response.setText("Enter time (HH:mm)");
					controller.setUserState(user, 7);

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
							session.get("newqueueweek").equals("Even"), user.userid,
							(String) session.get("newqueuename"), message);
				} else {
					q = new Queue(DayOfWeek.valueOf((String) session.get("newqueueday")),
							session.get("newqueuerepeat").equals("Once two weeks"), false, user.userid,
							(String) session.get("newqueuename"), message);
				}
//				creators.put(q, userid);
//				queues.put(q, new Stack());
//				first.put(q.name, null);
				q.creator_id = userid;
				controller.createQueue(q);
				List<String> buttons = controller.getQueues().stream().map(e -> e.name).collect(Collectors.toList());
				buttons.add("Add queue");
				setButtons(buttons, response);
				controller.setUserState(user, 1);
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
		response.enableMarkdown(false);
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
