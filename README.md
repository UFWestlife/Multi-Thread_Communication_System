# Multi-Thread_Communication_System
README<br>

Chat Server & Client<br>
Course Project of CNT5106C, Fall 2016<br>
by Ke Jin (5965-8460) and Zun Wang (6151-0196)<br>

Contributions:<br>
1. Basic framework of the applet: Ke Jin<br>
2. Broadcast: Ke Jin<br>
3. Unicast: Zun Wang<br>
4. Support for transmitting files: Ke Jin<br>
5. Blockcast: Zun Wang<br>
6. UI: Zun Wang<br>

Build Environment:<br>
OS: Windows 10<br>
Java Version: JRE 1.8.0.71<br>
IDE: Eclipse MARS.1<br>

Setup:<br>
1. First, run the server (ChatServer). There's no UI for server but it will output some log onto the console.<br><br>

2. Run chat client. If the connection is successful, there will be a connection success message in red on the top-right of the window.<br><br>

3. Enter a nickname and click "Enter" button. If success, you'll be able to chat now.<br><br>

4. You can open multiple chat client then. Note that the nickname can't be same. You will get a warning if you try to login with a nickname that already taken.<br><br>

5. To broadcast, select "Everyone" in the "To" selection menu (it's default). Type message you want to send, and click "Send Message".<br><br>

6. To unicast, double click the user you want to private chat and its name will be available in the "To" menu and selected already. Type the message and click "Send Message". If you want to send to this same user later, you can either double-click him or pick him in the "To" menu.<br><br>

7. To blockcast, single-click the user you want to block and click the "Block this user" button. Its name will be in the "Except" menu and selected. You can then do broadcast or unicast with this user blocked. To unblock, open the "Except" menu and select "Nobody".<br><br>

8. For any of broadcast, unicast or blockcast, you can send a file as well. After setting up "To" and "Except", click "Send File" to open the file you want to send. Note that the file is transformed into UTF-8 String and sent through server, so some special file format (i.e. ZIP format) may not be properly transmitted.<br><br>

9. You can read the message that sent to you by other user's broadcast on the left chat box, and private messages on the right one. When receiving a file, you will see a notification on the corresponding chat box and the file will be in the working directory of the client applet.<br><br>
