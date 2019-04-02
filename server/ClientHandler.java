/*+----------------------------------------------------------------------
 || ELEC463 Lab 4 - Andre Al-Khoury - 26017029
 || Class: ClientHandler
 || Purpose:  Class that handles the on-going communication between server and client
 ++-----------------------------------------------------------------------*/

package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader inputFromClient;
    private DataOutputStream outputToClient;
    public String username;
    ConcurrentLinkedQueue<Message> incomingMessageQueue;
    public ConcurrentLinkedQueue<Message> outgoingMessageQueue = new ConcurrentLinkedQueue<Message>();
    public boolean isConnected;
    private final char RECORD_SEPARATOR = 0x1e;

    public ClientHandler(Socket socket, BufferedReader inputFromClient, DataOutputStream outputToClient, ConcurrentLinkedQueue<Message> incomingMessageQueue, String username) {
        this.socket = socket;
        this.incomingMessageQueue = incomingMessageQueue;
        this.inputFromClient = inputFromClient;
        this.outputToClient = outputToClient;
        this.username = username;
        isConnected = true;
    }

    public void run() {
        if(isConnected){
            String string = "You are connected! Welcome to Chat Server.\n";
            try{
                outputToClient.writeBytes(string);
            }
            catch(IOException ex) {}
        }

        //Thread that sends messages to clients
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isConnected) {
                    Message outgoingMessage = outgoingMessageQueue.poll();
                    if (outgoingMessage != null) {
                        String string;
                        if(outgoingMessage.sender == null) {
                            string = outgoingMessage.message;
                        }
                        else if (outgoingMessage.recipient == null) {
                            string = outgoingMessage.sender + ": " + outgoingMessage.message + "\n";
                        } else {
                            if(outgoingMessage.sender.equals(username)) {
                                string = "Private message to " + outgoingMessage.recipient + ": " + outgoingMessage.message + "\n";
                            }
                            else {
                                string = "Private message from " + outgoingMessage.sender + ": " + outgoingMessage.message + "\n";
                            }
                        }
                        try {
                            outputToClient.writeBytes(string);
                            outputToClient.flush();
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        }).start();

        while (isConnected) {
            //check for incoming messages or disconnection
            String line = null;
            try {
                if ((line = inputFromClient.readLine()) != null) {
                    if(line.equals("DISCONNECT" + Character.toString(RECORD_SEPARATOR))) {
                        incomingMessageQueue.add(new Message(username + " has left the room.\n"));
                        isConnected = false;
                    }
                    else {
                        Message incomingMsg = Message.decodeMessage(line);
                        incomingMessageQueue.add(incomingMsg);
                    }
                }
            } catch (IOException ex) {
            }
        }

    }

    public void kick() {
        isConnected = false;
    }
}
