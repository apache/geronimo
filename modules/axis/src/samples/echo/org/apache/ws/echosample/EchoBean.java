package org.apache.ws.echosample;

public class EchoBean implements javax.ejb.SessionBean {
    public void ejbCreate() {
    }

    public void echoVoid() {
    }

    public int echoInt(int in) {
        return in;
    }

    public double echoDouble(double in) {
        return in;
    }

    public float echoFloat(float in) {
        return in;
    }

    public boolean echoBoolean(boolean in) {
        return in;
    }

    public String echoString(String in) {
        return in;
    }

    public short echoShort(short in) {
        return in;
    }

    public long echoLong(long in) {
        return in;
    }

    public char echoChar(char in) {
        return in;
    }

    public byte[] echoBytes(byte[] in) {
        return in;
    }

    public void echoEvoid() {
    }

    public EchoStruct echoStruct(EchoStruct in) {
        return in;
    }
//	public EchoStruct[] echoAStruct(EchoStruct[] in){
//		return in;
//	}
		
    public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }

    public void setSessionContext(javax.ejb.SessionContext arg0) throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
}
