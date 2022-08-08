package Threads;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SocketThread extends Thread {
    JLabel socketStateLabel;
    AsynchronousServerSocketChannel server;
    public SocketThread(JLabel label) {

        this.socketStateLabel = label;
    }

    public void run() {

        try {
            SwingUtilities.invokeAndWait(() -> {
                try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
                    server.bind(new InetSocketAddress("127.0.0.1", 1234));
                    Future<AsynchronousSocketChannel> acceptCon = server.accept();
                    AsynchronousSocketChannel client = acceptCon.get(10, TimeUnit.SECONDS);
                    if ((client != null) && (client.isOpen())) {

                        SwingUtilities.invokeAndWait(() -> socketStateLabel.setText("Opened"));

                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        Future<Integer> readVal = client.read(buffer);
                        System.out.println("Received from client: " + new String(buffer.array()).trim());
                        readVal.get();
                        buffer.flip();
                        String str = "I'm fine. Thank you!";
                        Future<Integer> writeVal = client.write(ByteBuffer.wrap(str.getBytes()));
                        System.out.println("Writing back to client: " + str);
                        writeVal.get();
                        buffer.clear();
                    }
                    client.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }


    }
}
