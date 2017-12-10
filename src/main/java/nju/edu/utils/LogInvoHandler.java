package nju.edu.utils;

import nju.edu.server.impl.HttpResponseImpl;

import java.lang.reflect.*;

/**
 * Created by SuperSY on 2017/12/10.
 */
public class LogInvoHandler implements InvocationHandler{
    private Object target ; //目标
    private LogInvoHandler(){}
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(target,args) ;

        //处理完response，添加日志记录
        afterHandle();

        return result ;
    }
    public static<T> T getProxyInstance(T target){
        LogInvoHandler invoHandler = new LogInvoHandler() ;
        invoHandler.setTarget(target);
        return (T) Proxy.newProxyInstance(invoHandler.getClass().getClassLoader(), target.getClass().getInterfaces(), invoHandler);
    }

    public void setTarget(Object target) {
        this.target = target;
    }
    private void afterHandle(){
        LogUtils.writeLog(((HttpResponseImpl)target).fromLog());
    }
}
