import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ClientUI extends JFrame{
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel bodyPanel;
    private JPanel footerPanel;
    private JLabel serverAddressLabel;
    private JTextField serverAddressTextField;
    private JButton connectToServerButton;
    private AsynchronousSocketChannel client;
    private static ClientUI instance;

    private Future<Void> future;
    public static ClientUI getInstance() {
        if (instance == null)
            instance = new ClientUI();
        return instance;
    }
    public String sendMessage(String message) throws ExecutionException, InterruptedException {
        byte[] byteMsg = new String(message).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
        Future<Integer> writeResult = client.write(buffer);

        // do some computation

        writeResult.get();
        buffer.flip();
        Future<Integer> readResult = client.read(buffer);

        // do some computation

        readResult.get();
        String echo = new String(buffer.array()).trim();
        buffer.clear();
        return echo;
    }
    private ClientUI() {
        try {
            client = AsynchronousSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 1234);
            future = client.connect(hostAddress);
            start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ClientUI(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        connectToServerButton.addActionListener(e -> {
            try {
                ClientUI client = ClientUI.getInstance();
                client.start();

                client.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while ((line = br.readLine()) != null) {
                    String response = client.sendMessage(line);
                    System.out.println("response from server: " + response);
                    System.out.println("Message to server:");
                }
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    private void start() {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    public void stop() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        JFrame frame = new ClientUI("Client");
        frame.setVisible(true);
    }
}
