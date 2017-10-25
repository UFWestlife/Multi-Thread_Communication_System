
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class ChatServer {
	public static void main(String args[]) {
		ServerSocket server = null;
		Socket you = null;
		Hashtable peopleList; // 存放与各聊天者客户端通信的服务器线程的散列表
		peopleList = new Hashtable();


		while (true) {
			try {
				server = new ServerSocket(6666);
			} catch (IOException e1) {
				System.out.println("Listening...");
			}
			try {
				you = server.accept(); // 建立和客户端的连接的套接字
				InetAddress address = you.getInetAddress();
				System.out.println("User IP：" + address);
			} catch (IOException e) {
			}
			if (you != null) {
				Server_thread peopleThread = new Server_thread(you, peopleList);
				peopleThread.start(); // 与该客户端通信的服务器，开始启动线程

			} else {
				continue;
			}
		}
	}
}

class Server_thread extends Thread {
	String name = null, sex = null; // 聊天者的昵称和性别
	Socket socket = null;
	File file = null;
	DataOutputStream out = null;
	DataInputStream in = null;
	Hashtable peopleList = null;

	Server_thread(Socket t, Hashtable list) {
		peopleList = list;
		socket = t;
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
		}
	}

	public void run() {
		while (true) {
			String s = null;
			try {// 等待（阻塞本线程，直到收到信息）客户端发来的信息
				s = in.readUTF();
				if (s.startsWith("Nickname:")) // 如果用户提交了昵称和性别
				{
					name = s.substring(s.indexOf(":") + 1); // 截取用户信息中的昵称

					boolean exists = peopleList.containsKey(name); // 检查散列表中是否已有叫做该昵称的聊天者
					if (exists == false) {
						peopleList.put(name, this); // 将当前线程添加到散列表，昵称作为关键字
						out.writeUTF("ChatSuccess:");
						Enumeration enum1 = peopleList.elements();

						while (enum1.hasMoreElements()) // 获取所有的与客户端通信的服务器线程
						{
							Server_thread th = (Server_thread) enum1.nextElement();// 将当前聊天者的昵称和性别通知所有的用户
							th.out.writeUTF("AvailableUser:" + name); // 也将其他聊天者的姓名通知本线程（当前用户）
							if (th != this) {
								out.writeUTF("AvailableUser:" + th.name);
							}
						}
					} else {// 如果该用户昵称已存在，提示用户重新输入
						out.writeUTF("ChatFail:");
					}
				} else if (s.startsWith("PublicChat:")) {
					String message = s.substring(s.indexOf(":") + 1);
					Enumeration enum1 = peopleList.elements(); // 获取所有的与客户端通信的服务器线程
					while (enum1.hasMoreElements()) {
						((Server_thread) enum1.nextElement()).out.writeUTF("PublicChat:" + message);
					}
				}
				
				else if (s.startsWith("PublicFile:")) {
					String message = s.substring(s.indexOf(":") + 1);
					Enumeration enum1 = peopleList.elements(); // 获取所有的与客户端通信的服务器线程
					while (enum1.hasMoreElements()) {
						((Server_thread) enum1.nextElement()).out.writeUTF("PublicFile:" + message);
					}
				}
				
				
				else if (s.startsWith("UserExit:")) {
					Enumeration enum1 = peopleList.elements(); // 获取所有的与客户端通信的服务器线程
					while (enum1.hasMoreElements()) // 通知其他聊天者，该用户已离线
					{
						try {
							Server_thread th = (Server_thread) enum1.nextElement();
							if (th != this && th.isAlive()) {
								th.out.writeUTF("UserExit:" + name);
							}
						} catch (IOException eee) {
						}
					}
					if (name != null)
						peopleList.remove(name);
					socket.close(); // 关闭当前连接
					System.out.println(name + " left the chat room.");
					break; // 结束本线程的工作，线程死亡
				}

				else if (s.startsWith("PrivateChat:")) {
					String privateChatMessage = s.substring(s.indexOf(":") + 1, s.indexOf("#"));
					String toPeople = s.substring(s.indexOf("#") + 1);// 找到要接受悄悄话的线程

					Server_thread toThread = (Server_thread) peopleList.get(toPeople);
					if (toThread != null) {
						toThread.out.writeUTF("PrivateChat:" + privateChatMessage);
					} else {// 通知当前用户，接受悄悄话的人已经离线了
						out.writeUTF("PrivateChat: " + toPeople + " is Offline.\n");
					}

				}
				else if (s.startsWith("BlockChat:")) {
					String blockmessage = s.substring(s.indexOf(":") + 1, s.indexOf("#"));
					Enumeration enum1 = peopleList.elements(); // 获取所有的与客户端通信的服务器线程
					String blockPeople = s.substring(s.indexOf("#") + 1);
					Server_thread toThread = (Server_thread) peopleList.get(blockPeople);// 找到需要屏蔽的线程
					
					while (enum1.hasMoreElements()) {
						enum1.nextElement();
						if(enum1.equals(toThread))
							continue;
						((Server_thread) enum1.nextElement()).out.writeUTF("BlockCast:" + blockmessage);
					}
					
				}
				
				else if (s.startsWith("PrivateFile:")) {
					String privateChatMessage = s.substring(s.indexOf(":") + 1, s.lastIndexOf("#"));
					String toPeople = s.substring(s.lastIndexOf("#") + 1);// 找到要接受悄悄话的线程

					Server_thread toThread = (Server_thread) peopleList.get(toPeople);
					if (toThread != null) {
						toThread.out.writeUTF("PrivateFile:" + privateChatMessage);
					} else {// 通知当前用户，接受悄悄话的人已经离线了
						out.writeUTF("PrivateChat: " + toPeople + " is Offline.\n");
					}

				}
				
			} catch (IOException ee) // 当聊天者关闭浏览器，将导致IOException
			{
				Enumeration enum1 = peopleList.elements(); // 获取所有的与客户端通信的服务器线程

				while (enum1.hasMoreElements()) // 通知其他聊天者，该用户离线
				{
					try {
						Server_thread th = (Server_thread) enum1.nextElement();
						if (th != this && th.isAlive()) {
							th.out.writeUTF("UserExit:" + name);
						}
					} catch (IOException eee) {
					}
				}
				if (name != null)
					peopleList.remove(name);
				try // 关闭当前连接
				{
					socket.close();
				} catch (IOException eee) {
				}
				System.out.println(name + " left.");
				break; // 结束本线程的工作，线程死亡
			}

		}
	}

}
