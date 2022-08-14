import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousSocketChannel clientChannel;
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
                    serverChannel = AsynchronousServerSocketChannel.open();
                    System.out.println("Socket is open");
                    SwingUtilities.invokeAndWait(()
                            -> socketStateLabel.setText("Opened with address: " + socketHost + ":" +socketPort));

                    serverChannel.bind(new InetSocketAddress(socketHost,socketPort));
                    while (true) {
                        serverChannel.accept(
                                null, new CompletionHandler<>() {
                                    @Override
                                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                                        if (serverChannel.isOpen())
                                            serverChannel.accept(null, this);
                                        clientChannel = result;
                                        if ((clientChannel != null) && (clientChannel.isOpen())) {
                                            ReadWriteHandler handler = new ReadWriteHandler();
                                            ByteBuffer buffer = ByteBuffer.allocate(32);
                                            Map<String, Object> readInfo = new HashMap<>();
                                            readInfo.put("action", "read");
                                            readInfo.put("buffer", buffer);
                                            clientChannel.read(buffer, readInfo, handler);
                                            String s = StandardCharsets.UTF_8.decode(buffer).toString();
                                            int i =0;
                                        }
                                    }

                                    @Override
                                    public void failed(Throwable exc, Object attachment) {
                                        // process error
                                    }
                                });
                        System.in.read();
                    }
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
    class ReadWriteHandler implements CompletionHandler<Integer, Map<String, Object>> {

        @Override
        public void completed(Integer result, Map<String, Object> attachment) {
            Map<String, Object> actionInfo = attachment;
            String action = (String) actionInfo.get("action");
            if ("read".equals(action)) {
                ByteBuffer buffer = (ByteBuffer) actionInfo.get("buffer");
                buffer.flip();
                actionInfo.put("action", "write");
                clientChannel.write(buffer, actionInfo, this);
                buffer.clear();
            } else if ("write".equals(action)) {
                ByteBuffer buffer = ByteBuffer.allocate(32);
                actionInfo.put("action", "read");
                actionInfo.put("buffer", buffer);
                clientChannel.read(buffer, actionInfo, this);
            }

        }
        @Override
        public void failed(Throwable exc, Map<String, Object> attachment) {

        }

    }
}
