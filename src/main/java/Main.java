import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by lujxu on 2017/12/5.
 */
public class Main {

    public static void main(String []args){
        Socket socket=null;
        try {
            // 创建一个监听8000端口的服务器Socket
            ServerSocket s = new ServerSocket(8000);
            System.out.println("MyWebServer等待来自浏览器的连接\n");
            while (true) {
                socket = s.accept();
                System.out.println("连接已建立。端口号：" + socket.getPort());
                new MyWebServerThread(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MyWebServerThread extends Thread {
    private Socket socket;

    MyWebServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run(){
        try {
            InputStreamReader inputStreamReader=new InputStreamReader(socket.getInputStream());
            char[] bs = new char[2048];
            BufferedReader fromClient=new BufferedReader(inputStreamReader);
            String requestMessageLine=fromClient.readLine();
            DataOutputStream outputStream=new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
