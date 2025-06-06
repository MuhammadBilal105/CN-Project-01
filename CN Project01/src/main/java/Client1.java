import java.io.*;
import java.net.*;
import java.util.logging.Logger;
import javax.swing.*;



//make connection between client and server, I/O stream & Control
public class Client1 extends javax.swing.JFrame {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dout;
    private String clientName;
    private String serverIP = "127.0.0.1";
    private static final Logger logger = Logger.getLogger(Client1.class.getName());
    private volatile boolean running = false;
    private volatile boolean userRequestedExit = false;

    
    
    //when client start check the connenctin between if the setup failed client are disconnecting
    public Client1() {
        initComponents();
        if (!getConnectionDetails()) {
            JOptionPane.showMessageDialog(this, "IP or Name was not provided. Exiting.");
            System.exit(0);
        }
        connectToServer();
    }

    
    
    
    //when we run the client server ask the IP and Username from the client and then connected to the server  
    private boolean getConnectionDetails() {
        String inputIP = JOptionPane.showInputDialog(this, "Enter server IP:", "127.0.0.1");
        if (inputIP == null || inputIP.trim().isEmpty()) return false;

        String name = JOptionPane.showInputDialog(this, "Enter your name:");
        if (name == null || name.trim().isEmpty()) return false;

        serverIP = inputIP.trim();
        clientName = name.trim();
        return true;
    }

    
    
    
    //this code is used for handling the communication data like msgs
    private void connectToServer() {
        if (running) return;
        running = true;

        new Thread(() -> {
            while (!userRequestedExit) {
                try {
                    appendToMessageArea("Trying to connect to server...");
                    socket = new Socket(serverIP, 1201);
                    dis = new DataInputStream(socket.getInputStream());
                    dout = new DataOutputStream(socket.getOutputStream());

                    dout.writeUTF(clientName);
                    appendToMessageArea("Connected to server as " + clientName);

                    String msgin;
                    while (!userRequestedExit && (msgin = dis.readUTF()) != null) {
                        appendToMessageArea(msgin);
                    }

                } catch (IOException e) {
                    if (!userRequestedExit) {
                        appendToMessageArea("Server connection lost. Retrying in 3 seconds...");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ignored) {}
                    }
                } finally {
                    disconnect();
                }
            }

            appendToMessageArea(clientName +" is disconnected permanently.");
        }).start();
    }

    
    
    
    //here we use this code when the client want to properly disconnect from the server
    private void disconnect() {
        try {
            running = false;
            if (dout != null) dout.close();
            if (dis != null) dis.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.severe("Error during disconnect: " + e.getMessage());
        }
    }

    
    //when the new msg enter the inbox this will make visible for the user 
    private void appendToMessageArea(String message) {
        SwingUtilities.invokeLater(() -> {
            msg_area.append(message + "\n");
            msg_area.setCaretPosition(msg_area.getDocument().getLength());
        });
    }
    
    
    
    
    
    //Handle the messages, Exit command when user enter the exit user disconnect from the server, check the connection errors  
    private void msg_sendActionPerformed(java.awt.event.ActionEvent evt) {
        String msg = msg_text.getText().trim();
        if (!msg.isEmpty()) {
            try {
                if (dout != null) {
                    if (msg.equalsIgnoreCase("exit")) {
                        userRequestedExit = true;
                    }
                    dout.writeUTF("/private " + msg);
                    msg_text.setText("");
                } else {
                    appendToMessageArea("⚠ Not connected to server");
                }
            } catch (IOException e) {
                logger.severe("Error sending message: " + e.getMessage());
                appendToMessageArea("Error sending message: " + e.getMessage());
            }
        }
    }

    
    
    
    
    //GUI "Graphical User Interface" Code where we can design a server and client interface
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea msg_area;
    private javax.swing.JButton msg_send;
    private javax.swing.JTextField msg_text;

    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        msg_area = new javax.swing.JTextArea();
        msg_send = new javax.swing.JButton();
        msg_text = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat Client");

        jLabel1.setFont(new java.awt.Font("Cambria", 1, 36));
        jLabel1.setText(" Client");

        msg_area.setBackground(new java.awt.Color(0, 0, 0));
        msg_area.setColumns(20);
        msg_area.setFont(new java.awt.Font("Arial", 0, 14));
        msg_area.setForeground(new java.awt.Color(255, 255, 255));
        msg_area.setRows(5);
        msg_area.setEditable(false);
        jScrollPane1.setViewportView(msg_area);

        msg_send.setBackground(new java.awt.Color(204, 0, 51));
        msg_send.setFont(new java.awt.Font("Cambria", 0, 16));
        msg_send.setForeground(new java.awt.Color(255, 255, 255));
        msg_send.setText("Send");
        msg_send.addActionListener(this::msg_sendActionPerformed);

        msg_text.setBackground(new java.awt.Color(0, 0, 0));
        msg_text.setFont(new java.awt.Font("Arial", 0, 14));
        msg_text.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jScrollPane1)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(msg_text, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(msg_send, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(12, 12, 12)
                            .addComponent(msg_send, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(msg_text, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> new Client1().setVisible(true));
    }
}
