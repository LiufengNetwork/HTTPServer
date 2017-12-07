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
            HttpRequest request=constructRequest(fromClient);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.write(HttpStatus.OK.getInitialLineBytes());
            outputStream.write(HttpUtils.CR);
            outputStream.write(HttpUtils.LF);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO throw IOException?
    private HttpRequest constructRequest(BufferedReader reader) throws IOException {
        String requestLine=reader.readLine();
        String [] params=requestLine.split(HttpUtils.SP+"");
        HttpMethod method=HttpMethod.valueOf(params[0]);
        HttpVersion version=HttpVersion.valueOf(params[2]);
        if(!isValidMethod(method,version)){
            //TODO 505
        }
        return null;
    }

    /**
     * 验证http method是否合法，http 1.0仅允许get, post和head
     * @param method
     * @param version
     * @return
     */
    private boolean isValidMethod(HttpMethod method, HttpVersion version){
        if (version.equals(HttpVersion.HTTP_1_0)){
            if (method.equals(HttpMethod.GET)||method.equals(HttpMethod.POST)||method.equals(HttpMethod.HEAD)){
                return true;
            }else{
                return false;
            }
        }
        return true;
    }
}
