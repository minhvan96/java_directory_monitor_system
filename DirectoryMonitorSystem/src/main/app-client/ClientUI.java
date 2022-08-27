import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.nio.file.StandardWatchEventKinds.*;

public class ClientUI extends JFrame {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel bodyPanel;
    private JPanel footerPanel;
    private JButton connectToServerButton;
    private JLabel socketStateLabel;
    private JTextField pathTextField;
    private JTextArea logTextArea;
    private JLabel pathLabel;
    private AsynchronousSocketChannel client;
    private static ClientUI instance;

    private Future<Void> future;

    public static ClientUI getInstance() {
        if (instance == null)
            instance = new ClientUI();
        return instance;
    }

    public String sendMessage(String message) throws ExecutionException, InterruptedException {
        byte[] byteMsg = message.getBytes();
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
        connectToServerButton.addActionListener(e -> addOpenSocketListener());
    }

    private void addOpenSocketListener() {
        new SwingWorker<>() {
            @Override
            protected Object doInBackground() {
                try {
                    if (!isValidPath(pathTextField.getText())) {
                        SwingUtilities.invokeAndWait(() -> {
                            JOptionPane.showMessageDialog(new JFrame(), "Path is invalid");
                        });
                        return "Failed";
                    }

                    ClientUI client = ClientUI.getInstance();
                    client.start();
                    System.out.println("Connect to server successfully");
                    SwingUtilities.invokeAndWait(()
                            -> socketStateLabel.setText("Connected to server"));
                    //region watcher
                    Path path = Path.of(pathTextField.getText());
                    FileSystem fs = path.getFileSystem();
                    try (WatchService service = fs.newWatchService()) {
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
                                    message = "New path created: " + newPath;
                                    System.out.println(message);
                                } else if (ENTRY_MODIFY == kind) {
                                    Path newPath = ((WatchEvent<Path>) watchEvent).context();
                                    message = "New path modified: " + newPath;
                                    System.out.println(message);
                                } else if (ENTRY_DELETE == kind) {
                                    Path newPath = ((WatchEvent<Path>) watchEvent).context();
                                    message = "New path deleted: " + newPath;
                                    System.out.println(message);
                                }
                                client.sendMessage(message);
                                logTextArea.append(message + "\n");
                            }

                            if (!key.reset()) {
                                break; // loop
                            }
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }

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
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                return "Connected";
            }
        }.execute();
    }

    private static boolean isValidPath(String path) {
        try {
            if (path != null && !path.trim().isEmpty())
                Paths.get(path);
            else
                return false;
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
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

    public static void main(String[] args) {
        JFrame frame = new ClientUI("Client");
        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        headerPanel = new JPanel();
        headerPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(headerPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        connectToServerButton = new JButton();
        connectToServerButton.setText("Connect");
        headerPanel.add(connectToServerButton, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        socketStateLabel = new JLabel();
        socketStateLabel.setText("Disconnected");
        headerPanel.add(socketStateLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pathTextField = new JTextField();
        headerPanel.add(pathTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        pathLabel = new JLabel();
        pathLabel.setText("Path");
        headerPanel.add(pathLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bodyPanel = new JPanel();
        bodyPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(bodyPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        bodyPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        logTextArea = new JTextArea();
        scrollPane1.setViewportView(logTextArea);
        footerPanel = new JPanel();
        footerPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(footerPanel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
