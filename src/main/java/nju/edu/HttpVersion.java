package nju.edu;

/**
 * Created by lujxu on 2017/12/7.
 */
public enum HttpVersion {
    HTTP_1_0("HTTP/1.0"),HTTP_1_1("HTTP/1.1");

    private String name;

     HttpVersion(String name){
        this.name=name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {

        return String.valueOf(this.name);

    }


}
