package nju.edu.server;

import nju.edu.HttpStatus;
import nju.edu.HttpUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by lujxu on 2017/12/7.
 */
public class HttpServer  implements Runnable {
    private  Socket socket;

    public  HttpServer(Socket socket){
        this.socket=socket;
    }

    public static void main(String []args){
        Socket socket=null;
        try {
            // 创建一个监听8000端口的服务器Socket
            ServerSocket s = new ServerSocket(HttpUtils.port);
            while (true) {
                socket = s.accept();
                System.out.println("connection is published, port=" + socket.getPort());
                new Thread(new HttpServer(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            InputStreamReader inputStreamReader=new InputStreamReader(socket.getInputStream());
            BufferedReader fromClient=new BufferedReader(inputStreamReader);
            String requestMessageLine=fromClient.readLine();
            DataOutputStream outputStream=new DataOutputStream(socket.getOutputStream());
            outputStream.write(HttpStatus.OK.getInitialLineBytes());
            outputStream.writeBytes("\r\n");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
