package nju.edu.utils;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Created by SuperSY on 2017/12/10.
 */
public class LogUtils {
    public static String logPath = HttpUtils.rootPath+"\\log\\access.log" ;
    static FileWriter logWriter;
    public static void writeLog(String content){
        try {
            logPath = logPath+"."+ DateFormatter.format(new Date(),DateFormatter.webFormat)+".txt" ;
            if(logWriter==null){
                File file = new File(logPath) ;
                if(!file.getParentFile().exists()){
                   file.getParentFile().mkdirs() ;
                }
                logWriter = new FileWriter(logPath,true) ;
            }
            logWriter.write(content);
            logWriter.flush();
        } catch (IOException e) {
            System.out.println("写日志文件失败");
            e.printStackTrace();
        }
    }
    public static void closeLog(){
        try {
            if(logWriter!=null){
                logWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
