package org.apache.ws.echosample;
public interface Echo  extends java.rmi.Remote {
	public void echoVoid()throws java.rmi.RemoteException;
	public int echoInt(int in)throws java.rmi.RemoteException;
	public double echoDouble(double in)throws java.rmi.RemoteException;
	public float echoFloat(float in)throws java.rmi.RemoteException;
	public boolean echoBoolean(boolean in)throws java.rmi.RemoteException;
	public String echoString(String in)throws java.rmi.RemoteException;
	public short echoShort(short in)throws java.rmi.RemoteException;
	public long echoLong(long in)throws java.rmi.RemoteException;
	//public char echoChar(char in);
	public byte[] echoBytes(byte[] in)throws java.rmi.RemoteException;
	public void echoEvoid()throws java.rmi.RemoteException;
	public EchoStruct echoStruct(EchoStruct in)throws java.rmi.RemoteException;
	//public EchoStruct[] echoAStruct(EchoStruct[] in)throws java.rmi.RemoteException;
	
}
