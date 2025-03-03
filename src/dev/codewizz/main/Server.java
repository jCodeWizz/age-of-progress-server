package dev.codewizz.main;

import dev.codewizz.main.utils.ByteUtils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {

    private static final int SERVER_PORT = 25565;

    private final int MAX_PACKET_SIZE = 1024;
    private final byte[] receivedDataBuffer = new byte[MAX_PACKET_SIZE * 10];

    private DatagramSocket socket;
    public Thread listenThread;
    private boolean connected;

    private final Database database;

    public Server() {
        if (!connect()) {
            System.err.println("Could NOT start listening for Clients");
        } else {
            System.out.println("Listening...");
        }

        database = new Database();
        database.open();
        database.setup();
    }

    private void process(DatagramPacket packet) {
        byte[] data = packet.getData();

        if (data[0] == -52) {
            byte[] usernameBytes = new byte[16];
            System.arraycopy(data, 1, usernameBytes, 0, usernameBytes.length);

            String username = new String(usernameBytes, StandardCharsets.US_ASCII).trim();
            String ip = packet.getAddress().toString().replaceAll("([^0-9.])", "");
            database.insertPlayer(username, ip);

            System.out.println("New connection!");

            return;
        } else if (data[0] == -53) {
            byte[] file = new byte[packet.getLength() - 1];
            System.arraycopy(data, 1, file, 0, file.length);
            String fileName = new String(file, StandardCharsets.US_ASCII).trim();
            sendFile(fileName, packet.getAddress(), packet.getPort());

            return;
        }

        System.err.println("Received code: " + data[0]);
    }

    private void sendFile(String fileName, InetAddress address, int port) {

        try {
            String[] filePath = fileName.split("/");

            File file = new File("res/" + filePath[filePath.length - 1]);

            Path path = Paths.get(file.getAbsolutePath());
            byte[] data = Files.readAllBytes(path);

            // send file header packet.
            byte[] headerData = new byte[5];
            headerData[0] = -54;
            System.arraycopy(ByteUtils.toBytes(data.length), 0, headerData, 1, 4);
            send(headerData, address, port);

            int offset = 0;
            int index = 0;
            while (offset < data.length) {
                int size = Math.min(data.length - offset + 5, MAX_PACKET_SIZE);
                byte[] chunk = new byte[size];

                chunk[0] = -55;
                System.arraycopy(ByteUtils.toBytes(index), 0, chunk, 1, 4);
                System.arraycopy(data, offset, chunk, 5, size - 5);
                offset += size - 5;
                index++;
                send(chunk, address, port);
            }
        } catch (IOException e) {
            System.err.println("couldn't read file: " + e);
        }
    }


    private boolean connect() {
        try {
            socket = new DatagramSocket(SERVER_PORT);
        } catch (SocketException e) {
            System.err.println("Couldn't create socket: " + e);
            connected = false;
            return false;
        }

        listenThread = new Thread(this::listen, "packet-handler");
        listenThread.start();

        connected = true;
        return true;
    }

    private void listen() {
        while (connected) {
            DatagramPacket packet = new DatagramPacket(receivedDataBuffer, MAX_PACKET_SIZE);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                if (e.getMessage() != null && e.getMessage().equals("Socket closed")) {
                    System.err.println("Socket was closed!");
                } else {
                    System.err.println("Faulty packet received, dropping...");
                }
            }
            if (packet.getData().length > 0 && connected) {
                process(packet);
            }
        }
        stop();
    }

    public void send(byte[] data, InetAddress address, int port) {
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Couldn't send data: " + e);
        }
    }

    public void stop() {
        connected = false;
        database.close();
        socket.close();
    }
}