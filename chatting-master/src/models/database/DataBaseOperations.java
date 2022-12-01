package models.database;

import models.Message;

import java.util.List;

public interface DataBaseOperations {  //Interface pro operace s databázema
    void addMessage(Message message);
    List<Message> getMessage();
}
