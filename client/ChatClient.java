/*+----------------------------------------------------------------------
 || ELEC463 Lab 4 - Andre Al-Khoury - 26017029
 || Class: ChatClient
 || Purpose:  Main method of the chat client
 ++-----------------------------------------------------------------------*/

package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatClient {

    static Socket clientSocket;
    static BufferedReader inputFromServer;
    static DataOutputStream outputToServer;
    static String clientUsername;
    private static final char RECORD_SEPARATOR = 0x1e;
    private static final char ERROR = 0x1f;
    private static final char USERNAMELIST = 0x1d;
    private static boolean isConnected = false;


    public static void main(String[] args) throws Exception {


        ChatClientGUI ClientGUI = new ChatClientGUI();

        Thread listenForMessages = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isConnected) {
                    try {
                        String receivedMessage = inputFromServer.readLine();
                        if(receivedMessage.startsWith(Character.toString(ERROR))) {
                            isConnected = false;
                            ClientGUI.buttonConnectionToggle.setText("Connect");
                            ClientGUI.nameTextBox.setEnabled(true);
                            ClientGUI.sendButton.setEnabled(false);
                            ClientGUI.privateMessageUsernameTextBox.setEnabled(false);
                            ClientGUI.messageBox.setEnabled(false);
                            clientSocket.close();
                            outputToServer.close();
                            inputFromServer.close();
                            ClientGUI.console.setText(ClientGUI.console.getText() + "\n Disconnected from server.\n");
                            ClientGUI.listModel.clear();
                            ClientGUI.displayError(receivedMessage.substring(1));
                        }
                        else if(receivedMessage.startsWith(Character.toString(USERNAMELIST))) {
                            ClientGUI.listModel.clear();
                            String[] usernameList = receivedMessage.split(Character.toString(USERNAMELIST));
                            for(int i = 0; i < usernameList.length; i++) {
                                ClientGUI.listModel.addElement(usernameList[i]);
                            }

                        }
                        else if (!receivedMessage.isEmpty()) {
                            ClientGUI.console.setText(ClientGUI.console.getText() + receivedMessage + "\n");
                            ClientGUI.sp.getVerticalScrollBar().setValue(ClientGUI.sp.getVerticalScrollBar().getMaximum());
                        }
                    } catch (IOException ex) {
                    }
                }
            }
        });

        ClientGUI.setConnectButtonActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ClientGUI.nameTextBox.getText().isEmpty()) {
                    ClientGUI.displayError("Please enter a nickname.");
                } else {
                    try {
                        if (isConnected == false) {
                            try {
                                clientSocket = new Socket("localhost", 1337);
                            } catch (IOException ex) {
                                ClientGUI.displayError("Error connecting to the server.");
                            }
                            if (clientSocket.isConnected()) {
                                isConnected = true;
                                ClientGUI.buttonConnectionToggle.setText("Disconnect");
                                ClientGUI.nameTextBox.setEnabled(false);
                                ClientGUI.privateMessageUsernameTextBox.setEnabled(true);
                                ClientGUI.messageBox.setEnabled(true);
                                inputFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                outputToServer = new DataOutputStream(clientSocket.getOutputStream());
                                clientUsername = ClientGUI.nameTextBox.getText();
                                outputToServer.writeBytes(Character.toString(RECORD_SEPARATOR) + clientUsername + "\n");
                                outputToServer.flush();
                                new Thread(listenForMessages).start();
                            }
                        } else {
                            outputToServer.writeBytes("DISCONNECT" + Character.toString(RECORD_SEPARATOR) + "\n");
                            outputToServer.flush();
                            isConnected = false;
                            ClientGUI.buttonConnectionToggle.setText("Connect");
                            ClientGUI.nameTextBox.setEnabled(true);
                            ClientGUI.sendButton.setEnabled(false);
                            ClientGUI.privateMessageUsernameTextBox.setEnabled(false);
                            ClientGUI.messageBox.setEnabled(false);
                            ClientGUI.console.setText(ClientGUI.console.getText() + "\n" + "You have left the room. \n");
                            clientSocket.close();
                            outputToServer.close();
                            inputFromServer.close();
                            ClientGUI.listModel.clear();
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        });


        ClientGUI.setSendActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Send message
                String recipient = ClientGUI.privateMessageUsernameTextBox.getText();
                String message = ClientGUI.messageBox.getText();
                if (!message.isEmpty()) {
                    String sendString;
                    if (recipient.isEmpty()) {
                        sendString = "1" + Character.toString(RECORD_SEPARATOR) + clientUsername + Character.toString(RECORD_SEPARATOR) + message + "\n";
                    } else {
                        sendString = "2" + Character.toString(RECORD_SEPARATOR) + clientUsername + Character.toString(RECORD_SEPARATOR)
                                + ClientGUI.privateMessageUsernameTextBox.getText() + Character.toString(RECORD_SEPARATOR) + message + "\n";
                    }
                    try {
                        outputToServer.writeBytes(sendString);
                        outputToServer.flush();
                    } catch (IOException ex) {
                    }
                    ClientGUI.messageBox.setText("");
                }
            }
        });


    }

}
