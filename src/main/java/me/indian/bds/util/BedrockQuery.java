package me.indian.bds.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;
import me.indian.bds.server.properties.Gamemode;

    /**
    Kod zaczerpnięty z
    https://github.com/justin-eckenweber/BedrockServerQuery/blob/main/src/main/java/me/justin/bedrockserverquery/data/BedrockQuery.java
     */

public record BedrockQuery(boolean online, String motd, int protocol, String minecraftVersion, int playerCount,
                           int maxPlayers, String mapName, Gamemode gamemode) {

    private static final byte IDUnconnectedPing = 0x01;
    private static final byte[] unconnectedMessageSequence = {0x00, (byte) 0xff, (byte) 0xff, 0x00, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfd, (byte) 0xfd, (byte) 0xfd, (byte) 0xfd, 0x12, 0x34, 0x56, 0x78};
    private static long dialerID = new Random().nextLong();

    public static BedrockQuery create(final String serverAddress, final int port) {
        try {
            final InetAddress address = InetAddress.getByName(serverAddress);

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeByte(IDUnconnectedPing);
            dataOutputStream.writeLong(System.currentTimeMillis() / 1000);
            dataOutputStream.write(unconnectedMessageSequence);
            dataOutputStream.writeLong(dialerID++);

            final byte[] requestData = outputStream.toByteArray();
            final byte[] responseData = new byte[1024 * 1024 * 4];

            final DatagramSocket socket = new DatagramSocket();
            final DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, address, port);
            socket.send(requestPacket);

            final DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);
            socket.setSoTimeout(2000);
            socket.receive(responsePacket);
            socket.close();

            final String[] splittedData = new String(responsePacket.getData(), 0, responsePacket.getLength()).split(";");

            return new BedrockQuery(true,
                    splittedData[1], Integer.parseInt(splittedData[2]), splittedData[3],
                    Integer.parseInt(splittedData[4]), Integer.parseInt(splittedData[5]),
                    splittedData[7], Gamemode.getByName(splittedData[8]));
        } catch (final Exception exception) {
            if (!(exception instanceof UnknownHostException) &&
                    !(exception instanceof SocketTimeoutException)) {
                exception.printStackTrace();
            }

            return new BedrockQuery(false, "", -1, "", 0, 0, "", Gamemode.SURVIVAL);
        }
    }
}