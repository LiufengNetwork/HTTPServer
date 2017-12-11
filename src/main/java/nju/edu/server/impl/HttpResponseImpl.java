package nju.edu.server.impl;

import nju.edu.HttpMethod;
import nju.edu.HttpStatus;
import nju.edu.server.HttpRequest;
import nju.edu.server.HttpResponse;
import nju.edu.server.HttpWriter;

import java.io.*;
import java.net.Socket;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

import static nju.edu.utils.HttpUtils.*;

public class HttpResponseImpl implements HttpResponse {

    private String root;//服务器根目录
    private String localURI = null;//资源绝对地址
    private Socket socket;
    private HttpRequest request;
    private HttpWriter httpWriter;

    public HttpResponseImpl(Socket socket, HttpRequest request, HttpWriter httpWriter) {
        this.socket = socket;
        this.request = request;
        this.root = System.getProperty("user.dir") + "/src/main/resource";
        this.httpWriter = httpWriter;
    }

    public boolean response() {
        boolean isSuccess;

        //根据请求类型进行操作
        try {
            HttpMethod method = request.getMethod();

            if (method == null) {
                return true;
            }

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
                case DELETE:
                    isSuccess = doDelete();
                    break;
                default:
                    //请求出现错误? 服务器返回400 Bad Request
                    httpWriter.badRequest();
                    isSuccess = true;
            }

            //finally close outputstream
            httpWriter.close();
        } catch (Exception e) {
            try {
                httpWriter.intervalError();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                isSuccess = true;
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
                httpWriter.setResponseHeader(HttpStatus.OK);
                File file = new File(localURI);
                httpWriter.writeLastModified(Instant.ofEpochMilli(file.lastModified()));
                httpWriter.endHeader();
                InputStream input = new BufferedInputStream(new FileInputStream(localURI));
                //静态网页,将本地文件作为消息体传送
                byte[] data = new byte[1024];
                while (input.read(data) != -1) {
                    httpWriter.writeBody(data);
                }
                input.close();
            } else {
                //资源未修改，返回304
                httpWriter.setResponseHeader(HttpStatus.NOT_MODIFIED);
                httpWriter.endHeader();
            }

            isSuccess = true;

        } else {
            //资源不存在，服务器返回404 Not Found
            httpWriter.setResponseHeader(HttpStatus.NOT_FOUND);
            httpWriter.endHeader();
            showNotFoundPage();
            isSuccess = true;
        }
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
        处理post请求，创建资源
        1.如果查询参数 _ulcr 的值指定为 1，返回资源链接
        2.如果查询参数 _ulcr 的值指定为 0，返回资源内容
        处理post请求，更新资源
     */
    private boolean doPost() throws IOException {
        boolean isSuccess = false;

        //Change 为更新资源，AddChange 为创建资源
        Properties p = request.getHeader();
        String _action = p.getProperty("_action");

        //如果_action值为AddChange，或者未设置，则默认为创建资源
        if (_action == null || _action.equals("AddChange")) {
            String _ulcr = p.getProperty("_ulcr");

            //若资源存在
            if (parseURI()) {
                httpWriter.setResponseHeader(HttpStatus.OK);
                httpWriter.endHeader();

                String res = "resource exist!";
                httpWriter.writeBody(res.getBytes(UTF_8));

                return true;
            }

            //若资源不存在，则创建资源
            byte[] result = createResource();

            //返回资源内容
            if (Integer.parseInt(_ulcr) == 0) {
                httpWriter.setResponseHeader(HttpStatus.OK);
                httpWriter.endHeader();
                httpWriter.writeBody(result);

                isSuccess = true;
            } else {//返回资源链接
                httpWriter.setResponseHeader(HttpStatus.CREATED);
                httpWriter.writeHeader(LOCATION, request.getUri());
                httpWriter.endHeader();

                isSuccess = true;
            }
        }

        return isSuccess;
    }

    /**
     * create resource which post by http
     * @throws IOException
     */
    private byte[] createResource() throws IOException {
        byte[] result = null;

        BufferedReader body = request.getBody();
        Properties headers = request.getHeader();
        File f = new File(localURI);
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(f));
        int newread = 0;
        int totalread = 0;
        int contentLength = Integer.parseInt(headers.getProperty(camelCase(CONTENT_LENGTH)));

        char[] bytes = new char[contentLength];

        while (totalread < contentLength) {
            newread = body.read(bytes, totalread, contentLength - totalread);
            totalread += newread;
        }
        result = new String(bytes).getBytes(UTF_8);
        outputStream.write(result);
        outputStream.close();

        return result;
    }

    private boolean doPut() throws IOException {
        return doPost();
    }

    private boolean doDelete() throws IOException {
        if (parseURI()) {
            File f = new File(localURI);
            f.delete();

            httpWriter.setResponseHeader(HttpStatus.OK);
            httpWriter.endHeader();
        } else {
            httpWriter.setResponseHeader(HttpStatus.INTERNAL_SERVER_ERROR);
            httpWriter.endHeader();
        }
        return true;
    }

    /*
        处理head请求，只返回响应头部
     */
    private boolean doHead() throws IOException {
        if (parseURI()) {
            httpWriter.setResponseHeader(HttpStatus.OK);
        } else {
            httpWriter.setResponseHeader(HttpStatus.NOT_FOUND);
        }
        httpWriter.endHeader();

        return true;
    }

    /**
     * parse the uri, get the resource path
     * @return
     */
    private boolean parseURI() {
        StringBuffer sb = new StringBuffer(root);
        //resourceName应该为请求资源的相对路径
        String resourceName = request.getUri();

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

    /**
     * 返回404 not found 页面
     */
    private void showNotFoundPage() throws IOException {
        String path = root + "/404/index.html";
        File file = new File(path);
        byte[] data = readContent(file, new Long(file.length()).intValue());
        httpWriter.writeBody(data);
    }

    /**
     * 构造日志行
     *
     * @return 如：0:0:0:0:0:0:0:1 - - [Sun, 10 Dec 2017 08:19:05 GMT] "GET /test/b.html HTTP/1.1" 200
     */
    public String fromLog() {
        if (request.getMethod() == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        //客户端地址
        sb.append(socket.getInetAddress().getHostAddress());
        sb.append(" - - ");
        //时间
        sb.append("[" + new Date() + "] ");

        //方法
        sb.append("\"" + request.getMethod().toString() + " ");

        //uri
        sb.append(request.getUri());
        //参数
        if (request.getQueryString() != null) {
            sb.append("?" + request.getQueryString());
        }
        sb.append(" ");

        //版本
        sb.append(request.getVersion().toString() + "\" ");

        sb.append(httpWriter.getStatus().getCode() + "\r\n");
        return sb.toString();
    }
}
