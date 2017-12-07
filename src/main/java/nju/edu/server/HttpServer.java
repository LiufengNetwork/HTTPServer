package nju.edu.server;

import nju.edu.HttpStatus;
import nju.edu.HttpUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by lujxu on 2017/12/7.
 */
public class HttpServer implements Runnable {
    private Socket socket;
    private Thread serverThread;

    public HttpServer(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) throws IOException {
        Socket socket = null;
        // 创建一个监听8000端口的服务器Socket
        ServerSocket s = new ServerSocket(HttpUtils.port);
        while (true) {
            socket = s.accept();
            System.out.println("connection is published, port=" + socket.getPort());
            HttpServer server=new HttpServer(socket);
            server.start();
        }

    }

    public void start() throws IOException {
        serverThread = new Thread(this);
        serverThread.start();
    }

    public void run() {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            BufferedReader fromClient = new BufferedReader(inputStreamReader);
            String requestMessageLine = fromClient.readLine();
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(HttpStatus.OK.getInitialLineBytes());
            outputStream.write(HttpUtils.CR);
            outputStream.write(HttpUtils.LF);
            System.out.println(requestMessageLine);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
