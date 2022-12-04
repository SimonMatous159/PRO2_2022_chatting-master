package models.database;

import models.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcDatabaseOperations implements DataBaseOperations{ //přidání a získání zpráv z databáze.
    private final Connection connection;

    public JdbcDatabaseOperations(String driver, String url) throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        this.connection = DriverManager.getConnection(url);
    }


    @Override
    public void addMessage(Message message) { //přidání zprávy
        try {
            String sql =
                    "INSERT INTO ChatMessages (author, text, created) "
                        + "VALUES ("
                            + "'" + message.getAuthor() + "',"
                            + "'" + message.getText() + "',"
                            + "'" + Timestamp.valueOf(message.getCreated()) + "',"
                        + ");";
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Message> getMessage() { //získání zprávy (Select getMassage)
        try {
            String sql = "SELECT * from ChatMessages;";
            ResultSet rst;
            Connection connection = null;
            Statement statement = connection.createStatement();
            rst = statement.executeQuery(sql);
            statement.close();
            ArrayList<Message> messages = new ArrayList<>();
            while (rst.next()) {
                Message msg = new Message(((ResultSet) rst).getString("author"), rst.getString("text"));
                messages.add(msg);
            }
            return messages;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
