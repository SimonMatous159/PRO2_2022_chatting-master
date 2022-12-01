package models.database;

import models.Message;

import java.sql.*;
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

    @Override  //získání zprávy (Select getMassage)
    public List<Message> getMessage() {
        return null;
    }
}
