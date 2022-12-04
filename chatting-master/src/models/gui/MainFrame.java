package models.gui;

import models.Message;
import models.chatClients.ChatClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {  //dědí od JFrame

    private ChatClient chatClient; //Konstruktor z ChatClient, abych se nemusel odkazovat na class.

    JTextArea txtChat;  //Zprávy a vypísi přihlášení
    JTextField txtInputMessage; //Píše se username

    public MainFrame(int width, int height, ChatClient chatClient) {
        super("PRO2 2022 ChatClient skB");
        setSize(width, height);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.chatClient = chatClient;

        initGui();  //Inicializuje vzhled
        setVisible(true);
    }

    private void initGui() {
        JPanel panelMain = new JPanel(new BorderLayout()); //BoxLayout, GridLayout.
        panelMain.add(initLoginPanel(), BorderLayout.NORTH);  //Vytvoří další panel na kterém jsou ovládací prvky
        panelMain.add(initChatPanel(),BorderLayout.CENTER);
        panelMain.add(initMessagePanel(),BorderLayout.SOUTH);
        panelMain.add(initLoggedUsersPanel(),BorderLayout.EAST);
        add(panelMain);
    }
    private JPanel initLoginPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); //přidání ovládacích prvků
        panel.add(new JLabel("Username"));
        JTextField txtInputUsername = new JTextField("",30);//konstruktor, nastavení textu a max délka
        panel.add(txtInputUsername);
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(new ActionListener() {  //zjišťuje jestli se neprovedla nějaké operace(delegát)
            @Override
            public void actionPerformed(ActionEvent e) {  //
                String userName = txtInputUsername.getText();
                System.out.println("clicked - " + userName); //Píše hlášku o přihlášení do systémové konzole
                if (chatClient.isAuthenticated()){ //Kontroluje Authentikaci
                    //logout
                    chatClient.logout();
                    btnLogin.setText("Login");
                    txtInputUsername.setEditable(true);
                    txtChat.setEnabled(false);
                    txtInputMessage.setEnabled(false);
                } else {
                    //login
                    if (userName.length()<1) { //zkontroluje zda je textové pole prázdné, když ne error massage
                        JOptionPane.showMessageDialog(null, "Enter you username","Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    chatClient.login(userName);
                    btnLogin.setText("Logout");
                    txtInputUsername.setEditable(false); //zabrání v editaci username
                    txtChat.setEnabled(true); //zapne txtChat
                    txtInputMessage.setEnabled(true);
                }
            }
        });
        panel.add(btnLogin);

        return panel;
    }

    private JPanel initChatPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
        txtChat = new JTextArea();
        txtChat.setEditable(false);
        txtChat.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(txtChat);
        panel.add(scrollPane);

        /*for (int i = 0; i < 50; i++) {
            txtChat.append("Message: " + i + "\n");
        }*/
        chatClient.addActionListenerMessagesChanged(e -> { //přidá ActionListenery
            refreshMessages();
        });

        return panel;
    }

    private JPanel initLoggedUsersPanel() {
        JPanel panel = new JPanel();

        /*Object[][] data = new Object[][]{
                {"0,0","0,1"},
                {"1,0","1,1"},
                {"aaa","bbb"}
        };
        String[] colNames = new String[]{"Col1", "Col2"};

        JTable tblLoggedUsers = new JTable(data, colNames); */

        JTable tblLoggedUsers = new JTable();
        LoggedUsersTableModel loggedUsersTableModel = new LoggedUsersTableModel(chatClient);
        tblLoggedUsers.setModel(loggedUsersTableModel);

        JScrollPane scrollPane = new JScrollPane(tblLoggedUsers);
        scrollPane.setPreferredSize(new Dimension(250,500)); //Dimension je jako konstruktor, ale můžeš nastavit šířku a výšku
        panel.add(scrollPane);

        chatClient.addActionListenerLoggedUsersChanged(e -> {
            loggedUsersTableModel.fireTableDataChanged();
        });

        return panel;
    }

    private JPanel initMessagePanel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtInputMessage = new JTextField("",50);
        txtInputMessage.setEnabled(false);
        panel.add(txtInputMessage);
        JButton btnSendMessage = new JButton("Send");
        btnSendMessage.addActionListener(e -> {
            String msgText = txtInputMessage.getText();
            System.out.println("btn send clicked - " + msgText);

            //txtChat.append(txtInputMessage.getText() + "\n");

            if (msgText.length() == 0) {  //kontrola délky textu
                return;
            }
            if (!chatClient.isAuthenticated()) { //kontrola authentikace
                return;
            }
            chatClient.sendMessage(msgText);
            txtInputMessage.setText("");
            refreshMessages();
        });
        panel.add(btnSendMessage);

        return panel;
    }
    private void refreshMessages(){ //refreshuje zprávy
        txtChat.setText("");
        if (!chatClient.isAuthenticated())
            return;
        for (Message msg:
             chatClient.getMessage()) {
            txtChat.append(msg.toString());
            txtChat.append("\n");
        }
    }
}
