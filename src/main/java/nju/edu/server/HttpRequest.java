package nju.edu.server;

import nju.edu.HttpMethod;
import nju.edu.HttpVersion;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by SuperSY on 2017/12/10.
 */
public interface HttpRequest {
    public void constructRequest()  throws IOException;
    public HttpMethod getMethod() ;
    public Properties getHeader() ;
    public String getUri() ;
    public String getBody() ;
    public String getQueryString() ;
    public HttpVersion getVersion() ;
}
