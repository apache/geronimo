package org.apache.geronimo.tomcat.listener;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;

public class DispatchListener implements InstanceListener{

    public void instanceEvent(InstanceEvent event) {
        
        if (event.equals(event.BEFORE_DISPATCH_EVENT)){
            beforeDispatch(event.getRequest(), event.getResponse());
        }
        if (event.equals(event.AFTER_DISPATCH_EVENT)){
            afterDispatch(event.getRequest(), event.getResponse());
        }
    }
    
    private void beforeDispatch(ServletRequest request, ServletResponse respons){
        
    }
    
    private void afterDispatch(ServletRequest request, ServletResponse respons){
        
    }

}
