
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;

public class InputNameArea extends Panel implements ActionListener, Runnable {
	TextField nameFile = null; // ���������û��ǳƵ��ı���
	String name = null;
	Checkbox male = null, female = null; // ѡ���Ա�ĵ�ѡ��
	CheckboxGroup group = null;
	Button enterChatBtn = null, exitChatBtn = null;
	Socket socket = null; // �ͷ��������ӵ��׽���
	DataInputStream in = null; // ��ȡ��������������Ϣ
	DataOutputStream out = null; // �������������Ϣ
	Thread thread = null; // �����ȡ��������������Ϣ���߳�
	boolean chatStatus = false;
	HashMap listTable; // ��������������ǳƵ�ɢ�б�

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
				if (socket != null && name != null) {// ���û���Ϣ���͸���������
					try {
						out.writeUTF("Nickname:" + name);
					} catch (IOException ee) {
						nameFile.setText("Chat server not found. " + ee);
					}
				}
			}
		}
		if (e.getSource() == exitChatBtn) {// ֪ͨ�������û��Ѿ��뿪
			try {
				out.writeUTF("UserExit:");
			} catch (IOException ee) {
			}
		}
	}

	public void run() {
		String message = null;
		while (true) {// ���ܷ�������������Ϣ�������з�������
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
					String people = message.substring(message.indexOf(":") + 1); // ��Ŀǰ���ߵ��������ǳ���ӵ�ɢ�б���
					listTable.put(people, people);
				} else if (message.startsWith("ChatFail:")) {
					chatStatus = false;
					nameFile.setText("This nickname is used. Please try another nickname.");
				}
			}

		}
	}
}