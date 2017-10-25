
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.applet.*;
import java.util.HashMap;
import java.util.Hashtable;

public class ChatArea extends Panel implements ActionListener, Runnable {
	Socket socket = null; // 和服务器间建立的连接的套接字
	DataInputStream in = null; // 读取服务器信息的输入流
	DataOutputStream out = null; // 向服务器发送信息的输出流
	Thread threadMessage = null; // 读取服务器信息的线程
	TextArea publicArea, privateArea = null;
	TextField messageField = null;
	Button confirmBtn, sendFileBtn, refreshPublicAreaBtn, refreshPrivateAreaBtn, blockBtn;
	Label toLabel, blockLabel, tipLabel = null;
	String name = null; // 聊天者的昵称
	HashMap<String, Boolean> listTable; // 存放在线聊天者昵称的散列表
	List listComponent = null; // 显示在线聊天者昵称的的List组件
	Choice privateChatList; // 选择私人聊天者的下拉列表
	Choice blockChatList;// 选择屏蔽聊天者的下拉列表
	int width, height; // 聊天区的宽和高

	public ChatArea(String name, HashMap listTable, int width, int height) {
		setLayout(null);
		setBackground(Color.magenta);
		this.width = width;
		this.height = height;
		setSize(width, height);
		this.listTable = listTable;
		this.name = name;
		threadMessage = new Thread(this);
		publicArea = new TextArea(10, 10);
		privateArea = new TextArea(10, 10);
		toLabel = new Label("To:");
		blockLabel = new Label("Except:");
		confirmBtn = new Button("Send Message");
		sendFileBtn = new Button("Send File");
		refreshPublicAreaBtn = new Button("Refresh public");
		refreshPrivateAreaBtn = new Button("Refresh private");
		blockBtn = new Button("Block him");
		tipLabel = new Label("Double-click to unicast");
		messageField = new TextField(28);
		confirmBtn.addActionListener(this);
		sendFileBtn.addActionListener(this);
		messageField.addActionListener(this);
		refreshPublicAreaBtn.addActionListener(this);
		refreshPrivateAreaBtn.addActionListener(this);
		blockBtn.addActionListener(this);
		listComponent = new List();
		listComponent.addActionListener(this); // 双击列表中的聊天者的昵称，可选中与之私聊

		privateChatList = new Choice();
		privateChatList.add("Everyone");
		privateChatList.select(0); // 默认情况下，用户输入内容发送给所有聊天者
		
		blockChatList = new Choice();
		blockChatList.add("Nobody");
		blockChatList.select(0);
		

		add(publicArea);
		publicArea.setBounds(10, 10, (width - 400) / 2, (height - 150));
		add(privateArea);
		privateArea.setBounds(10 + (width - 200) / 2, 10, (width - 400) / 2, (height - 150));
		add(listComponent);
		listComponent.setBounds(10 + (width - 200), 10, 180, (height - 150));
		add(tipLabel);
		tipLabel.setBounds(10 + (width - 200), 10 + (height - 165), 180, 80);
		Panel pSouth = new Panel();
		pSouth.add(toLabel);
		pSouth.add(privateChatList);
		pSouth.add(blockLabel);
		pSouth.add(blockChatList);
		pSouth.add(messageField);
		pSouth.add(confirmBtn);
		pSouth.add(sendFileBtn);
		pSouth.add(refreshPublicAreaBtn);
		pSouth.add(refreshPrivateAreaBtn);
		pSouth.add(blockBtn);
		add(pSouth);
		pSouth.setBounds(10, 20 + (height - 100), width - 20, 60);
	}

	public void setName(String s) {
		name = s;
	}

