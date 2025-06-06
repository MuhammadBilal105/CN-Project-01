import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;



//create a chat server window which can handle safely multiple connected user
public class Server1 extends javax.swing.JFrame {
    private ServerSocket serverSocket;
    private final List<ServerClient1handler> clients = Collections.synchronizedList(new ArrayList<>());
    private static final Logger logger = Logger.getLogger(Server1.class.getName());
    private volatile boolean isRunning = false;

    public Server1() {
        initComponents();
        setupServer();
    }

    
    
    
    //setup the buttons for server, with the help of buttons we can start the server, stop the server, send the message
    private void setupServer() {
        start_button.addActionListener(e -> startServer());
        stop_button.addActionListener(e -> stopServer());
        msg_send.addActionListener(e -> sendButtonActionPerformed());
    }

    
    
    //this is a start button which start the server when we click on it, this button work when the server stop
    private void startServer() {
        if (isRunning) {
            appendToLog("Server is already running");
            return;
        }

        
        
        
        //this code is used for server loop that accept the new connection of a new client
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(1201, 0, InetAddress.getByName("0.0.0.0"));
                isRunning = true;
                appendToLog("Server started on port 1201");
                SwingUtilities.invokeLater(() -> {
                    start_button.setEnabled(false);
                    stop_button.setEnabled(true);
                });

                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();

                        ServerClient1handler clientHandler = new ServerClient1handler(clientSocket, this);
                        clients.add(clientHandler);
                        new Thread(clientHandler).start();
                    } catch (IOException e) {
                        if (isRunning) {
                            appendToLog("Accept error: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                appendToLog("Failed to start server: " + e.getMessage());
            }
        }).start();
    }

    
    
   //this is a stop button which stop the server when we click on it, this button works when the server start    
    private void stopServer() {
        if (!isRunning) {
            appendToLog("Server is not running");
            return;
        }

        
        //it's use for shut down the server, close all connection of client & also shutdown the socket 
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

            synchronized (clients) {
                for (ServerClient1handler client : new ArrayList<>(clients)) {
                    try {
                        client.getClientSocket().close();
                    } catch (IOException e) {
                        logger.warning("Error closing client socket: " + e.getMessage());
                    }
                }
                clients.clear();
            }
            
            
            //when server stoped it show the message "Server Stopped"
            //make the start button clickable when the server will be stop
            //when if something goes wrong while stopping, it shows the error 
            appendToLog("Server stopped");
            SwingUtilities.invokeLater(() -> {
                start_button.setEnabled(true);
                stop_button.setEnabled(false);
            });
        } catch (IOException e) {
            appendToLog("Error stopping server: " + e.getMessage());
        }
    }

    
    //Broadcast to manage the all clients which are connected to the server
    public void broadcast(String message, ServerClient1handler excludeClient) {
        synchronized (clients) {
            for (ServerClient1handler client : new ArrayList<>(clients)) {
                if (client != excludeClient) {
                    try {
                        client.sendMessage(message);
                    } catch (IOException e) {
                        logger.warning("Broadcast failed to " + client.getClientName());
                        clients.remove(client);
                    }
                }
            }
        }
    }
    

    //Disconnect or remove the client from the active list
    public void removeClient(ServerClient1handler client) {
        clients.remove(client);
        appendToLog(client.getClientName() + " was removed from active clients");
    }
    
    //it's used for auto scrol the new messages and updates the server for new msgs 
    public void appendToLog(String message) {
        SwingUtilities.invokeLater(() -> {
            msg_area.append(message + "\n");
            msg_area.setCaretPosition(msg_area.getDocument().getLength());
        });
    }

    
    //control the send button when click on the "Send" button msgs are distribute to everyone
    private void sendButtonActionPerformed() {
        String msg = msg_text.getText().trim();
        if (!msg.isEmpty()) {
            broadcast("Server: " + msg, null);
            appendToLog("Server: " + msg);
            msg_text.setText("");
        }
    }

    
    
    //GUI "Graphical User Interface" Code where we can design a server and client interface
    @SuppressWarnings("unchecked")
    private void initComponents() {
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        msg_area = new javax.swing.JTextArea();
        msg_send = new javax.swing.JButton();
        msg_text = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        start_button = new javax.swing.JButton();
        stop_button = new javax.swing.JButton();

        jButton1.setText("jButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat Server");

        msg_area.setBackground(new java.awt.Color(0, 0, 0));
        msg_area.setColumns(20);
        msg_area.setFont(new java.awt.Font("Arial", 0, 14));
        msg_area.setForeground(new java.awt.Color(255, 255, 255));
        msg_area.setRows(5);
        msg_area.setEditable(false);
        jScrollPane1.setViewportView(msg_area);

        msg_send.setBackground(new java.awt.Color(204, 0, 51));
        msg_send.setFont(new java.awt.Font("Cambria", 1, 16));
        msg_send.setForeground(new java.awt.Color(0, 0, 0));
        msg_send.setText("Send");

        msg_text.setBackground(new java.awt.Color(0, 0, 0));
        msg_text.setFont(new java.awt.Font("Arial", 0, 14));
        msg_text.setForeground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Cambria", 1, 36));
        jLabel1.setText("Server");

        start_button.setBackground(new java.awt.Color(204, 0, 51));
        start_button.setFont(new java.awt.Font("Cambria", 1, 16));
        start_button.setForeground(new java.awt.Color(0, 0, 0));
        start_button.setText("Start Server");

        stop_button.setBackground(new java.awt.Color(204, 0, 51));
        stop_button.setFont(new java.awt.Font("Cambria", 1, 16));
        stop_button.setForeground(new java.awt.Color(0, 0, 0));
        stop_button.setText("Stop Server");
        stop_button.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(msg_text)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(msg_send, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 118, Short.MAX_VALUE)
                        .addComponent(start_button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stop_button)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(start_button)
                    .addComponent(stop_button))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(msg_send)
                    .addComponent(msg_text, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }

    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea msg_area;
    private javax.swing.JButton msg_send;
    private javax.swing.JTextField msg_text;
    private javax.swing.JButton start_button;
    private javax.swing.JButton stop_button;

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Server1.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new Server1().setVisible(true));
    }
}
