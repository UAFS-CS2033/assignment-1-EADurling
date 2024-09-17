import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.StringTokenizer;

public class Server {
    private ServerSocket serverSocket;
    private final int portNo;
    private final String docRoot = "docroot";

    public Server(int portNo) {
        this.portNo = portNo;
    }

    private void processConnection(Socket clientSocket) {
        try {
            OutputStream out;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                out = clientSocket.getOutputStream();
                String requestLine = in.readLine();
                if (requestLine != null) {
                    
                    StringTokenizer tokens = new StringTokenizer(requestLine);
                    String key = tokens.nextToken();
                    String requestedFile = tokens.nextToken();
                    if (key.equals("GET")) {
                        
                        String filepath;
                        if (requestedFile.equals("/home.html") || requestedFile.equals("/")) {
                            filepath = docRoot + "/home.html";
                        } else {
                            filepath = docRoot + requestedFile;
                        }
                        
                        File file = new File(filepath);
                        if (file.exists() && !file.isDirectory()) {
                            if (filepath.endsWith(".html")) {
                                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                                out.write(("Content-Length: " + file.length() + "\r\n").getBytes("UTF-8"));
                                out.write("Content-Type: text/html\r\n".getBytes());
                                out.write("\r\n".getBytes());
                                
                                try {
                                    try (BufferedReader fr = new BufferedReader(new FileReader(file))) {
                                        String buffer2;
                                        while ((buffer2 = fr.readLine()) != null) {
                                            if (buffer2.contains("<img")) {
                                                buffer2 = buffer2.replace("src=\"", "src=\"/");
                                            }
                                            out.write((buffer2 + "\n").getBytes());
                                        }
                                    }
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            } 
                            else if(filepath.endsWith(".css")){
                                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                                out.write(("Content-Length: " + file.length() + "\r\n").getBytes("UTF-8")); // length
                                out.write("Content-Type: text/css\r\n".getBytes());
                                out.write("\r\n".getBytes());
                            }
                            else if(filepath.endsWith(".ico")){
                                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                                out.write(("Content-Length: " + file.length() + "\r\n").getBytes("UTF-8"));
                                out.write("Content-Type: image/avif\r\n".getBytes());
                                out.write("\r\n".getBytes());
                                
                                byte[] bytes = Files.readAllBytes(file.toPath());
                                out.write(bytes);
                            }
                            else if (filepath.endsWith(".png")) {
                                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                                out.write(("Content-Length: " + file.length() + "\r\n").getBytes("UTF-8"));
                                out.write("Content-Type: image/avif\r\n".getBytes());
                                out.write("\r\n".getBytes());
                                
                                byte[] bytes = Files.readAllBytes(file.toPath());
                                out.write(bytes);
                            } 
                            
                            
                            
                        } else { 
                            out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
                            out.write("\r\n".getBytes());
                            out.write("<html><body><h1>404 Not Found</h1></body></html>\r\n".getBytes()); 
                            out.write("\r\n".getBytes());
                        }
                    }
                }
            }
            out.close();
            

        } catch (IOException e) {}
    }

    public void run() {

        boolean running = true;
        try {
            serverSocket = new ServerSocket(portNo);
            System.out.printf("Listen on Port: %d\n", portNo);

            while (running) {
                try (Socket clientSocket = serverSocket.accept()) {
                    processConnection(clientSocket);
                }
            }
            serverSocket.close();

        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }

    public static void main(String[] args0) throws IOException {
        Server server = new Server(8080);
        server.run();
    }
}