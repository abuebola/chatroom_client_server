/*+----------------------------------------------------------------------
 || ELEC463 Lab 4 - Andre Al-Khoury - 26017029
 || Class: ChatServerGUI
 || Purpose:  GUI of the chat server
 ++-----------------------------------------------------------------------*/


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChatServerGUI {

    /* DECLARATIONS */
    private static JFrame frame;
    private static JPanel panel;
    public static JLabel clientsConnected;
    public static JList usernameList;
    public static DefaultListModel listModel;
    public static JButton kickButton;

    public ChatServerGUI() {

        frame = new JFrame();
        panel = new JPanel();
        clientsConnected = new JLabel();
        kickButton = new JButton("Kick user");

        frame.setSize(new Dimension(250, 470));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Chat Server");
        frame.setResizable(false);

        clientsConnected = new JLabel("No client connected.");
        clientsConnected.setLayout(null);
        clientsConnected.setBounds(50, 30, 150, 20);
        panel.add(clientsConnected);

        kickButton.setLayout(null);
        kickButton.setBounds(60, 380, 100, 30);
        kickButton.setEnabled(false);
        panel.add(kickButton);

        /* JLIST */
        listModel = new DefaultListModel();
        usernameList = new JList(listModel);
        usernameList.setLayout(null);
        usernameList.setBounds(50, 60, 130, 300);

        usernameList.addListSelectionListener (new ListSelectionListener()
        {

            public void valueChanged (ListSelectionEvent e)
            {
                boolean enable = !usernameList.isSelectionEmpty();
                kickButton.setEnabled(enable);
            }

        });
        usernameList.addMouseListener(new MouseAdapter() {

            int lastSelectedIndex;

            public void mouseClicked(MouseEvent e) {

                int index = usernameList.locationToIndex(e.getPoint());

                if (index != -1 && index == lastSelectedIndex) {
                    usernameList.clearSelection();
                }
                lastSelectedIndex = usernameList.getSelectedIndex();
            }
        });
        panel.add(usernameList);

        panel.setLayout(null);
        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);


    }

    public static void setKickActionListener(ActionListener al) {
        kickButton.addActionListener(al);
    }



}
