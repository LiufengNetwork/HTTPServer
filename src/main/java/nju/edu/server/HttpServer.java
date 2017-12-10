package nju.edu.server;

import nju.edu.server.impl.HttpRequestImpl;
import nju.edu.server.impl.HttpResponseImpl;
import nju.edu.utils.HttpUtils;
import nju.edu.utils.LogInvoHandler;

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
            HttpRequest request= new HttpRequestImpl(socket.getInputStream()) ;

            //日志功能通过代理实现，不涉及业务功能，可以不用点进去看，
            HttpResponse response = LogInvoHandler.getProxyInstance(new HttpResponseImpl(socket, request, new HttpWriter(socket))) ;

            //是否响应完成
            if (response.response()) {
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedRequestException e) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


}
