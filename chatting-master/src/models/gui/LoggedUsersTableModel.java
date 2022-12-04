package models.gui;

import models.chatClients.ChatClient;

import javax.swing.table.AbstractTableModel;

public class LoggedUsersTableModel extends AbstractTableModel { //Model tabulky, pro přihlášené uživatele
    private ChatClient chatClient;

    public LoggedUsersTableModel(ChatClient chatClient){
        this.chatClient = chatClient;
    }

    @Override
    public int getRowCount() {
        return chatClient.getLoggedUsers().size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) { //vrátí název uživatele v řádku
        return chatClient.getLoggedUsers().get(rowIndex);
    }

    @Override
    public String getColumnName(int column) {
        return "User";
    } //vrátí název sloupce(Username)
}
