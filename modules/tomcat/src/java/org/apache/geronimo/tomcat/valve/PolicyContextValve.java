package org.apache.geronimo.tomcat.valve;

import org.apache.catalina.valves.ValveBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import javax.servlet.ServletException;
import javax.security.jacc.PolicyContext;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jeffgenender
 * Date: Feb 5, 2005
 * Time: 6:00:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class PolicyContextValve extends ValveBase{

    private final String policyContextID;

    public PolicyContextValve(String policyContextID){
        this.policyContextID = policyContextID;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {

        String oldId = PolicyContext.getContextID();

        PolicyContext.setContextID(policyContextID);

        // Pass this request on to the next valve in our pipeline
        getNext().invoke(request, response);

        PolicyContext.setContextID(oldId);
    }
}
