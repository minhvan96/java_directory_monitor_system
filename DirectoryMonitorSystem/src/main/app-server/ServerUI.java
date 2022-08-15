import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
                                        }
                                    }

                                    @Override
                                    public void failed(Throwable exc, Object attachment) {
                                        System.out.println("Failed!!!");
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
                String bufferString = new String(buffer.array(), StandardCharsets.UTF_8);
                System.out.println("Read: " + bufferString);
                try {
                    writeToFile(clientChannel.getLocalAddress().toString(), bufferString.trim());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                buffer.clear();
            } else if ("write".equals(action)) {
                ByteBuffer buffer = ByteBuffer.allocate(32);
                actionInfo.put("action", "read");
                actionInfo.put("buffer", buffer);
                clientChannel.read(buffer, actionInfo, this);
                String newContent = new String(buffer.array(), StandardCharsets.UTF_8);
                System.out.println("Write:" + newContent);
            }

        }
        @Override
        public void failed(Throwable exc, Map<String, Object> attachment) {
            System.out.println("Failed");
        }

        private void writeToFile(String id, String text){
            try {
                String directoryPath = System.getenv("APPDATA");
                String path = String.valueOf(Paths.get(directoryPath, "client"));
                File myObj = new File(path);
                if (myObj.createNewFile()) {
                    System.out.println("File created: " + myObj.getName());
                } else {
                    System.out.println("File already exists.");
                }
                BufferedWriter output = new BufferedWriter(new FileWriter(path, true));
                output.append(text+ '\n');
                output.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
}
