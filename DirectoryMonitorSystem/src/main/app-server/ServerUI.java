import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class ServerUI extends JFrame {
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

    public void createClientsTable() {
        var clientLogs = readClientLogs();
        String[] clientTableColumnHeaders = {"Address", "Event", "Time"};
        clientsTable.setModel(new DefaultTableModel(
                clientLogs,
                clientTableColumnHeaders
        ));

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(clientsTable.getModel());
        clientsTable.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>(3);
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
    }

    private void addNewLogToTable(String clientId, String event, String date) {
        DefaultTableModel model = (DefaultTableModel) clientsTable.getModel();
        model.addRow(new Object[]{clientId, event, date});
    }

    private Object[][] readClientLogs() {
        try {
            String directoryPath = System.getenv("APPDATA");
            String path = String.valueOf(Paths.get(directoryPath, "client"));
            Integer index = 0;
            File logFile = new File(path);
            Scanner logReader = new Scanner(logFile);

            Object[][] record = new Object[getMaxLine(path)][3];

            while (logReader.hasNextLine()) {
                String line = logReader.nextLine();
                if (line == "")
                    continue;
                String[] data = line.split("#", 3);
                String clientAddress = data[0];
                String action = data[1];
                String date = data[2];
                record[index][0] = clientAddress;
                record[index][1] = action;
                record[index][2] = date;
                index++;
            }
            return record;
        } catch (FileNotFoundException exception) {
            System.out.println("cannot read file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static int getMaxLine(String filePath) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filePath));
        int index = 0;
        while (input.readLine() != null) {
            index++;
        }
        return index;
    }

    private void addOpenSocketListener() {
        new SwingWorker() {
            @Override
            protected Object doInBackground() {
                try {
                    serverChannel = AsynchronousServerSocketChannel.open();
                    System.out.println("Socket is open");
                    SwingUtilities.invokeAndWait(()
                            -> socketStateLabel.setText("Opened with address: " + socketHost + ":" + socketPort));

                    serverChannel.bind(new InetSocketAddress(socketHost, socketPort));
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

    public static void main(String[] args) {
        JFrame frame = new ServerUI("Directory Monitor Server");
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
        headerPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(headerPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        welcomeLabel = new JLabel();
        welcomeLabel.setText("Welcome to Directory Monitor Server");
        headerPanel.add(welcomeLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        socketStateLabel = new JLabel();
        socketStateLabel.setText("Closed");
        headerPanel.add(socketStateLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bodyPanel = new JPanel();
        bodyPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(bodyPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        clientsListPanel = new JPanel();
        clientsListPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        bodyPanel.add(clientsListPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        clientsListScrollPanel = new JScrollPane();
        clientsListPanel.add(clientsListScrollPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        clientsTable = new JTable();
        clientsListScrollPanel.setViewportView(clientsTable);
        clientsListLabel = new JLabel();
        this.$$$loadLabelText$$$(clientsListLabel, this.$$$getMessageFromBundle$$$("i18", "clients"));
        clientsListPanel.add(clientsListLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clientLogPanel = new JPanel();
        clientLogPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        bodyPanel.add(clientLogPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        clientLogPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        clientLogLabel = new JLabel();
        this.$$$loadLabelText$$$(clientLogLabel, this.$$$getMessageFromBundle$$$("i18", "client.log"));
        clientLogPanel.add(clientLogLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        footerPanel = new JPanel();
        footerPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(footerPanel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        clientsListLabel.setLabelFor(clientsListScrollPanel);
        clientLogLabel.setLabelFor(scrollPane1);
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
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
                try {
                    writeToFile(clientChannel.getRemoteAddress().toString(), bufferString.trim());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                buffer.clear();
            } else if ("write".equals(action)) {
                ByteBuffer buffer = ByteBuffer.allocate(512);
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

        private void writeToFile(String id, String text) {
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
                output.append(id + "#" + text + "#" + new Date() + '\n');
                output.close();
                addNewLogToTable(id, text, new Date().toString());
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
}
