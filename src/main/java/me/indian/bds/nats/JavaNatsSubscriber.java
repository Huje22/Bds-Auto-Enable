package me.indian.bds.nats;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class JavaNatsSubscriber {

    private static final String temat = "k";

    public static void main(String[] args) throws Exception {
        final Connection nc = Nats.connect();
        try {
                receive(nc);


//            send(nc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void send(Connection nc) throws Exception {
        String message = "Hello from Java!";
        nc.publish(temat, message.getBytes());
        System.out.println("Message sent successfully");
    }

    public static void receive(Connection nc) throws Exception {
        Dispatcher d = nc.createDispatcher((msg) -> {
            String str = new String(msg.getData(), StandardCharsets.UTF_8);
            System.out.println(str);
        });
        d.subscribe(temat);
    }
}
