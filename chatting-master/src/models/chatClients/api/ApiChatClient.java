package models.chatClients.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import models.Message;
import models.chatClients.ChatClient;
import models.chatClients.LocalDateTimeDeserializer;
import models.chatClients.LocalDateTimeSerializer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//http://fimuhkpro22021.aspifyhost.cz/swagger/index.html
// Sbírka metod, sloužíčí k zjednodušení práce programátorovi, což v tomhle případě nemusíme řešit, od nuly jako login, logout, authorize, získání zpráv a získání přihlášených lidí
public class ApiChatClient implements ChatClient {
    private String loggedUser;
    private List<String> loggedUsers;
    private List<Message> messages;

    private List<ActionListener> listenersLoggedusersChanged = new ArrayList<>();
    private List<ActionListener> listenersMessagesChanged = new ArrayList<>();

    private final String BASE_URL = "http://fimuhkpro22021.aspifyhost.cz/";
    private String token;
    private Gson gson;

    public ApiChatClient() {  // construktor Apichatclientu, který inicializuje.
        loggedUsers = new ArrayList<>();
        messages = new ArrayList<>(); //inicializace listů(lepší pole, lépe se odstraňuje a přidává).
        GsonBuilder gsonB = new GsonBuilder() // slouží k práci s date, které jsem získal()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting(); // set user friendly file writing
        gsonB.registerTypeAdapter(LocalDateTime.class,new LocalDateTimeSerializer());
        gsonB.registerTypeAdapter(LocalDateTime.class,new LocalDateTimeDeserializer());
        gson = gsonB.create();
        Runnable refreshData = () ->{ // vytvoří vlákno na kterém běží metody pro refresh přihlášených uživatelů a zpráv
            Thread.currentThread().setName("RefreshData");
            try{
                while (true) {
                    if(isAuthenticated()){
                        refreshLoggedUsers();
                        refreshMessages();
                    }
                    TimeUnit.SECONDS.sleep(2); //uřčuje za jak dlouho se zopakuje cyklus.
                }
            }catch(Exception e){}
            refreshLoggedUsers();
        };
        Thread refreshDataThread = new Thread(refreshData);//inicializace vlákna
        refreshDataThread.start(); //zapne druhé vlákno od teď program běží na dalším vlákně.
    }

    @Override //slouží k poslání zprávy
    public void sendMessage(String text) {
        try{
            SendMessageRequest msgRequest = new SendMessageRequest(token ,text);
            String url = BASE_URL + "/api/Chat/SendMessage"; //na tuhle url se aplikace připojí
            HttpPost post = new HttpPost(url);
            StringEntity body = new StringEntity(gson.toJson(msgRequest),"utf-8"); //vytvoří instanci na dotaz pro vytvoření zprávy
            body.setContentType("application/json"); //String se convertuje na Json type.
            post.setEntity(body);

            CloseableHttpClient httpClient = HttpClients.createDefault(); //HTTP client na předání
            CloseableHttpResponse response = httpClient.execute(post); // metoda na předání

            if(response.getStatusLine().getStatusCode() == 204){
                refreshMessages();
            }

        }catch(Exception e){}
    }

    @Override //slouží k přihlašení uživatele
    public void login(String userName) {
        try{
            String url = BASE_URL + "/api/Chat/Login";
            HttpPost post = new HttpPost(url);
            StringEntity body = new StringEntity("\""+userName+"\"","utf-8");
            body.setContentType("application/json");
            post.setEntity(body);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(post);

            if(response.getStatusLine().getStatusCode() == 200){  // vrací zpětnou vazbu, že se user přihlásil
                token = EntityUtils.toString(response.getEntity()); // přiřazuje TOKEN neboli identitu k uživateli
                token = token.replace("\"", "").trim(); //Zkrácení tokenu nevim proč

                loggedUser = userName; //uloží do paměti username
                refreshLoggedUsers();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override // slouží k odhlášení uživatele, ale odesíláš token. Zbytek udělá api.
    public void logout() {
        try{
            String url = BASE_URL + "/api/Chat/Logout";
            HttpPost post = new HttpPost(url);
            StringEntity body = new StringEntity("\""+token+"\"","utf-8");
            body.setContentType("application/json");
            post.setEntity(body);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(post);

            if(response.getStatusLine().getStatusCode() == 204){
               token = null;
               loggedUser = null;

               loggedUsers = new ArrayList<>();
               raiseEventLoggedUsersChanged();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAuthenticated() {
        System.out.println("is authenticated: " + (loggedUser != null));
        return loggedUser != null;
    }

    @Override
    public List<String> getLoggedUsers() {
        return loggedUsers;
    }

    @Override
    public List<Message> getMessage() {
        return messages;
    }

    @Override
    public void addActionListenerLoggedUsersChanged(ActionListener toAdd) {
        listenersLoggedusersChanged.add(toAdd);
    }

    @Override
    public void addActionListenerMessagesChanged(ActionListener toAdd) {
        listenersMessagesChanged.add(toAdd);
    }

    private void raiseEventLoggedUsersChanged(){
        for (ActionListener al:
                listenersLoggedusersChanged) {
            al.actionPerformed(new ActionEvent(this,1, "usersChanged"));
        }
    }

    private void raiseEventMessagesChanged(){
        for (ActionListener al:
                listenersMessagesChanged) {
            al.actionPerformed(new ActionEvent(this,1, "messagesChanged"));
        }
    }

    private void addSystemMessages(int type, String author){
        messages.add(new Message(type,author));
        raiseEventMessagesChanged();
    }

    private void refreshLoggedUsers(){ //Refreshne přihlášené uživatele
        try {
            String url = BASE_URL + "/api/Chat/GetLoggedUsers";
            HttpGet get = new HttpGet(url);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(get);

            if(response.getStatusLine().getStatusCode() == 200){
                String resultJson = EntityUtils.toString(response.getEntity());

                loggedUsers = gson.fromJson(
                        resultJson,
                        new TypeToken<ArrayList<String>>(){}.getType()
                );
                raiseEventLoggedUsersChanged();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void refreshMessages(){ //Refreshne zprávy
        try {
            String url = BASE_URL + "/api/Chat/GetMessages";
            HttpGet get = new HttpGet(url);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(get);

            if(response.getStatusLine().getStatusCode() == 200){
                String resultJson = EntityUtils.toString(response.getEntity());

                messages = gson.fromJson(
                        resultJson,
                        new TypeToken<ArrayList<Message>>(){}.getType()
                );
                raiseEventMessagesChanged();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
