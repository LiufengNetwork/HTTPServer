package nju.edu.server.impl;

import nju.edu.HttpMethod;
import nju.edu.server.MalformedRequestException;
import nju.edu.utils.HttpUtils;
import nju.edu.HttpVersion;
import nju.edu.server.HttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static nju.edu.utils.HttpUtils.LF;  //\n
import static nju.edu.utils.HttpUtils.CR; //\r

/**
 * Created by lujxu on 2017/12/8.
 */
public class HttpRequestImpl implements HttpRequest {
    //url "？"后的参数
    private String queryString;
    private HttpMethod method;
    private String uri;
    private HttpVersion version;
    /**
     * 首部行
     */
    private Properties header;
    private BufferedReader body;

    private InputStream inputStream;

    public HttpRequestImpl(InputStream inputStream) throws IOException, MalformedRequestException {
        this.inputStream = inputStream;
        this.header = new Properties();
        this.constructRequest();
    }

    //TODO throw IOException?
    public void constructRequest() throws IOException, MalformedRequestException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        //解析请求行 request line
        String requestLine = reader.readLine();
        List<String> params = splitLine(requestLine);
        if (params.size() >= 3) {
            this.method = HttpMethod.valueOf(params.get(0).toUpperCase());
            this.version = HttpVersion.HTTP_1_0;
            String versionStr = params.get(2).toUpperCase();
            if (versionStr.equals(HttpVersion.HTTP_1_1.toString())) {
                this.version = HttpVersion.HTTP_1_1;
            }
            if (!isValidMethod(this.method, this.version)) {
                //TODO 505
            }
            splitUrl(params.get(1));
        } else {
            //不合法的request
            return;
//            throw new MalformedRequestException("bad request");
        }
        //解析头部行 header line
        constructHeader(reader);
        //构造entity body
        if (HttpUtils.hasBody(this.method)) {
            constructEntityBody(reader);
        }
    }

    /**
     * 验证http method是否合法，http 1.0仅允许get, post和head
     *
     * @param method
     * @param version
     * @return
     */
    private boolean isValidMethod(HttpMethod method, HttpVersion version) {
        if (version.equals(HttpVersion.HTTP_1_0)) {
            if (method.equals(HttpMethod.GET) || method.equals(HttpMethod.POST) || method.equals(HttpMethod.HEAD)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 将字符串按sp（空格）切分
     *
     * @param line
     * @return
     */
    private List<String> splitLine(String line) {
        List<String> list = new ArrayList<>();
        if (line == null || line.isEmpty())
            return list;
        final int length = line.length();
        int start = HttpUtils.findNonWhitespace(line, 0);
        int end = HttpUtils.findWhitespace(line, start);
        while (start < end) {
            String temp = line.substring(start, end);
            list.add(temp);
            start = HttpUtils.findNonWhitespace(line, end);
            end = HttpUtils.findWhitespace(line, start);
        }
        return list;
    }

    /**
     * 将字符串按colon（冒号）切分
     *
     * @param line
     * @return
     */
    private List<String> splitHeaderLine(String line) {
        List<String> list = new ArrayList<>();
        if (line == null || line.isEmpty())
            return list;
        int index = HttpUtils.findColonIndex(line, 0);
        list.add(line.substring(0, index).trim());
        list.add(line.substring(index + 1, line.length()).trim());
        return list;
    }

    private void splitUrl(String url) {
        int idx = url.indexOf('?');
        if (idx > 0) {
            this.uri = url.substring(0, idx);
            this.queryString = url.substring(idx + 1);
        } else {
            this.uri = url;
            this.queryString = null;
        }
    }

    /**
     * construct header
     *
     * @param reader
     * @throws IOException
     */
    private void constructHeader(BufferedReader reader) throws IOException {
        String temp = "";
        while (reader != null && (temp = reader.readLine()) != null) {
            if (temp.length() < 2 || (temp.length() >= 2 && temp.charAt(0) == CR && temp.charAt(1) == LF)) {
                break;
            }
            List<String> list = splitHeaderLine(temp);
            if (list.size() >= 2) {
                this.header.put(HttpUtils.camelCase(list.get(0)), list.get(1));
            }
        }
    }

    private void constructEntityBody(BufferedReader reader) throws IOException {
        this.body = reader;
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

    public BufferedReader getBody() {
        return body;
    }
}
