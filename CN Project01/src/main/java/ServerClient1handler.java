import java.io.*;
import java.net.*;
import java.util.logging.Logger;

//handler basically used for handle the input/output  and it is use for server auto/manual reply to the client 



//it's used for as a bodyguard for one connected user handle their msgs, errors and connection
public class ServerClient1handler implements Runnable {
    private final Socket clientSocket;
    private final Server1 server;
    private DataInputStream dis;
    private DataOutputStream dout;
    private String clientName;
    private static final Logger logger = Logger.getLogger(ServerClient1handler.class.getName());

    
    
    //set up a client handler with their connection and server link
    public ServerClient1handler(Socket socket, Server1 server) {
        this.clientSocket = socket;
        this.server = server;
    }
    
    
    
    
    //it's used for manage the all messgaes in the chat room 
    @Override
    public void run() {
        try {
            dis = new DataInputStream(clientSocket.getInputStream());
            dout = new DataOutputStream(clientSocket.getOutputStream());

            clientName = dis.readUTF();
            server.broadcast(clientName + " has joined the chat", this);
            sendMessage("You are connected as " + clientName);
            
             server.appendToLog(clientName + " joined the chat.");

            String message;
           while (!(message = dis.readUTF()).equalsIgnoreCase("exit")) {
    if (message.startsWith("/private ")) {
        String privateMsg = message.substring(9).trim();
        sendMessage(clientName + ":" + privateMsg);
        server.appendToLog(clientName + ": "+ privateMsg);
    } else {
        server.appendToLog(clientName + ": " + message);
        server.broadcast(clientName + ": " + message, this);
    }

    
    
    
    //used for auto response and handle the disconnect of client 
    String response = generateAutoResponse(message);
    if (response != null) {
        sendMessage("Server: " + response);
        server.appendToLog("Server: " + response);
    }
}
        } catch (IOException e) {
            logger.warning("Client error: " + e.getMessage());
        } finally {
            cleanupClient();
        }
    }

    
    
    
    
    
    
    //auto response to the client by using (if , return) condition
    private String generateAutoResponse(String msg) {
        msg = msg.toLowerCase();
        if (msg.contains("hi") || msg.contains("hello")) return "Hello " + clientName + "!";
        if (msg.contains("how are you")) return "i'm a server, always running!";
        if (msg.contains("hello server")) return "hey client how are you!";
        if (msg.contains("who are you?")) return "i'm a server always available for you";
        if (msg.contains("i'm m bilal")) return "i have not any specific name but you can call me any name";
        if (msg.contains("english alphabet")) return "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z : Total Alphabet is : 26";
        if (msg.contains("vowel")) return "A E I O U  : Total Vowel is : 5";
        if (msg.contains("consonent")) return "B C D F G H J K L M N P Q R S T V W X Y Z : Total Consonent is : 21";
        if (msg.contains("thanks") || msg.contains("thank you")) return "You're welcome.";
        return null;
    }

    
    
    
    //used for deliver a msg to the client
    public void sendMessage(String message) throws IOException {
        dout.writeUTF(message);
        dout.flush();
    }

    
    
    
    //handle client disconnection cleanly
    private void cleanupClient() {
        try {
            server.appendToLog(clientName + " disconnected.");
            server.removeClient(this);
            clientSocket.close();
        } catch (IOException e) {
            logger.warning("Cleanup error: " + e.getMessage());
        }
    }

    
    //get a client network connection
    public Socket getClientSocket() {
        return clientSocket;
    }

    
    //get a client Name 
    public String getClientName() {
        return clientName;
    }
}
