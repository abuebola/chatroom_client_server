/*+----------------------------------------------------------------------
 || ELEC463 Lab 4 - Andre Al-Khoury - 26017029
 || Class: ChatClientGUI
 || Purpose:  GUI of the chat client.
 ++-----------------------------------------------------------------------*/

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;

public class ChatClientGUI {

    /* DECLARATIONS */
    private JFrame frame;
    private JPanel panel;
    public JTextPane console;
    public JScrollPane sp;
    private JLabel nameLabel;
    private JLabel TCPClientLabel;
    public JButton buttonConnectionToggle;
    public JTextField nameTextBox;
    public JTextField messageBox;
    public JButton sendButton;
    private JLabel privateMessageLabel;
    public JTextField privateMessageUsernameTextBox;
    private JList usernameList;
    public DefaultListModel listModel;

    public ChatClientGUI() {

        frame = new JFrame();
        panel = new JPanel();
        console = new JTextPane();
        nameLabel = new JLabel("Enter your nickname:");
        TCPClientLabel = new JLabel("<html><font color='green' size='5'>WhatsApp Messenger</font></html>");
        buttonConnectionToggle = new JButton("Connect");
        nameTextBox = new JTextField();
        messageBox = new JTextField();
        sendButton = new JButton("Send");
        privateMessageLabel = new JLabel("Send private message to:");
        privateMessageUsernameTextBox = new JTextField();



        /* FRAME */
        frame.setSize(new Dimension(650, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Chat Client");
        frame.setResizable(false);

        /* CHAT CLIENT TITLE */
        TCPClientLabel.setLayout(null);
        TCPClientLabel.setBounds(20, 10, 300, 80);
        panel.add(TCPClientLabel);


        /* NAME LABEL */
        nameLabel.setLayout(null);
        nameLabel.setBounds(360, 20, 150, 20);
        panel.add(nameLabel);

        /* NAME TEXT BOX */
        nameTextBox.setLayout(null);
        nameTextBox.setBounds(490, 20, 120, 20);
        panel.add(nameTextBox);

        /*CONNECT BUTTON*/
        buttonConnectionToggle.setLayout(null);
        buttonConnectionToggle.setBounds(505, 50, 100, 30);
        panel.add(buttonConnectionToggle);

        /* CONSOLE */
        console.setEditable(false);
        sp = new JScrollPane(console);
        sp.setBounds(20, 100, 450, 300);
        panel.add(sp);


        /* JLIST */
        listModel = new DefaultListModel();
        usernameList = new JList(listModel);
        usernameList.setLayout(null);
        usernameList.setBounds(490, 100, 130, 300);

        usernameList.addListSelectionListener (new ListSelectionListener()
        {

            public void valueChanged (ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting()) {
                    privateMessageUsernameTextBox.setText(usernameList.getSelectedValue().toString());
                }
            }

        });
        usernameList.addMouseListener(new MouseAdapter() {

            int lastSelectedIndex;

            public void mouseClicked(MouseEvent e) {

                int index = usernameList.locationToIndex(e.getPoint());

                if (index != -1 && index == lastSelectedIndex) {
                    usernameList.clearSelection();
                    privateMessageUsernameTextBox.setText("");
                }

                lastSelectedIndex = usernameList.getSelectedIndex();
            }
        });
        panel.add(usernameList);


        /* PRIVATE MESSAGE LABEL */
        privateMessageLabel.setLayout(null);
        privateMessageLabel.setBounds(20, 450, 150, 20);
        panel.add(privateMessageLabel);

        /* PRIVATE MESSAGE USERNAME TEXT BOX */
        privateMessageUsernameTextBox.setLayout(null);
        privateMessageUsernameTextBox.setBounds(180, 450, 120, 20);
        privateMessageUsernameTextBox.setEnabled(false);
        panel.add(privateMessageUsernameTextBox);

        /* MESSAGE BOX */
        messageBox.setLayout(null);
        messageBox.setBounds(20, 500, 500, 25);
        messageBox.setEnabled(false);
        panel.add(messageBox);

        /*SEND BUTTON*/
        sendButton.setLayout(null);
        sendButton.setBounds(530, 497, 100, 30);
        sendButton.setEnabled(false);
        panel.add(sendButton);
        Document textFieldDoc = messageBox.getDocument();
        textFieldDoc.addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updated(e);
            }
            public void insertUpdate(DocumentEvent e) {
                updated(e);
            }
            public void removeUpdate(DocumentEvent e) {
                updated(e);
            }
            private void updated(DocumentEvent e) {
                boolean enable = e.getDocument().getLength() > 0;
                sendButton.setEnabled(enable);
            }
        });

        /*PANEL*/
        panel.setLayout(null);
        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);



    }

    public void setConnectButtonActionListener(ActionListener al) {
        buttonConnectionToggle.addActionListener(al);
        nameTextBox.addActionListener(al);
    }

    public void setSendActionListener(ActionListener al) {
        sendButton.addActionListener(al);
        messageBox.addActionListener(al);
    }

    public void displayError(String errorMessage){
        JOptionPane.showMessageDialog(null, errorMessage);
    }

}
