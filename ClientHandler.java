import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER : " + clientUsername + " has entered the chat.\nThere are now "+ClientHandler.clientHandlers.size()+" users in chat.");
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
    }

    // Overridden method
    public void run() {
        String messageFromClient;

        while(socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
                System.out.println(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for(ClientHandler clientHandler : clientHandlers) {
            try {
                if(!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER : " + clientUsername + " has left the chat.\n"+clientHandlers.size()+" users are left.");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if(bufferedReader != null)  { bufferedReader.close(); }
            if(bufferedWriter != null)  { bufferedWriter.close(); }
            if(socket != null)          { socket.close(); }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public static String getName(int index) {
        ClientHandler user = clientHandlers.get(index);
        String username = user.clientUsername;
        return username;
    }
    public static String getAllNames() {
        int counter = 0;
        String nameList = "";
        ClientHandler user;
        String name;
        
        while(counter < clientHandlers.size()) {
            user = clientHandlers.get(counter);
            name = user.clientUsername;
            nameList += name;
            if(!(counter == (clientHandlers.size()-1))) { nameList += ", "; }
            counter++;
        }
        return nameList;
    }
}