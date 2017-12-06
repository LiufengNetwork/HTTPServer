package nju.edu.server;

import nju.edu.HttpMethod;
import nju.edu.HttpUtils;
import nju.edu.HttpVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by lujxu on 2017/12/8.
 */
public class HttpRequest {
    //url "？"后的参数
    private String queryString;
    private HttpMethod method;
    private  String uri;
    private HttpVersion version;
    /**
     * 首部行
     */
    private Properties header;

    private InputStream inputStream;

    public HttpRequest(InputStream inputStream) throws IOException {
        this.inputStream=inputStream;
        this.constructRequest();
    }

    public HttpRequest(HttpMethod method, String url, HttpVersion version) {
        this.method = method;
        this.version = version;
        int idx = url.indexOf('?');
        if (idx > 0) {
            uri = url.substring(0, idx);
            queryString = url.substring(idx + 1);
        } else {
            uri = url;
            queryString = null;
        }
    }

    //TODO throw IOException?
    private HttpRequest constructRequest() throws IOException {
        BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
        String requestLine=reader.readLine();
        List<String> params=splitLine(requestLine);
        if (params.size()>=3) {
            HttpMethod method = HttpMethod.valueOf(params.get(0).toUpperCase());
            HttpVersion version = HttpVersion.valueOf("HTTP/1.0");
//            HttpVersion version = HttpVersion.valueOf(params.get(2).toUpperCase());
            if (!isValidMethod(method, version)) {
                //TODO 505
            }
        }else{
            //TODO 不合法的request
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

    /**
     * 将字符串按sp（空格）切分
     * @param line
     * @return
     */
    private List<String> splitLine(String line){
        List<String> list=new ArrayList<>();
        final int length = line.length();
        int start = HttpUtils.findNonWhitespace(line, 0);
        int end = HttpUtils.findWhitespace(line, start);
        while (start<end) {
            String temp = line.substring(start, end);
            list.add(temp);
            start=HttpUtils.findNonWhitespace(line, end);
            end=HttpUtils.findWhitespace(line,start);
        }
        return  list;
    }

    public String getQueryString() {
        return queryString;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public Properties getHeader() {
        return header;
    }
}
