package nju.edu.server;

import nju.edu.HttpMethod;
import nju.edu.HttpStatus;
import nju.edu.HttpUtils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class HttpResponse {

    private String root = "F:\\HTTPServerResources";//服务器根目录
    private String localURI = null;//资源绝对地址
    private Socket socket;
    private HttpRequest request;
    private DataOutputStream outputStream;

    public HttpResponse(Socket socket, HttpRequest request){
        this.socket = socket;
        this.request = request;
    }

    public boolean response(){

        boolean isSuccess;

        //根据请求类型进行操作
        try {
            HttpMethod method = request.getMethod();
            switch(method){
                case GET: isSuccess = doGet(); break;
                case POST: isSuccess = doPost(); break;
                case HEAD: isSuccess = doHead(); break;
                case PUT: isSuccess = doPut(); break;
                default:
                    //请求出现错误? 服务器返回400 Bad Request
                    setResponseHead(HttpStatus.BAD_REQUEST);
                    isSuccess = true;
            }
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return isSuccess;
    }

    /*
        处理get请求，返回资源的内容
     */
    private boolean doGet() throws IOException{

        boolean isSuccess = false;

        //判断资源是否存在
        if(parseURI()){
            setResponseHead(HttpStatus.OK);

            InputStream input = new BufferedInputStream(new FileInputStream(localURI));
            //静态网页,将本地文件作为消息体传送
            byte[] data = new byte[1024];
            int length = -1;
            while((length = input.read(data)) != -1){
                outputStream.write(data);
                outputStream.flush();
            }

            isSuccess = true;

        }else{
            //资源不存在，服务器返回404 Not Found
            setResponseHead(HttpStatus.NOT_FOUND);

            isSuccess = true;
        }

        return isSuccess;
    }

    /*
        设置response的首部
     */
    private void setResponseHead(HttpStatus httpStatus) throws IOException{
        outputStream = new DataOutputStream(socket.getOutputStream());
        outputStream.write(httpStatus.getInitialLineBytes());
        outputStream.write(HttpUtils.CR);
        outputStream.write(HttpUtils.LF);
    }

    /*
        处理post请求，创建资源
        1.如果查询参数 _ulcr 的值指定为 1，返回资源链接
        2.如果查询参数 _ulcr 的值指定为 0，返回资源内容
        处理post请求，更新资源
     */
    private boolean doPost() throws IOException{
        boolean isSuccess = false;

        //Change 为更新资源，AddChange 为创建资源
        Properties p = request.getHeader();
        String _action = p.getProperty("_action").trim();

        //如果_action值为AddChange，或者未设置，则默认为创建资源
        if(_action.equals("AddChange") || _action == null){
            String _ulcr = p.getProperty("_ulcr").trim();

            //若资源存在
            if(parseURI()){
                setResponseHead(HttpStatus.OK);

                String res = "资源文件已存在！";
                outputStream.write(res.getBytes());
                outputStream.flush();

                return true;
            }

            //若资源不存在，则创建资源
            createResource();

            //返回资源内容
            if(Integer.parseInt(_ulcr) == 0){
                setResponseHead(HttpStatus.OK);

                InputStream input = new BufferedInputStream(new FileInputStream(localURI));
                //静态网页,将本地文件作为消息体传送
                byte[] data = new byte[1024];
                int length = -1;
                while((length = input.read(data)) != -1){
                    outputStream.write(data);
                    outputStream.flush();
                }

                isSuccess = true;
            }else{//返回资源链接
                outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.write(HttpStatus.CREATED.getInitialLineBytes());

                String Location = "Location: "+request.getUri()+"\r\n";
                outputStream.write(Location.getBytes());

                outputStream.write(HttpUtils.CR);
                outputStream.write(HttpUtils.LF);

                isSuccess = true;
            }
        }

        return isSuccess;
    }

    private void createResource() throws IOException{
        String body = request.getBody();

        File f = new File(localURI);
        OutputStream outputStream = new FileOutputStream(f);

        outputStream.write(body.getBytes());

        outputStream.close();

    }

    private boolean doPut() throws IOException{
        return doPost();
    }

    /*
        处理head请求，只返回响应头部
     */
    private boolean doHead() throws IOException{

        boolean isSuccess = false;

        if(parseURI()){
            setResponseHead(HttpStatus.OK);
        }else{
            setResponseHead(HttpStatus.NOT_FOUND);
        }

        return true;
    }

    private boolean parseURI(){
        StringBuffer sb = new StringBuffer(root);
        //?????????????resourceName应该为请求资源的相对路径
        String resourceName = request.getUri();

        resourceName = resourceName.replace("/","\\");

        sb.append(resourceName);
        localURI = sb.toString();

        //判断资源是否存在
        File f = new File(localURI);
        if(f.exists() && f.isFile()){
            return true;
        }else{
            return false;
        }
    }

}
