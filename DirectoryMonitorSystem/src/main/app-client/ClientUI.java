import javax.swing.*;
import java.io.IOException;
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
    AsynchronousSocketChannel client = null;

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

    public ClientUI(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        connectToServerButton.addActionListener(e -> {
            try {
                client = AsynchronousSocketChannel.open();
                InetSocketAddress hostAddress = new InetSocketAddress("127.0.0.1", 1234);
                Future<Void> future = client.connect(hostAddress);
                System.out.println("Connect to server successfully");
                sendMessage("Hellllooooo");
                future.get();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public static void main(String[] args){
        JFrame frame = new ClientUI("Client");
        frame.setVisible(true);
    }
}
