package nju.edu.server;

import nju.edu.HttpMethod;
import nju.edu.HttpStatus;

import java.io.*;
import java.net.Socket;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static nju.edu.HttpUtils.*;

public class HttpResponse {

    private String root = "D:\\Resource";//服务器根目录
    private String localURI = null;//资源绝对地址
    private Socket socket;
    private HttpRequest request;
    private DataOutputStream outputStream;

    public HttpResponse(Socket socket, HttpRequest request) {
        this.socket = socket;
        this.request = request;
    }

    public boolean response() {

        boolean isSuccess;

        //根据请求类型进行操作
        try {
            HttpMethod method = request.getMethod();
            switch (method) {
                case GET:
                    isSuccess = doGet();
                    break;
                case POST:
                    isSuccess = doPost();
                    break;
                case HEAD:
                    isSuccess = doHead();
                    break;
                case PUT:
                    isSuccess = doPut();
                    break;
                default:
                    //请求出现错误? 服务器返回400 Bad Request
                    setResponseHead(HttpStatus.BAD_REQUEST);
                    isSuccess = true;
            }
        } catch (IOException e) {
            try {
                setResponseHead(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                return false;
            }

        }

        return isSuccess;
    }

    /*
        处理get请求，返回资源的内容
     */
    private boolean doGet() throws IOException {

        boolean isSuccess = false;

        //判断资源是否存在
        if (parseURI()) {
            if (isModified()) {
                //资源被修改，重新读取资源
                setResponseHead(HttpStatus.OK);
                File file = new File(localURI);
                writeLastModified(Instant.ofEpochMilli(file.lastModified()));
                writeEnd();
                InputStream input = new BufferedInputStream(new FileInputStream(localURI));
                //静态网页,将本地文件作为消息体传送
                byte[] data = new byte[1024];
                while (input.read(data) != -1) {
                    outputStream.write(data);
                    outputStream.flush();
                }
                input.close();
            } else {
                //资源未修改，返回304
                setResponseHead(HttpStatus.NOT_MODIFIED);
            }

            isSuccess = true;

        } else {
            //资源不存在，服务器返回404 Not Found
            setResponseHead(HttpStatus.NOT_FOUND);
            writeEnd();
            InputStream input = new BufferedInputStream(new FileInputStream(root + "/404/404.htm"));
            //静态网页,将本地文件作为消息体传送
            byte[] data = new byte[1024];
            while (input.read(data) != -1) {
                outputStream.write(data);
                outputStream.flush();
            }
            isSuccess = true;
        }

        outputStream.close();
        return isSuccess;
    }

    /**
     * check the resource is whether modified
     *
     * @return
     */
    private boolean isModified() {
        Properties headers = request.getHeader();
        String modifySince = headers.getProperty(IF_MODIFIED_SINCE);
        File file = new File(localURI);

        if (modifySince != null && modifySince.equals(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("GMT"))))) {
            return false;
        }
        return true;
    }

    /*
        设置response的首部
     */
    private void setResponseHead(HttpStatus httpStatus) throws IOException {
        outputStream = new DataOutputStream(socket.getOutputStream());
        outputStream.write(httpStatus.getInitialLineBytes());
    }

    /**
     * Writes out the "Last-modified" header entry to the response output stream, appending "\r\n".
     * <p>
     * The date is formatted using {@code DateTimeFormatter.RFC_1123_DATE_TIME}.
     *
     * @param instant the last-modified header entry value
     * @throws IOException indicating an error occurred while writing out to the output stream
     */
    public void writeLastModified(Instant instant) throws IOException {
        writeHeader(LAST_MODIFIED, instant == null ? "Never" :
                DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(instant, ZoneId.of("GMT"))));
    }

    /**
     * Writes out a header entry to the response output stream, appending "\r\n".
     *
     * @param key   the header entry key
     * @param value the header entry value
     * @throws IOException indicating an error occurred while writing out to the output stream
     */
    public void writeHeader(String key, String value) throws IOException {
        writeHeader(key + ":" + value);
    }

    /**
     * Writes out a header entry to the response output stream, appending "\r\n".
     *
     * @param header the header entry
     * @throws IOException indicating an error occurred while writing out to the output stream
     */
    public void writeHeader(String header) throws IOException {
        outputStream.write((header + "\r\n").getBytes(ASCII));
    }

    /**
     * write head end "\r\n"
     *
     * @throws IOException
     */
    private void writeEnd() throws IOException {
        outputStream.write(CR);
        outputStream.write(LF);
    }

    /*
        处理post请求，创建资源
        1.如果查询参数 _ulcr 的值指定为 1，返回资源链接
        2.如果查询参数 _ulcr 的值指定为 0，返回资源内容
        处理post请求，更新资源
     */
    private boolean doPost() throws IOException {
        boolean isSuccess = false;

        //Change 为更新资源，AddChange 为创建资源
        Properties p = request.getHeader();
        String _action = p.getProperty("_action").trim();

        //如果_action值为AddChange，或者未设置，则默认为创建资源
        if (_action.equals("AddChange") || _action == null) {
            String _ulcr = p.getProperty("_ulcr").trim();

            //若资源存在
            if (parseURI()) {
                setResponseHead(HttpStatus.OK);
                writeEnd();

                String res = "资源文件已存在！";
                outputStream.write(res.getBytes());
                outputStream.flush();

                return true;
            }

            //若资源不存在，则创建资源
            createResource();

            //返回资源内容
            if (Integer.parseInt(_ulcr) == 0) {
                setResponseHead(HttpStatus.OK);
                writeEnd();

                InputStream input = new BufferedInputStream(new FileInputStream(localURI));
                //静态网页,将本地文件作为消息体传送
                byte[] data = new byte[1024];
                int length = -1;
                while ((length = input.read(data)) != -1) {
                    outputStream.write(data);
                    outputStream.flush();
                }

                isSuccess = true;
            } else {//返回资源链接
                outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.write(HttpStatus.CREATED.getInitialLineBytes());

                String Location = "Location: " + request.getUri() + "\r\n";
                outputStream.write(Location.getBytes());

                outputStream.write(CR);
                outputStream.write(LF);

                isSuccess = true;
            }
        }

        return isSuccess;
    }

    private void createResource() throws IOException {
        String body = request.getBody();

        File f = new File(localURI);
        OutputStream outputStream = new FileOutputStream(f);

        outputStream.write(body.getBytes());

        outputStream.close();

    }

    private boolean doPut() throws IOException {
        return doPost();
    }

    /*
        处理head请求，只返回响应头部
     */
    private boolean doHead() throws IOException {

        boolean isSuccess = false;

        if (parseURI()) {
            setResponseHead(HttpStatus.OK);
        } else {
            setResponseHead(HttpStatus.NOT_FOUND);
        }
        writeEnd();

        return true;
    }

    private boolean parseURI() {
        StringBuffer sb = new StringBuffer(root);
        //resourceName应该为请求资源的相对路径
        String resourceName = request.getUri();

        resourceName = resourceName.replace("/", "\\");

        sb.append(resourceName);
        localURI = sb.toString();

        //判断资源是否存在
        File f = new File(localURI);
        if (f.exists() && f.isFile()) {
            return true;
        } else {
            return false;
        }
    }

}
