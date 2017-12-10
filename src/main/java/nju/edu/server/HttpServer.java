package nju.edu.server;

import nju.edu.HttpMethod;
import nju.edu.HttpStatus;
import nju.edu.HttpUtils;
import nju.edu.HttpVersion;

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
            HttpServer server = new HttpServer(socket);
            server.start();
        }

    }

    public void start() throws IOException {
        serverThread = new Thread(this);
        serverThread.start();
    }

    public void run() {
        try {
            HttpRequest request = new HttpRequest(socket.getInputStream());

            HttpResponse response = new HttpResponse(socket, request);

//            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
//            outputStream.write(HttpStatus.OK.getInitialLineBytes());
//            outputStream.write(HttpUtils.CR);
//            outputStream.write(HttpUtils.LF);

            //是否响应完成
            if (response.response()) {
                socket.close();
            } else {

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