	public void setSocketConnection(Socket socket, DataInputStream in, DataOutputStream out) {
		this.socket = socket;
		this.in = in;
		this.out = out;
		try // 启动线程，接受服务器信息
		{
			threadMessage.start();
		} catch (Exception e) {
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == confirmBtn || e.getSource() == messageField) {			
			String message = " ";
			String people = privateChatList.getSelectedItem();
			String blocker = blockChatList.getSelectedItem();
			// int tempIndex = people.lastIndexOf("[Banned]");
			// if (tempIndex > 0) {
			// people=people.substring(0,tempIndex);
			// } //获取信息发送对象的昵称

			message = messageField.getText();
			if (message.length() > 0) {// 将聊天的内容及对象发送给服务器
				try {
					if (people.equals("Everyone") && blocker.equals("Nobody")) {// broadcast
						out.writeUTF("PublicChat:" + name + " said:" + "\n\t" + message + "\n");
					} 
					else if (people != "Everyone" && blocker.equals("Nobody"))  {// unicast
						out.writeUTF("PrivateChat:" + name + " whispered:" + "\n\t" + message + "\n#" + people);
						privateArea.append("You wrispered " + people + ":\n\t" + message + "\n");
					}
					else if (people == "Everyone" && blocker != "Nobody")  {// blockcast
						out.writeUTF("BlockChat:" + name + " said:" + "\n\t" + message +  "\n#" + blocker);
						privateArea.append("You said to everyone, except " + blocker + ":\n\t" + message + "\n");
					}
	
				} 
				catch (IOException event) {
				}
			}
		}
		else if (e.getSource() == sendFileBtn) {
			String people = privateChatList.getSelectedItem();
			FileDialog fd = new FileDialog(new Frame(), "Open File", FileDialog.LOAD);
			try {
				fd.setVisible(true);
				String fileName = fd.getFile();
				FileInputStream in = new FileInputStream(fd.getDirectory() + fileName);
				int size = in.available();
				byte[] buffer = new byte[size];
				in.read(buffer);
				in.close();
				String fileUTF = new String(buffer, "UTF-8");
				
				if (people.equals("Everyone")) {
					out.writeUTF("PublicFile:" + name + ":" + fileName + ":" + fileUTF);
				} else {
					out.writeUTF("PrivateFile:" + name + ":" + fileName + ":" + fileUTF + "#" + people);
					publicArea.append("You sent a private file to " + people + ":\n\t" + fileName + "\n");
				}
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else if (e.getSource() == listComponent) {
			privateChatList.insert(listComponent.getSelectedItem(), 0);
			privateChatList.repaint();
		} 
		else if (e.getSource() == blockBtn) {
			blockChatList.insert(listComponent.getSelectedItem(), 0);
			blockChatList.repaint();
		}
		else if (e.getSource() == refreshPublicAreaBtn) {
			publicArea.setText(null);
		} 
		else if (e.getSource() == refreshPrivateAreaBtn) {
			privateArea.setText(null);
		}
	}

	public void run() {
		while (true) {
			String s = null;
			try {
				s = in.readUTF(); // 等待（阻塞本线程，直到收到信息）服务器信息
				if (s.startsWith("PublicChat:")) // 读取服务器发来的信息
				{
					String content = s.substring(s.indexOf(":") + 1);
					publicArea.append("\n" + content);
				}
				if (s.startsWith("BlockCast:")) // 读取服务器发来的信息
				{
					String content = s.substring(s.indexOf(":") + 1);
					publicArea.append("\n" + content);
				}
				if (s.startsWith("PublicFile:")) // 读取服务器发来的文件
				{
					String str = s.substring(s.indexOf(":") + 1);
					String senderName = str.substring(0, str.indexOf(":"));
					str = str.substring(str.indexOf(":") + 1);
					String fileName = str.substring(0, str.indexOf(":"));
					str = str.substring(str.indexOf(":") + 1);
					try {
						InputStream is = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
						publicArea.append(senderName + " sent a file:\n\t" + fileName + "\n");
						// 接收传输来的文件
						FileOutputStream fos = new FileOutputStream(fileName);
						int data;
						while ((data = is.read()) != -1) {
							fos.write(data);
						}
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (s.startsWith("PrivateChat:")) // 读取服务器发来的信息
				{
					String content = s.substring(s.indexOf(":") + 1);
					privateArea.append("\n" + content);
				}
				if (s.startsWith("PrivateFile:")) // 读取服务器发来的文件
				{
					String str = s.substring(s.indexOf(":") + 1);
					String senderName = str.substring(0, str.indexOf(":"));
					str = str.substring(str.indexOf(":") + 1);
					String fileName = str.substring(0, str.indexOf(":"));
					str = str.substring(str.indexOf(":") + 1);
					try {
						InputStream is = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
						privateArea.append(senderName + " sent a private file:\n\t" + fileName + "\n");
						// 接收传输来的文件
						FileOutputStream fos = new FileOutputStream(fileName);
						int data;
						while ((data = is.read()) != -1) {
							fos.write(data);
						}
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else if (s.startsWith("AvailableUser:")) {// 显示新加入的聊天者的信息
					String people = s.substring(s.indexOf(":") + 1);
					listTable.put(people, true);
					listComponent.add(people);
					listComponent.repaint(); // 刷新List组件，显示新用户昵称
				} else if (s.startsWith("UserExit:")) {// 删除已离线的聊天者信息
					String awayPeopleName = s.substring(s.indexOf(":") + 1);
					listComponent.remove(awayPeopleName);
					listComponent.repaint();
					publicArea.append("\n" + listTable.get(awayPeopleName).toString() + "Offline");
					listTable.remove(awayPeopleName);
				}
				Thread.sleep(5);
			} catch (IOException e) // 服务器关闭套接字连接时，导致IOException
			{
				listComponent.removeAll();
				listComponent.repaint();
				listTable.clear();
				publicArea.setText("Disconnected from chat server.\nPlease refresh to re-enter chat room.\n");
				break;
			} catch (InterruptedException e) {
			}
		}
	}
}