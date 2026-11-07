import javax.swing.*;
import java.awt.*;

public class GUI {
    // Components
    private JFrame frame;

    // user & password field
    private JTextField userField;
    private JPasswordField passwordField;

    // host & input field
    private JTextField hostField;
    private JTextField inputField;

    // status and log area
    private JTextArea statusArea;
    private JTextArea logArea;

    // Button field
    private JButton connectButton;

    private JButton listButton;
    private JButton pwdButton;
    private JButton cwdButton;

    private JButton downloadButton;
    private JButton uploadButton;
    private JButton deleteButton;

    private JButton mkdirButton;
    private JButton rmdirButton;

    private JButton quitButton;

    // panel
    private JPanel topPanel;

    // scroll pane
    private JScrollPane statusScroll;
    private JScrollPane logScroll;

    // split pane
    private JSplitPane splitPane;

    // ftp client
    private FTPClient ftp;

    // Constructor
    public GUI() {
        ftp = new FTPClient();
        ftp.setGUI(this); // GUI <-> FTP Client

        createWindow();
        createComponents();
        createLayout();
        createActions();
        setDisconnectedState();

        frame.setVisible(true);
    }

    // status method
    private void status(String text) {
        statusArea.append(text + "\n");

        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    // protocol log method
    public void protocolLog(String text) {
        logArea.append(text + "\n");

        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // get input method
    private String getInput() {
        String input = inputField.getText().trim();

        if (input.isEmpty()) {
            status("Input required");
            return null;
        }
        return input;
    }

    // button locking method
    private void setButtonsEnabled(boolean enabled) {

        connectButton.setEnabled(enabled);

        listButton.setEnabled(enabled);
        pwdButton.setEnabled(enabled);
        cwdButton.setEnabled(enabled);

        downloadButton.setEnabled(enabled);
        uploadButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);

        mkdirButton.setEnabled(enabled);
        rmdirButton.setEnabled(enabled);

        quitButton.setEnabled(enabled);
    }

    // Window
    private void createWindow() {
        frame = new JFrame("FTP Client");

        frame.setSize(1100, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // minimum size of window
        frame.setMinimumSize(new Dimension(1000, 600));
    }

    // Create Component
    private void createComponents() {
        // Host field
       hostField = new JTextField("ftp.gnu.org", 20);

       // Input field
        inputField = new JTextField(15);
        inputField.setText(""); // clean after use

        // user & password field
        userField = new JTextField("anonymous", 10);
        passwordField = new JPasswordField("anonymous", 10);

        // status area
        statusArea = new JTextArea(5, 50);
        statusArea.setEditable(false);

        statusScroll = new JScrollPane(statusArea);

        // log area
        logArea = new JTextArea();
        logArea.setEditable(false);

        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        logScroll = new JScrollPane(logArea);

       // Connect Button
       connectButton = new JButton("Connect");
       // List Button
        listButton = new JButton("LIST");
        // PWD Button
        pwdButton = new JButton("PWD");
        // CWD BUTTON
        cwdButton = new JButton ("CWD");
        // DOWNLOAD BUTTON
        downloadButton = new JButton("DOWNLOAD");
        // UPLOAD BUTTON
        uploadButton = new JButton("UPLOAD");
        // DELETE BUTTON
        deleteButton = new JButton("DELETE");
        //MKDIR BUTTON
        mkdirButton = new JButton("MKDIR");
        //RMDIR BUTTON
        rmdirButton = new JButton("RMDIR");
        // QUIT BUTTON
        quitButton = new JButton("QUIT");
    }

    // Layout
    private void createLayout() {
        // top panel with 2 rows
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 1));

        // row 1 connection controls
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // host field
        connectionPanel.add(new JLabel("Host: "));
        connectionPanel.add(hostField);

        // user field
        connectionPanel.add(new JLabel("User:"));
        connectionPanel.add(userField);

        // password field
        connectionPanel.add(new JLabel("Pass:"));
        connectionPanel.add(passwordField);

        // connect and quit buttons
        connectionPanel.add(connectButton);
        connectionPanel.add(quitButton);

        // row 2 ftp command controls
        JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // input field
        commandPanel.add(new JLabel("Input: "));
        commandPanel.add(inputField);

        // list, pwd and cwd buttons
        commandPanel.add(listButton);
        commandPanel.add(pwdButton);
        commandPanel.add(cwdButton);

        // download, upload, delete, mkdir and rmdir buttons
        commandPanel.add(downloadButton);
        commandPanel.add(uploadButton);
        commandPanel.add(deleteButton);
        commandPanel.add(mkdirButton);
        commandPanel.add(rmdirButton);

        // add rows to top panel
        topPanel.add(connectionPanel);
        topPanel.add(commandPanel);

        // add to frame
        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);

