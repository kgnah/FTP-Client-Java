import java.io.*;
import java.net.*;

public class FTPClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // GUI & FTP Client connection
    private GUI gui;

    public void setGUI(GUI gui) {
        this.gui = gui;
    }

    private void protocolLog(String text) {
        if (gui != null) {
            gui.protocolLog(text);
        }
    }

    // connect()
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);

        // receive response from server
        String line = in.readLine();

        if (line == null) {
            throw new IOException("Server disconnected");
        }

        protocolLog("Server: " + line);

        String code = line.substring(0, 3);

        if (line.length() > 3 && line.charAt(3) == '-') {
            while (true) {
                line = in.readLine();

                if (line == null) {
                    throw new IOException("Server disconnected during welcome message");
                }

                protocolLog("Server: " + line);

                if (line.startsWith(code + " ")) {
                    break;
                }
            }
        }
    }

    // sendCommand()
    public String sendCommand(String command) throws IOException {
        // send command
        out.println(command);
        protocolLog("Client: " + command);

        // read the first line and store it
        String line = in.readLine();

        if (line == null) {
            throw new IOException("Server disconnected");
        }

        protocolLog("Server: " + line);

        StringBuilder response = new StringBuilder(); // create container
        response.append(line).append("\n"); // storing it

        //extracting code
        String code = line.substring(0, 3);

        // store more line
        if (line.length() > 3 && line.charAt(3) == '-') {
            while (true) {
                line = in.readLine();

                if (line == null) {
                    throw new IOException("Server disconnected during multiline response");
                }

                protocolLog("Server: " + line);
                response.append(line).append("\n");

                if (line.startsWith(code + " ")) {
                    break;
                }
            }
        }
        return response.toString();
    }

    // login()
    public boolean login(String user, String pass) throws IOException {
        String userResponse = sendCommand("USER " + user);

        if (userResponse.startsWith("331")) {
            String passResponse = sendCommand("PASS " + pass);

            if (passResponse.startsWith("230")) {
                protocolLog("Login Successful");
                return true;
            } else {
                protocolLog("Login failed: " + passResponse);
                return false;
            }
        } else if (userResponse.startsWith("230")) {
            protocolLog("Anonymous login accepted");
            return true;
        } else {
            protocolLog("Login error: " + userResponse);
            return false;
        }
    }

    // PASV
    public Socket enterPassiveMode() throws IOException {
        // send PASV command
        String response = sendCommand("PASV");

        // extract the part inside parentheses
        int start = response.indexOf('(');
        int end = response.indexOf(')');

        if (start == -1 || end == -1) {
            throw new IOException("Invalid PASV response: " + response);
        }

        // extract substring between '(' and ');
        String data = response.substring(start + 1, end);

        String[] parts = data.split(","); // splitting data

        // Error handling
        if (parts.length != 6) {
            throw new IOException("Invalid PASV data: " + data);
        }

        // build IP (h1, h2, h3, h4, p1, p2) => ip = h1.h2.h3.h4
        String ip = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];

        // build port
        int p1 = Integer.parseInt(parts[4]);
        int p2 = Integer.parseInt(parts[5]);

        int port = p1 * 256 + p2;

        // print ip and port
        protocolLog("PASV IP: " + ip);
        protocolLog("PASV Port: " + port);

        // create data socket
        Socket dataSocket = new Socket(ip, port);

        return dataSocket;
    }

    public boolean list() throws IOException {
        // enter passive mode
        Socket dataSocket = enterPassiveMode();

        // send LIST command
        String response = sendCommand("LIST");

        // check if it error
        if (!response.startsWith("150")) {
            protocolLog("LIST failed: " + response);

            dataSocket.close();
            return false;
        }

        // read data from data socket
        BufferedReader dataIn = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

        // print each line
        String line;
        while ((line = dataIn.readLine()) != null) {
            protocolLog(line);
        }

        // close socket
        dataIn.close();
        dataSocket.close();

        // final response
        String finalResponse = in.readLine();
        protocolLog("Server: " + finalResponse);
        return finalResponse.startsWith("226");
    }

    // RETR downloading command
    public boolean download(String fileName) throws IOException {
        // enter passive mode
        Socket dataSocket = enterPassiveMode();

        // send RETR command
        String response = sendCommand("RETR " + fileName);

        // check before action
        if (!response.startsWith("150")) {
            protocolLog("Download failed: " + response);
            dataSocket.close();

            return false;
        }

        // get input stream from data socket
        InputStream dataIn = dataSocket.getInputStream();

        // create file output stream
        FileOutputStream fileOut = new FileOutputStream(fileName);

        // read bytes and write to file
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = dataIn.read(buffer)) != -1) {
            fileOut.write(buffer, 0, bytesRead);
        }

        // closing
        dataIn.close();
        fileOut.close();
        dataSocket.close();

        // final response
        String finalResponse = in.readLine();
        protocolLog("Server: " + finalResponse);
        return finalResponse.startsWith("226");
    }

    // STOR uploading command
    public boolean upload(String fileName) throws IOException {
        // local file validation
        File file = new File(fileName);

        if (!file.exists() || !file.isFile()) {
            protocolLog("Local file not found: " + fileName);
            return false;
        }

        // enter passive mode
        Socket dataSocket = enterPassiveMode();

        // send STOR command
        String response = sendCommand("STOR " + fileName);

        // error handling
        if (!response.startsWith("150")) {
            protocolLog("Upload failed: " + response);

            dataSocket.close();

            return false;
        }

        // open local file
        FileInputStream fileIn = new FileInputStream(fileName);

        // get output stream to server
        OutputStream dataOut = dataSocket.getOutputStream();

        // read file and send to server
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fileIn.read(buffer)) != -1) {
            dataOut.write(buffer, 0, bytesRead);
        }

        // closing
        fileIn.close();
        dataOut.close();
        dataSocket.close();

        // final response
        String finalResponse = in.readLine();
        protocolLog("Server: " + finalResponse);
        return finalResponse.startsWith("226");
    }

    // PWD: get directory
    public boolean pwd() throws IOException {
        // send command
        String response = sendCommand("PWD");

        // extract path from response
        int firstQuote = response.indexOf('"');
        int secondQuote = response.indexOf('"', firstQuote + 1);

        if (firstQuote != -1 && secondQuote != -1) {
            String path = response.substring(firstQuote + 1, secondQuote);
            protocolLog("Current directory: " + path);
            return true;
        } else {
            protocolLog("Could not parse directory from response");
            return false;
        }
    }

    // CWD: change directory
    public boolean cwd(String path) throws IOException {
        // send command
        String response = sendCommand("CWD " + path);

        // check if it success
        if (response.startsWith("250")) {
            protocolLog("Directory changed successfully");
            return true;
        } else {
            protocolLog("CWD failed: " + response);
            return false;
        }
    }

    // DELE: delete file
    public boolean delete(String fileName) throws IOException {
        // send command
        String response = sendCommand("DELE " + fileName);

        // check if it success
        if (response.startsWith("250")) {
            protocolLog("File deleted successfully: " + fileName);
            return true;
        } else {
            protocolLog("DELE failed: " + response);
            return false;
        }
    }

    // MKDIR: make directory
    public boolean mkdir(String directoryName) throws IOException {
        // send command
        String response = sendCommand("MKD " + directoryName);

        // check if it success
        if (response.startsWith("257")) {
            protocolLog("Directory created: " + directoryName);
            return true;
        }
        else {
            protocolLog("MKD failed: " + response);
            return false;
        }
    }

    // RMDIR: remove directory
    public boolean rmdir(String directoryName) throws IOException {
        // send command
        String response = sendCommand("RMD " + directoryName);

        // check if it success
        if (response.startsWith("250")) {
            protocolLog("Directory removed: " + directoryName);
            return true;
        } else {
            protocolLog("RMD failed: " + response);
            return false;
        }
    }

    // quit
    public void quit() throws IOException {
        // send command
       sendCommand("QUIT");

        in.close();
        out.close();
        socket.close();

    }
}