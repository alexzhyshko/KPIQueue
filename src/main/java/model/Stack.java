package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Stack {

	private List<User> stack = new ArrayList<>();

	public boolean push(User u) {
		if (stack.contains(u)) {
			return false;
		} else {
			stack.add(u);
			return true;
		}
	}

	public boolean remove(User u) {
		if (stack.contains(u)) {
			List<User> temp = stack.subList(stack.indexOf(u) + 1, stack.size()).stream().collect(Collectors.toList());
			stack.subList(stack.indexOf(u), stack.size()).clear();
			stack.addAll(temp);
			return true;
		} else {
			return false;
		}
	}

	public int indexOf(User u) {
		return stack.indexOf(u);
	}

	public List<User> getAll() {
		return this.stack.stream().collect(Collectors.toList());
	}

	public User getFirst() {
		if (this.stack.size()!=0)
			return this.stack.get(0);
		else
			return null;
	}

}
