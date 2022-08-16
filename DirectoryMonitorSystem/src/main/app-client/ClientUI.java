import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.nio.file.StandardWatchEventKinds.*;

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
            addOpenSocketListener();
        });
    }

    private void addOpenSocketListener(){
        new SwingWorker<>(){
            @Override
            protected Object doInBackground()  {
                try {
                    ClientUI client = ClientUI.getInstance();
                    client.start();
                    System.out.println("Connect to server successfully");

                    //region watcher
                    Path path = Path.of("D:\\KHTN\\Java\\DirMonitor\\test");
                    FileSystem fs = path.getFileSystem();
                    try (WatchService service = fs.newWatchService()){
                        path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                        WatchKey key;
                        while (true) {
                            key = service.take();
                            WatchEvent.Kind<?> kind;
                            String message = "";
                            for (WatchEvent<?> watchEvent : key.pollEvents()) {
                                // Get the type of the event
                                kind = watchEvent.kind();
                                if (OVERFLOW == kind) {
                                } else if (ENTRY_CREATE == kind) {
                                    Path newPath = ((WatchEvent<Path>) watchEvent).context();
                                    message ="New path created: " + newPath;
                                    System.out.println(message);
                                } else if (ENTRY_MODIFY == kind) {
                                    Path newPath = ((WatchEvent<Path>) watchEvent).context();
                                    message = "New path modified: " + newPath;
                                    System.out.println(message);
                                } else if(ENTRY_DELETE == kind){
                                    Path newPath = ((WatchEvent<Path>) watchEvent).context();
                                    message = "New path deleted: " + newPath;
                                    System.out.println(message);
                                }
                                client.sendMessage(message);
                            }

                            if (!key.reset()) {
                                break; // loop
                            }
                        }
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }

                    //

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
                return "Connected";
            }
        }.execute();
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
