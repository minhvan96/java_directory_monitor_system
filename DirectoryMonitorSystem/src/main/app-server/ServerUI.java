import javax.swing.*;

public class ServerUI extends JFrame{
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel bodyPanel;
    private JPanel footerPanel;
    private JPanel clientsListPanel;
    private JPanel clientLogPanel;
    private JTable clientsListTable;
    private JScrollPane clientsListScrollPanel;
    private JLabel welcomeLabel;
    private JLabel clientsListLabel;
    private JLabel clientLogLabel;

    public ServerUI(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
    }

    public static void main(String[] args){
        JFrame frame = new ServerUI("Directory Monitor Server");
        frame.setVisible(true);
    }
}
