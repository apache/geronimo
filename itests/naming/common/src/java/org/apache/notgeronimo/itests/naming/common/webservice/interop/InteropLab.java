/**
 * InteropLab.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.apache.notgeronimo.itests.naming.common.webservice.interop;

public interface InteropLab extends javax.xml.rpc.Service {
//    public java.lang.String getinteropTestPortAddress();

    public org.apache.notgeronimo.itests.naming.common.webservice.interop.InteropTestPortType getinteropTestPort() throws javax.xml.rpc.ServiceException;

//    public org.apache.geronimo.axis.builder.interop.InteropTestPortType getinteropTestPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
//    public java.lang.String getinteropTest2PortAddress();

    public org.apache.notgeronimo.itests.naming.common.webservice.interop.InteropTest2PortType getinteropTest2Port() throws javax.xml.rpc.ServiceException;

//    public org.apache.geronimo.axis.builder.interop.InteropTest2PortType getinteropTest2Port(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
