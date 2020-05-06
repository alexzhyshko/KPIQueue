package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import model.User;

public class Bot extends TelegramLongPollingBot {

	private final String name = "kpiqueue228_bot";
	private final String token = "1169641439:AAGg2EvlSjYaUAM4DT5B5x07brfEb5s3QYQ";
	// users
	private HashMap<Integer, User> users = new HashMap<>();
	// queue
	private HashMap<Integer, User> queue = new HashMap<>();

	public void onUpdateReceived(Update update) {
		try {
			SendMessage response = new SendMessage();
			long chatid = update.getMessage().getChatId();
			int userid = update.getMessage().getFrom().getId();
			String username = update.getMessage().getFrom().getUserName();
			response.enableMarkdown(true);
			response.setChatId(Long.toString(chatid));
			String message = update.getMessage().getText();

			if (!users.containsKey(userid)) {
				users.put(userid, new User(username, userid, chatid));
			}

			User user = users.get(userid);

			switch (user.userstate) {
			case 0:
				response.setText("Привет, тут очередь");
				setButtons(Arrays.asList("Записаться в очередь", "Показать очередь", "Вычеркнуть меня из очереди",
						"Я сдал"), response);
				user.userstate = 1;
				break;
			case 1:
				if (message.equals("Записаться в очередь")) {
					int queuesize = 0;
					try {
						queuesize = queue.keySet().stream().mapToInt(e -> (int) e).max().getAsInt();
					} catch (Exception e) {
						//e.printStackTrace();
						System.out.println("Размер очереди = 0");
					}
					queuesize++;
					if (user.queue == -1) {
						queue.put(queuesize, user);
						user.queue = queuesize;
						response.setText("Записал, ты " + (queuesize) + " по счету");

					} else {
						response.setText("Ты уже есть в очереди");
					}
				} else if (message.equals("Показать очередь")) {
					String text = "";
					try {
						for (Entry e : queue.entrySet()) {
							text += e.getKey() + " @" + ((User) (e.getValue())).username + "\n";
						}
					} catch (Exception e0) {
						//e0.printStackTrace();
						System.out.println("В очереди пусто");
					}
					if (text.isBlank()) {
						text = "Очередь пустая";
					}
					response.setText(text);
				} else if (message.equals("Вычеркнуть меня из очереди")) {
					if (!queue.containsValue(user)) {
						response.setText("Тебя тут и так нету");
					} else {
						User temp = ((User) (queue.get(user.queue + 1)));
						queue.remove(user.queue);
						int queuenum = user.queue;
						int counter = queuenum;
						if (this.queue.size() > 0) {
							for (Entry e : this.queue.entrySet()) {
								if ((int) (e.getKey()) > counter) {
									if ((int) (e.getKey()) == 1) {
										queue.remove((int) (e.getKey()));
									} else {
										User t = queue.get((int) (e.getKey()));
										t.queue = t.queue-1;
										queue.put((int) (e.getKey()) - 1, t);
										queue.remove((int) (e.getKey()));
									}
									counter++;
								}
							}
						} else {
							queue.remove(counter);
						}
						user.queue = -1;

						SendMessage notific = new SendMessage();
						try {
							notific.enableMarkdown(true);
							notific.setChatId(temp.chatid);
							notific.setText("Твоя очередь сдавать");
							execute(notific);
						} catch (Exception e0) {
							e0.printStackTrace();
						}

						response.setText("Окей");

					}
				} else if (message.equals("Я сдал")) {
					if (!queue.containsValue(user)) {
						response.setText("Тебя и так нету в очереди");
					} else {
						User temp = ((User) (queue.get(user.queue + 1)));
						queue.remove(user.queue);
						int queuenum = user.queue;
						int counter = queuenum;
						if (this.queue.size() > 0) {
							for (Entry e : this.queue.entrySet()) {
								if ((int) (e.getKey()) > counter) {
									if ((int) (e.getKey()) == 1) {
										queue.remove((int) (e.getKey()));
									} else {
										User t = queue.get((int) (e.getKey()));
										t.queue = t.queue-1;
										queue.put((int) (e.getKey()) - 1, t);
										queue.remove((int) (e.getKey()));
									}
									counter++;
								}
							}
						} else {
							queue.remove(counter);
						}
						System.out.println("dsfsf");
						user.queue = -1;
						SendMessage notific = new SendMessage();
						try {
							notific.enableMarkdown(true);
							notific.setChatId(temp.chatid);
							notific.setText("Твоя очередь сдавать. Теперь ты первый");
							execute(notific);
						} catch (Exception e0) {
							//e0.printStackTrace();
							System.out.println("Перед пользователем нету других людей, сообщение отправлять не нужно");
						}

						response.setText("Молодец");
					}
				} else {
					System.out.println("Таких команд я не знаю");
				}
				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
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
