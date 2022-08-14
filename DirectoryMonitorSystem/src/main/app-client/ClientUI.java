import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
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

    public ClientUI(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        connectToServerButton.addActionListener(e -> {
            AsynchronousSocketChannel client = null;
            try {
                client = AsynchronousSocketChannel.open();
                InetSocketAddress hostAddress = new InetSocketAddress("127.0.0.1", 1234);
                Future<Void> future = client.connect(hostAddress);
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
