import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;

public class ServerUI extends JFrame{
    private static final int socketPort = 1234;
    private static final String socketHost = "127.0.0.1";
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel bodyPanel;
    private JPanel footerPanel;
    private JPanel clientsListPanel;
    private JPanel clientLogPanel;
    private JTable clientsTable;
    private JScrollPane clientsListScrollPanel;
    private JLabel welcomeLabel;
    private JLabel clientsListLabel;
    private JLabel clientLogLabel;
    private JLabel socketStateLabel;
    AsynchronousServerSocketChannel server;
    public ServerUI(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

        createClientsTable();
        addOpenSocketListener();
    }

    private void createClientsTable(){
        String[] clientsTableColumn = {"Client ID", "Client Name"};
        clientsTable.setModel(new DefaultTableModel(
                null,
                clientsTableColumn
        ));
    }
    private void addOpenSocketListener(){
        new SwingWorker(){
            @Override
            protected Object doInBackground()  {
                try {
                    if(server != null && server.isOpen()){
                        server.close();
                        SwingUtilities.invokeAndWait(()
                                -> socketStateLabel.setText("Closed"));
                        return "close";
                    }
                    server = AsynchronousServerSocketChannel.open();
                    System.out.println("Socket is open");
                    SwingUtilities.invokeAndWait(()
                            -> socketStateLabel.setText("Opened with address: " + socketHost + ":" +socketPort));

                    server.bind(new InetSocketAddress(socketHost,socketPort));
                    Future<AsynchronousSocketChannel> acceptCon = server.accept();
                    AsynchronousSocketChannel client = acceptCon.get(10, TimeUnit.SECONDS);
                    if ((client != null) && (client.isOpen())) {
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
                return "done";
            }
        }.execute();
    }

    public static void main(String[] args){
        JFrame frame = new ServerUI("Directory Monitor Server");
        frame.setVisible(true);
    }
}
