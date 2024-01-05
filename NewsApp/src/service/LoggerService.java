package service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.spec.RSAOtherPrimeInfo;

public final class LoggerService {
    public static LoggerService shared = new LoggerService();

    public void setupLogger(String logMessage) {
        String loggerIpAddress = "192.168.30.13";

        try (Socket socket = new Socket(loggerIpAddress, 9700);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            // Send log message to logger
            oos.writeObject(logMessage);
        } catch (IOException e) {
            // Handle exception
//            logger.warning("Failed to send log message to logger: " + e.getMessage());
        }
    }

    public void start() throws IOException, ClassNotFoundException {
        int loggerPort = 9700; // Choose a port for the logger service

        System.out.println("start");

        ServerSocket serverSocket = new ServerSocket(loggerPort);
        while (true) {
            System.out.println("start2");

            Socket clientSocket = serverSocket.accept();
            System.out.println("start3");

            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            System.out.println("socket done");

            // Receive and print log message
            String message = (String) ois.readObject();

            // Check if the received message is a trigger to start
            if ("start".equals(message)) {
                // Start processing when the trigger is received
                // You can call any method or perform any action you need
                System.out.println("Received trigger message. Starting processing...");
            } else {
                // Handle other messages or log them
                System.out.println("Received message: " + message);
            }
        }
    }
}

