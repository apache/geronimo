/**
 * InteropTest2PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.apache.geronimo.axis.builder.interop;

public interface InteropTest2PortType extends java.rmi.Remote {
    public void echoVoid() throws java.rmi.RemoteException;
    public java.lang.String echoString(java.lang.String inputString) throws java.rmi.RemoteException;
    public java.lang.String[] echoStringArray(java.lang.String[] inputStringArray) throws java.rmi.RemoteException;
    public int echoInteger(int inputInteger) throws java.rmi.RemoteException;
    public int[] echoIntegerArray(int[] inputIntegerArray) throws java.rmi.RemoteException;
    public float echoFloat(float inputFloat) throws java.rmi.RemoteException;
    public float[] echoFloatArray(float[] inputFloatArray) throws java.rmi.RemoteException;
    public org.apache.geronimo.axis.builder.interop.types.SOAPStruct echoStruct(org.apache.geronimo.axis.builder.interop.types.SOAPStruct inputStruct) throws java.rmi.RemoteException;
    public org.apache.geronimo.axis.builder.interop.types.SOAPStruct[] echoStructArray(org.apache.geronimo.axis.builder.interop.types.SOAPStruct[] inputStructArray) throws java.rmi.RemoteException;
    public java.util.Calendar echoDate(java.util.Calendar inputDate) throws java.rmi.RemoteException;
    public byte[] echoBase64(byte[] inputBase64) throws java.rmi.RemoteException;
    public boolean echoBoolean(boolean inputBoolean) throws java.rmi.RemoteException;
    public java.math.BigDecimal echoDecimal(java.math.BigDecimal inputDecimal) throws java.rmi.RemoteException;
    public byte[] echoHexBinary(byte[] inputHexBinary) throws java.rmi.RemoteException;
    public void echoStructAsSimpleTypes(org.apache.geronimo.axis.builder.interop.types.SOAPStruct inputStruct, javax.xml.rpc.holders.StringHolder outputString, javax.xml.rpc.holders.IntHolder outputInteger, javax.xml.rpc.holders.FloatHolder outputFloat) throws java.rmi.RemoteException;
    public org.apache.geronimo.axis.builder.interop.types.SOAPStruct echoSimpleTypesAsStruct(java.lang.String inputString, int inputInteger, float inputFloat) throws java.rmi.RemoteException;
    public java.lang.String[] echo2DStringArray(java.lang.String[] input2DStringArray) throws java.rmi.RemoteException;
    public org.apache.geronimo.axis.builder.interop.types.SOAPStructStruct echoNestedStruct(org.apache.geronimo.axis.builder.interop.types.SOAPStructStruct inputStruct) throws java.rmi.RemoteException;
    public org.apache.geronimo.axis.builder.interop.types.SOAPArrayStruct echoNestedArray(org.apache.geronimo.axis.builder.interop.types.SOAPArrayStruct inputStruct) throws java.rmi.RemoteException;
}
