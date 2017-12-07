package nju.edu.server;

import nju.edu.HttpStatus;
import nju.edu.HttpUtils;

import java.io.*;
import java.net.Socket;

public class HttpResponse {

    private String root = "F:\\HTTPServerResources";//服务器根目录
    private String localURI = null;//资源绝对地址
    private Socket socket;
    private HttpRequest request;

    public HttpResponse(Socket socket, HttpRequest request){
        this.socket = socket;
        this.request = request;
    }

    public boolean response(){

        if(parseURI()){
            //根据请求类型进行操作
        }else{
            //服务器返回404 Not Found
            try {
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.write(HttpStatus.NOT_FOUND.getInitialLineBytes());
                outputStream.write(HttpUtils.CR);
                outputStream.write(HttpUtils.LF);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    private boolean doGet() throws IOException{
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        outputStream.write(HttpStatus.OK.getInitialLineBytes());
        outputStream.write(HttpUtils.CR);
        outputStream.write(HttpUtils.LF);

        InputStream input = new BufferedInputStream(new FileInputStream(localURI));
        //静态网页,将本地文件作为消息体传送
        byte[] data = new byte[1024];
        int length = -1;
        while((length = input.read(data)) != -1){
            outputStream.write(data);
            outputStream.flush();
        }

        return true;
    }

    private boolean doPost() throws IOException{
        return doGet();
    }

    private boolean doPut(){
        return false;
    }

    private boolean doHead(){
        return false;
    }

    private boolean parseURI(){
        StringBuffer sb = new StringBuffer(root);
        String resourceName = "";
        sb.append("\\"+resourceName);
        localURI = sb.toString();

        //判断资源是否存在
        File f = new File(localURI);
        if(f.exists()){
            return true;
        }else{
            return false;
        }
    }

}
