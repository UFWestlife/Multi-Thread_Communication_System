
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class ChatServer {
	public static void main(String args[]) {
		ServerSocket server = null;
		Socket you = null;
		Hashtable peopleList; // �����������߿ͻ���ͨ�ŵķ������̵߳�ɢ�б�
		peopleList = new Hashtable();


		while (true) {
			try {
				server = new ServerSocket(6666);
			} catch (IOException e1) {
				System.out.println("Listening...");
			}
			try {
				you = server.accept(); // �����Ϳͻ��˵����ӵ��׽���
				InetAddress address = you.getInetAddress();
				System.out.println("User IP��" + address);
			} catch (IOException e) {
			}
			if (you != null) {
				Server_thread peopleThread = new Server_thread(you, peopleList);
				peopleThread.start(); // ��ÿͻ���ͨ�ŵķ���������ʼ�����߳�

			} else {
				continue;
			}
		}
	}
}

class Server_thread extends Thread {
	String name = null, sex = null; // �����ߵ��ǳƺ��Ա�
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
			try {// �ȴ����������̣߳�ֱ���յ���Ϣ���ͻ��˷�������Ϣ
				s = in.readUTF();
				if (s.startsWith("Nickname:")) // ����û��ύ���ǳƺ��Ա�
				{
					name = s.substring(s.indexOf(":") + 1); // ��ȡ�û���Ϣ�е��ǳ�

					boolean exists = peopleList.containsKey(name); // ���ɢ�б����Ƿ����н������ǳƵ�������
					if (exists == false) {
						peopleList.put(name, this); // ����ǰ�߳���ӵ�ɢ�б��ǳ���Ϊ�ؼ���
						out.writeUTF("ChatSuccess:");
						Enumeration enum1 = peopleList.elements();

						while (enum1.hasMoreElements()) // ��ȡ���е���ͻ���ͨ�ŵķ������߳�
						{
							Server_thread th = (Server_thread) enum1.nextElement();// ����ǰ�����ߵ��ǳƺ��Ա�֪ͨ���е��û�
							th.out.writeUTF("AvailableUser:" + name); // Ҳ�����������ߵ�����֪ͨ���̣߳���ǰ�û���
							if (th != this) {
								out.writeUTF("AvailableUser:" + th.name);
							}
						}
					} else {// ������û��ǳ��Ѵ��ڣ���ʾ�û���������
						out.writeUTF("ChatFail:");
					}
				} else if (s.startsWith("PublicChat:")) {
					String message = s.substring(s.indexOf(":") + 1);
					Enumeration enum1 = peopleList.elements(); // ��ȡ���е���ͻ���ͨ�ŵķ������߳�
					while (enum1.hasMoreElements()) {
						((Server_thread) enum1.nextElement()).out.writeUTF("PublicChat:" + message);
					}
				}
				
				else if (s.startsWith("PublicFile:")) {
					String message = s.substring(s.indexOf(":") + 1);
					Enumeration enum1 = peopleList.elements(); // ��ȡ���е���ͻ���ͨ�ŵķ������߳�
					while (enum1.hasMoreElements()) {
						((Server_thread) enum1.nextElement()).out.writeUTF("PublicFile:" + message);
					}
				}
				
				
				else if (s.startsWith("UserExit:")) {
					Enumeration enum1 = peopleList.elements(); // ��ȡ���е���ͻ���ͨ�ŵķ������߳�
					while (enum1.hasMoreElements()) // ֪ͨ���������ߣ����û�������
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
					socket.close(); // �رյ�ǰ����
					System.out.println(name + " left the chat room.");
					break; // �������̵߳Ĺ������߳�����
				}

				else if (s.startsWith("PrivateChat:")) {
					String privateChatMessage = s.substring(s.indexOf(":") + 1, s.indexOf("#"));
					String toPeople = s.substring(s.indexOf("#") + 1);// �ҵ�Ҫ�������Ļ����߳�

					Server_thread toThread = (Server_thread) peopleList.get(toPeople);
					if (toThread != null) {
						toThread.out.writeUTF("PrivateChat:" + privateChatMessage);
					} else {// ֪ͨ��ǰ�û����������Ļ������Ѿ�������
						out.writeUTF("PrivateChat: " + toPeople + " is Offline.\n");
					}

				}
				else if (s.startsWith("BlockChat:")) {
					String blockmessage = s.substring(s.indexOf(":") + 1, s.indexOf("#"));
					Enumeration enum1 = peopleList.elements(); // ��ȡ���е���ͻ���ͨ�ŵķ������߳�
					String blockPeople = s.substring(s.indexOf("#") + 1);
					Server_thread toThread = (Server_thread) peopleList.get(blockPeople);// �ҵ���Ҫ���ε��߳�
					
					while (enum1.hasMoreElements()) {
						enum1.nextElement();
						if(enum1.equals(toThread))
							continue;
						((Server_thread) enum1.nextElement()).out.writeUTF("BlockCast:" + blockmessage);
					}
					
				}
				
				else if (s.startsWith("PrivateFile:")) {
					String privateChatMessage = s.substring(s.indexOf(":") + 1, s.lastIndexOf("#"));
					String toPeople = s.substring(s.lastIndexOf("#") + 1);// �ҵ�Ҫ�������Ļ����߳�

					Server_thread toThread = (Server_thread) peopleList.get(toPeople);
					if (toThread != null) {
						toThread.out.writeUTF("PrivateFile:" + privateChatMessage);
					} else {// ֪ͨ��ǰ�û����������Ļ������Ѿ�������
						out.writeUTF("PrivateChat: " + toPeople + " is Offline.\n");
					}

				}
				
			} catch (IOException ee) // �������߹ر��������������IOException
			{
				Enumeration enum1 = peopleList.elements(); // ��ȡ���е���ͻ���ͨ�ŵķ������߳�

				while (enum1.hasMoreElements()) // ֪ͨ���������ߣ����û�����
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
				try // �رյ�ǰ����
				{
					socket.close();
				} catch (IOException eee) {
				}
				System.out.println(name + " left.");
				break; // �������̵߳Ĺ������߳�����
			}

		}
	}

}
