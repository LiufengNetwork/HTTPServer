package nju.edu.server;

import nju.edu.HttpMethod;
import nju.edu.HttpVersion;

/**
 * Created by lujxu on 2017/12/8.
 */
public class HttpRequest {
    //url "？"后的参数
    private String queryString;
    private HttpMethod method;
    private  String uri;
    private HttpVersion version;

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
}
