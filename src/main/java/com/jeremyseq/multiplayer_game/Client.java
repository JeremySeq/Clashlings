package main.java.com.jeremyseq.multiplayer_game;

import main.java.com.jeremyseq.multiplayer_game.client.App;

import java.io.*;
import java.net.Socket;

public class Client {
    // initialize socket and input output streams
    private Socket socket = null;
    private DataInputStream input = null;
    public DataOutputStream out = null;
    public DataInputStream server_response = null;
    public String username;

    // constructor to put ip address and port
    public Client(String address, int port)
    {
        // establish a connection
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // takes input from terminal
            input = new DataInputStream(System.in);
            server_response = new DataInputStream(socket.getInputStream());

            // sends output to the socket
            out = new DataOutputStream(
                    socket.getOutputStream());
        }
        catch (IOException i) {
            System.out.println(i);
            return;
        }


        try {
            System.out.print("Username: ");
            username = input.readLine();
            out.writeUTF("$init_connection:username=" + username);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Thread gameThread = new Thread(() -> {
            App.runApp(this);
        });
        gameThread.start();

        // string to read message from input
        String line = "";

        // keep reading until "Over" is input
        while (!line.equals("Over")) {
            try {
                line = input.readLine();

                out.writeUTF(line);
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }

        // close the connection
        try {
            input.close();
            out.close();
            socket.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) throws IOException {
        // Enter data using BufferReader
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        // Reading data using readLine
        String ip;
        if (args.length > 0 && args[0] != null) {
            ip = args[0];
        } else {
            System.out.print("IP Address: ");
            ip = reader.readLine();
        }
//        System.out.print("Port: ");
//        String port = reader.readLine();
        Client client = new Client(ip, 5000);
    }
}