        // split pane
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statusScroll, logScroll);
        splitPane.setDividerLocation(100);

        frame.add(splitPane, BorderLayout.CENTER);
    }

    // Button Actions
    private void createActions() {
        // Connect Button Action
        connectButton.addActionListener(e -> {
            new Thread(() -> {

                clearLogs();
                setButtonsEnabled(false);

                try {
                    ftp.connect(hostField.getText(), 21);

                    // custom username & password
                    String username = userField.getText();
                    String password = new String(passwordField.getPassword());

                    if (ftp.login(username, password)) {

                        status("Connected and logged in to server");
                        setConnectedState();
                    }
                    else {
                        status("Login failed");
                        setDisconnectedState();
                    }
                }
                catch (Exception ex) {
                    status("Connection failed");
                    setDisconnectedState();
                }
            }).start();
        });

        // LIST BUTTON ACTION
        listButton.addActionListener(e -> {
            new Thread(() -> {

                setButtonsEnabled(false);

                try {
                    if (ftp.list()) {
                        status("LIST executed");
                    }
                    else{
                        status("LIST failed");
                    }
                }
                catch (Exception ex) {
                    status("LIST failed");
                }
                finally {
                    setConnectedState();
                }
            }).start();
        });

        // PWD BUTTON ACTION
        pwdButton.addActionListener(e -> {
            new Thread(() -> {

                setButtonsEnabled(false);

                try {
                   if (ftp.pwd()) {
                       status("PWD executed");
                   }
                   else{
                       status("PWD failed");
                   }
                }
                catch (Exception ex) {
                    status("PWD failed");
                }
                finally {
                    setConnectedState();
                }

            }).start();
        });

        // CWD BUTTON ACTION
        cwdButton.addActionListener(e -> {
            new Thread(() -> {

                String input = getInput();

                if (input == null) {
                    return;
                }

                setButtonsEnabled(false);

                try {
                    if (ftp.cwd(input)) {
                        status("CWD executed");
                    }
                    else {
                        status("CWD failed");
                    }
                }
                catch (Exception ex) {
                    status("CWD failed");
                }
                finally {
                    setConnectedState();
                }

            }).start();
        });

        // DOWNLOAD BUTTON ACTION
        downloadButton.addActionListener(e -> {
            new Thread(() -> {

                String input = getInput();

                if (input == null) {
                    return;
                }

                setButtonsEnabled(false);

                try {
                    if (ftp.download(input)) {
                        status("DOWNLOAD completed");
                    }
                    else{
                        status("DOWNLOAD failed");
                    }
                }
                catch (Exception ex) {
                    status("DOWNLOAD failed");
                }
                finally {
                    setConnectedState();
                }

            }).start();
        });

        // UPLOAD BUTTON ACTION
        uploadButton.addActionListener(e -> {
            new Thread(() -> {

                try {
                    String input = getInput();

                    if (input == null) {
                        return;
                    }

                    setButtonsEnabled(false);

                    if (ftp.upload(input)) {
                        status("UPLOAD completed");
                    }
                    else {
                        status("UPLOAD failed");
                    }
                }
                catch (Exception ex) {
                    status("UPLOAD failed");
                }
                finally {
                    setConnectedState();
                }

            }).start();
        });

        // DELETE BUTTON ACTION
        deleteButton.addActionListener(e -> {
            new Thread(() -> {

                try {
                    String input = getInput();

                    if (input == null) {
                        return;
                    }

                    setButtonsEnabled(false);

                    if (ftp.delete(input)) {
                        status("DELETE completed");
                    }
                    else {
                        status("DELETE failed");
                    }
                }
                catch (Exception ex) {
                    status("DELETE failed");
                }
                finally {
                    setConnectedState();
                }

            }).start();
        });

        // MKDIR BUTTON ACTION
        mkdirButton.addActionListener(e -> {
           new Thread(() -> {

               try {
                   String input = getInput();

                   if (input == null) {
                       return;
                   }

                   setButtonsEnabled(false);

                   if (ftp.mkdir(input)){
                       status("MKDIR completed");
                   }
                   else {
                       status("MKDIR failed");
                   }
               }
               catch (Exception ex) {
                   status("MKDIR failed");
               }
               finally {
                   setConnectedState();
               }

           }).start();
        });

        // RMDIR BUTTON ACTION
        rmdirButton.addActionListener(e -> {
            new Thread(() -> {

                try {
                    String input = getInput();

                    if (input == null) {
                        return;
                    }

                    setButtonsEnabled(false);

                    if (ftp.rmdir(input)) {
                        status("RMDIR completed");
                    }
                    else {
                        status("RMDIR failed");
                    }
                }
                catch (Exception ex) {
                    status("RMDIR failed");
                }
                finally {
                    setConnectedState();
                }

            }).start();
        });

        // QUIT BUTTON ACTION
        quitButton.addActionListener(e -> {
            new Thread(() -> {
                setButtonsEnabled(false);

                try {
                    ftp.quit();
                    status("Disconnected");
                    setDisconnectedState();
                }
                catch (Exception ex) {
                    status("Error during quit");
                }
            }).start();
        });
    }

    // connected state
    private void setConnectedState() {
        connectButton.setEnabled(false);

        listButton.setEnabled(true);
        pwdButton.setEnabled(true);
        cwdButton.setEnabled(true);

        downloadButton.setEnabled(true);
        uploadButton.setEnabled(true);
        deleteButton.setEnabled(true);

        mkdirButton.setEnabled(true);
        rmdirButton.setEnabled(true);

        quitButton.setEnabled(true);
    }

    // disconnected state
    private void setDisconnectedState() {
        connectButton.setEnabled(true);

        listButton.setEnabled(false);
        pwdButton.setEnabled(false);
        cwdButton.setEnabled(false);

        downloadButton.setEnabled(false);
        uploadButton.setEnabled(false);
        deleteButton.setEnabled(false);

        mkdirButton.setEnabled(false);
        rmdirButton.setEnabled(false);

        quitButton.setEnabled(false);
    }

    // clean status and log areas before new session
    private void clearLogs() {
        statusArea.setText("");

        logArea.setText("");
    }

    // Main
    public static void main(String[] args) {
        new GUI();
    }
}

