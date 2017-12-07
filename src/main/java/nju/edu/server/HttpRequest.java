package nju.edu.server;

import nju.edu.HttpMethod;
import nju.edu.HttpVersion;

import java.util.Properties;

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
