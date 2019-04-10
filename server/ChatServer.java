/*+----------------------------------------------------------------------
 || ELEC463 Lab 4 - Andre Al-Khoury - 26017029
 || Class: ChatServer
 || Purpose:  Main method of the chat server.
 ++-----------------------------------------------------------------------*/


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.sleep;

public class ChatServer {

    private static ServerSocket welcomeSocket;
    private static ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>(); //shared by all the clients. ClientHandlers add to it and ChatServer forwards messages to the appropriate ClientHandler
    private static HashMap<String, ClientHandler> clients = new HashMap<String, ClientHandler>();

    //Non-printable ASCII characters to use in our protocol
    private static final char RECORD_SEPARATOR = 0x1e;  //separates message fields (sender, recipient, message)
    private static final char ERROR = 0x1f; //indicate that the message is an error msg sent to the client
    private static final char USERNAMELIST = 0x1d;  //indicate that the message is the list of usernames

    public static void main(String[] args) {

        ChatServerGUI ChatGUI = new ChatServerGUI();

        //Thread that listens for connections
        new Thread(new Runnable() {
            @Override
            public void run() {
                welcomeSocket = null;
                try {
                    welcomeSocket = new ServerSocket(1337);
                } catch (IOException ex) {
                }
                if (welcomeSocket != null) {
                    listenForConnections();
                }
            }
        }).start();

        //Thread that transfers messages to ClientHandlers
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Message message = messageQueue.poll();
                    if (message != null) {
                        String recipient = message.recipient;
                        String sender = message.sender;
                        synchronized (clients) {
                            if (recipient != null) {
                                for (ClientHandler c : clients.values()) {
                                    if (c.username.equals(recipient) || c.username.equals(sender)) {
                                        c.outgoingMessageQueue.add(message);
                                    }
                                }
                            } else {
                                for (ClientHandler c : clients.values()) {
                                    c.outgoingMessageQueue.add(message);
                                }
                            }
                        }
                    }
                }
            }
        }).start();


        //Thread that counts clients and maintains list of connected clients to send to the users
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                    }


                    synchronized (clients) {
                        boolean someUserHasDisconnected = false;
                        if (!clients.isEmpty()) {
                            ArrayList<String> found = new ArrayList<String>();
                            for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
                                if(!entry.getValue().isConnected) {
                                    found.add(entry.getKey());
                                    someUserHasDisconnected = true;
                                }
                            }

                            for(String s : found) {
                                clients.remove(s);
                            }

                            if(someUserHasDisconnected) {
                                sendListOfUsernames();
                                updateClientsList();
                            }
                            int number = clients.size();
                            if (number == 0) {
                                ChatGUI.clientsConnected.setText("No client connected.");
                            } else {
                                ChatGUI.clientsConnected.setText(clients.size() + " clients connected.");
                            }
                        }
                    }
                }
            }
        }).start();

        ChatServerGUI.setKickActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                kickClient(ChatServerGUI.usernameList.getSelectedValue().toString());
            }
        });
    }


    private static void listenForConnections() {

        while (true) {
            try {
                Socket connectionSocket = welcomeSocket.accept();   //blocking call
                BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outputToClient = new DataOutputStream(connectionSocket.getOutputStream());
                String receivedString = inputFromClient.readLine();
                if (!receivedString.isEmpty()) {
                    //when connected, the first thing the client sends is its username
                    if (receivedString.startsWith(Character.toString(RECORD_SEPARATOR))) {
                        String username = receivedString.substring(1);
                        boolean usernameExists = false;
                        synchronized (clients) {
                            usernameExists = clients.containsKey(username);
                        }
                        if (usernameExists) {
                            outputToClient.writeBytes(Character.toString(ERROR) + "Nickname already exists. Please choose another nickname.\n");
                            outputToClient.flush();
                            connectionSocket.close();
                            outputToClient.close();
                            inputFromClient.close();
                        } else {
                            ClientHandler c = new ClientHandler(connectionSocket, inputFromClient, outputToClient, messageQueue, username);
                            Thread t = new Thread(c);
                            t.start();
                            t.setName(username);
                            synchronized (clients) {
                                for (ClientHandler x : clients.values()) {
                                    x.outgoingMessageQueue.add(new Message(username + " has entered the room.\n"));
                                }
                                clients.put(username, c);
                                updateClientsList();
                                sendListOfUsernames();
                            }

                        }
                    }
                }

            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    private static void sendListOfUsernames() {
        String usernamelist = "";
        for (String s : clients.keySet()) {
            usernamelist = usernamelist + Character.toString(USERNAMELIST) + s;
        }
        if (usernamelist.length() > 1) {
            for (ClientHandler x : clients.values()) {
                x.outgoingMessageQueue.add(new Message(usernamelist + "\n"));
            }
        }
    }

    public static void kickClient(String username) {
        synchronized (clients) {

            for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
                if(!entry.getKey().equals(username)) {
                    entry.getValue().outgoingMessageQueue.add(new Message(username + " has been kicked out.\n"));
                }
                else {
                    entry.getValue().outgoingMessageQueue.add(new Message(Character.toString(ERROR) + "You have been kicked out.\n"));
                }
            }
            clients.get(username).kick();
        }
    }

    //updates the list of clients in the jlist
    public static void updateClientsList() {
        ChatServerGUI.listModel.clear();
        for(String s : clients.keySet())
            ChatServerGUI.listModel.addElement(s);
        }
    }
