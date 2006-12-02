package org.apache.geronimo.cxf;

import java.io.Serializable;
import java.util.List;

import org.apache.cxf.jaxws.javaee.PortComponentHandlerType;

public class PortInfo implements Serializable {

    private String serviceName;
    private String portName;
    private String seiInterfaceName;
    private String wsdlFile;
    private String servletLink;
    // TODO: will have to construct handlers when the container becomes active 
    //
    private List<PortComponentHandlerType> handlers;
   
    public String getPortName() {
        return portName;
    }
    public void setPortName(String pn) {
        portName = pn;
    }
    public String getServiceEndpointInterfaceName() {
        return seiInterfaceName;
    }
    public void setServiceEndpointInterfaceName(String sei) {
        seiInterfaceName = sei;
    }
    public String getServletLink() {
        return servletLink;
    }
    public void setServletLink(String sl) {
        servletLink = sl;
    }
    public String getWsdlFile() {
        return wsdlFile;
    }
    public void setWsdlFile(String wf) {
        wsdlFile = wf;
    }
    
    public void setHandlers(List<PortComponentHandlerType> h) {
        handlers = h;
    }
    
    public List<PortComponentHandlerType> getHandlers() {
        return handlers;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String sn) {
        serviceName = sn;        
    }

    /*
    private String serviceName;
    private String portName;
    private String seiInterfaceName;
    private String wsdlFile;
    private String servletLink;
     */
    public String toString() {
        return "[" + serviceName + ":" + portName + ":" + seiInterfaceName + ":" + wsdlFile + "]";
    }
}
