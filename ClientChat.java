
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.applet.*;

public class ClientChat extends Applet implements Runnable {
	public static final int DEFAULT_WEIDH = 1220;
	public static final int DEFAULT_HEIGHT = 520;
	Socket socket = null;
	DataInputStream in = null;
	DataOutputStream out = null;
	InputNameArea inputNameArea = null;
	ChatArea charArea = null;
	HashMap listTable; // 存放在线聊天者昵称的散列表
	Label statusLabel;
	Panel north, center;
	Thread thread;

	public void init() {
		setSize(DEFAULT_WEIDH, DEFAULT_HEIGHT);
		 int width=getSize().width; //获取java applet小程序的宽
		 int height=getSize().height; //获取java applet小程序的高
//		int width = 1200;
//		int height = 500;

		listTable = new HashMap();
		setLayout(new BorderLayout());
		inputNameArea = new InputNameArea(listTable);
		int h = inputNameArea.getSize().height;
		charArea = new ChatArea("", listTable, width, height - (h + 5));
		charArea.setVisible(true);
		statusLabel = new Label("Connecting to server, please wait...", Label.CENTER);
		statusLabel.setForeground(Color.red);
		north = new Panel(new FlowLayout(FlowLayout.LEFT));
		center = new Panel();
		north.add(inputNameArea);
		north.add(statusLabel);
		center.add(charArea);
		add(north, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		validate();
	}

	public void start() // 清除此前的套接字
	{
		if (socket != null && in != null && out != null) {
			try {
				socket.close();
				in.close();
				out.close();
				charArea.setVisible(false);
			} catch (Exception ee) {
			}
		}
		try {
			socket = new Socket(this.getCodeBase().getHost(), 6666);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ee) {
			statusLabel.setText("Connection failed.");
		}
		if (socket != null) // 如果连接成功，则提示用户输入昵称
		{
			InetAddress address = socket.getInetAddress();
			statusLabel.setText("Connection to: " + address + " success");
			inputNameArea.setSocketConnection(socket, in, out);
			north.validate();
		}
		if (thread == null) // 为该用户启动一个新线程
		{
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop() {
		try {
			socket.close();
			thread = null;
		} catch (IOException e) {
			this.showStatus(e.toString());
		}
	}

	public void run() {
		while (thread != null) {
			if (inputNameArea.getChatStatus() == true) {
				charArea.setVisible(true);
				charArea.setName(inputNameArea.getName());
				charArea.setSocketConnection(socket, in, out);
				statusLabel.setText("Have a nice chat!");
				center.validate();
				break;
			}
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
	}
}
