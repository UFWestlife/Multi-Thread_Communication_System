
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;

public class InputNameArea extends Panel implements ActionListener, Runnable {
	TextField nameFile = null; // 用来输入用户昵称的文本条
	String name = null;
	Checkbox male = null, female = null; // 选择性别的单选框
	CheckboxGroup group = null;
	Button enterChatBtn = null, exitChatBtn = null;
	Socket socket = null; // 和服务器连接的套接字
	DataInputStream in = null; // 读取服务器发来的消息
	DataOutputStream out = null; // 向服务器发送消息
	Thread thread = null; // 负责读取服务器发来的信息的线程
	boolean chatStatus = false;
	HashMap listTable; // 存放在线聊天者昵称的散列表

	public InputNameArea(HashMap listTable2) {
		this.listTable = listTable2;
		nameFile = new TextField(10);
		enterChatBtn = new Button("Enter");
		exitChatBtn = new Button("Exit");
		enterChatBtn.addActionListener(this);
		exitChatBtn.addActionListener(this);
		thread = new Thread(this);
		add(new Label("Nickname: "));
		add(nameFile);
		add(enterChatBtn);
		add(exitChatBtn);
		exitChatBtn.setEnabled(false);
	}

	public void setChatStatus(boolean b) {
		chatStatus = b;
	}

	public boolean getChatStatus() {
		return chatStatus;
	}

	public String getName() {
		return name;
	}

	public void setName(String s) {
		name = s;
	}

	public void setSocketConnection(Socket socket, DataInputStream in, DataOutputStream out) {
		this.socket = socket;
		this.in = in;
		this.out = out;
		try {
			thread.start();
		} catch (Exception e) {
			nameFile.setText(" " + e);
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == enterChatBtn) {
			exitChatBtn.setEnabled(true);
			if (chatStatus == true) {
				nameFile.setText(name + "You are in the chat room now.");
			} else {
				this.setName(nameFile.getText());
				if (socket != null && name != null) {// 将用户信息发送给服务器端
					try {
						out.writeUTF("Nickname:" + name);
					} catch (IOException ee) {
						nameFile.setText("Chat server not found. " + ee);
					}
				}
			}
		}
		if (e.getSource() == exitChatBtn) {// 通知服务器用户已经离开
			try {
				out.writeUTF("UserExit:");
			} catch (IOException ee) {
			}
		}
	}

	public void run() {
		String message = null;
		while (true) {// 接受服务器发来的消息，并进行分析处理
			if (in != null) {
				try {
					message = in.readUTF();
				} catch (IOException e) {
					nameFile.setText("Disconnected from char server. " + e);
				}
			}
			if (message != null) {
				if (message.startsWith("ChatSuccess:")) {
					chatStatus = true;
					break;
				} else if (message.startsWith("AvailableUser:")) {
					String people = message.substring(message.indexOf(":") + 1); // 将目前在线的聊天者昵称添加到散列表中
					listTable.put(people, people);
				} else if (message.startsWith("ChatFail:")) {
					chatStatus = false;
					nameFile.setText("This nickname is used. Please try another nickname.");
				}
			}

		}
	}
}