package org.apache.geronimo.tomcat.valve;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.geronimo.tomcat.interceptor.BeforeAfter;

public class GeronimoBeforeAfterValve extends ValveBase{
    
    private final BeforeAfter beforeAfter;
    private final int contextIndexCount;

    public GeronimoBeforeAfterValve(BeforeAfter beforeAfter, int contextIndexCount) {
        this.beforeAfter = beforeAfter;
        this.contextIndexCount = contextIndexCount;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        Object context[] = new Object[contextIndexCount];
        
        if (beforeAfter != null){
            beforeAfter.before(context, request, response);
        }
        
        // Pass this request on to the next valve in our pipeline
        getNext().invoke(request, response);
        
        if (beforeAfter != null){
            beforeAfter.after(context, request, response);
        }
        
    }

}
