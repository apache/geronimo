/**
 * InteropTest2PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.apache.notgeronimo.itests.naming.common.webservice.interop;

import org.apache.notgeronimo.itests.naming.common.webservice.interop.types.SOAPStruct;
import org.apache.notgeronimo.itests.naming.common.webservice.interop.types.SOAPStructStruct;
import org.apache.notgeronimo.itests.naming.common.webservice.interop.types.SOAPArrayStruct;
import org.apache.notgeronimo.itests.naming.common.webservice.interop.types.SOAPStructStruct;

public interface InteropTest2PortType extends java.rmi.Remote {
    public void echoVoid() throws java.rmi.RemoteException;
    public java.lang.String echoString(java.lang.String inputString) throws java.rmi.RemoteException;
    public java.lang.String[] echoStringArray(java.lang.String[] inputStringArray) throws java.rmi.RemoteException;
    public int echoInteger(int inputInteger) throws java.rmi.RemoteException;
    public int[] echoIntegerArray(int[] inputIntegerArray) throws java.rmi.RemoteException;
    public float echoFloat(float inputFloat) throws java.rmi.RemoteException;
    public float[] echoFloatArray(float[] inputFloatArray) throws java.rmi.RemoteException;
    public SOAPStruct echoStruct(SOAPStruct inputStruct) throws java.rmi.RemoteException;
    public SOAPStruct[] echoStructArray(SOAPStruct[] inputStructArray) throws java.rmi.RemoteException;
    public java.util.Calendar echoDate(java.util.Calendar inputDate) throws java.rmi.RemoteException;
    public byte[] echoBase64(byte[] inputBase64) throws java.rmi.RemoteException;
    public boolean echoBoolean(boolean inputBoolean) throws java.rmi.RemoteException;
    public java.math.BigDecimal echoDecimal(java.math.BigDecimal inputDecimal) throws java.rmi.RemoteException;
    public byte[] echoHexBinary(byte[] inputHexBinary) throws java.rmi.RemoteException;
    public void echoStructAsSimpleTypes(SOAPStruct inputStruct, javax.xml.rpc.holders.StringHolder outputString, javax.xml.rpc.holders.IntHolder outputInteger, javax.xml.rpc.holders.FloatHolder outputFloat) throws java.rmi.RemoteException;
    public SOAPStruct echoSimpleTypesAsStruct(java.lang.String inputString, int inputInteger, float inputFloat) throws java.rmi.RemoteException;
    public java.lang.String[] echo2DStringArray(java.lang.String[] input2DStringArray) throws java.rmi.RemoteException;
    public SOAPStructStruct echoNestedStruct(SOAPStructStruct inputStruct) throws java.rmi.RemoteException;
    public SOAPArrayStruct echoNestedArray(SOAPArrayStruct inputStruct) throws java.rmi.RemoteException;
}
