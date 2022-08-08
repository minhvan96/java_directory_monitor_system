import Threads.SocketThread;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ServerUI extends JFrame{
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
    private JButton openSocketButton;

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
        openSocketButton.addActionListener(e -> {
            SocketThread st = new SocketThread(socketStateLabel);
            st.start();
        });
    }

    public static void main(String[] args){
        JFrame frame = new ServerUI("Directory Monitor Server");
        frame.setVisible(true);
    }
}
