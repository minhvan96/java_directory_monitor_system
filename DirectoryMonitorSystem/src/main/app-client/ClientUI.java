import javax.swing.*;

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
    }

    public static void main(String[] args){
        JFrame frame = new ClientUI("Client");
        frame.setVisible(true);
    }
}
