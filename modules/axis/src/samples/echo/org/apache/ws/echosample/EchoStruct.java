package org.apache.ws.echosample;

import java.io.Serializable;

public class EchoStruct implements Serializable{
	private int intVal;
	private double doubleVal;
	private byte[] bytesVal;
	private float floatVal;
	private long longVal;
	private short shortVal;
	private boolean boolaenVal;
	
	private String strVal;
	
	private int[] intaVal;
	private double[] doubleaVal;
	private byte[][] bytesaVal;
	private float[] floataVal;
	private long[] longaVal;
	private short[] shortaVal;
	private boolean[] boolaenaVal;
	
	private String[] straVal;
	
	private SmallEchoStruct sturctVal;
	private SmallEchoStruct sturctaVal;
	
    /**
     * @return
     */
    public boolean[] getBoolaenaVal() {
        return boolaenaVal;
    }

    /**
     * @return
     */
    public boolean isBoolaenVal() {
        return boolaenVal;
    }

    /**
     * @return
     */
    public byte[][] getBytesaVal() {
        return bytesaVal;
    }

    /**
     * @return
     */
    public byte[] getBytesVal() {
        return bytesVal;
    }

   
   

    /**
     * @return
     */
    public double[] getDoubleaVal() {
        return doubleaVal;
    }

    /**
     * @return
     */
    public double getDoubleVal() {
        return doubleVal;
    }

    /**
     * @return
     */
    public float[] getFloataVal() {
        return floataVal;
    }

    /**
     * @return
     */
    public float getFloatVal() {
        return floatVal;
    }

    /**
     * @return
     */
    public int[] getIntaVal() {
        return intaVal;
    }

    /**
     * @return
     */
    public int getIntVal() {
        return intVal;
    }

    /**
     * @return
     */
    public long[] getLongaVal() {
        return longaVal;
    }

    /**
     * @return
     */
    public long getLongVal() {
        return longVal;
    }

    /**
     * @return
     */
    public short[] getShortaVal() {
        return shortaVal;
    }

    /**
     * @return
     */
    public short getShortVal() {
        return shortVal;
    }

    /**
     * @return
     */
    public String[] getStraVal() {
        return straVal;
    }

    /**
     * @return
     */
    public String getStrVal() {
        return strVal;
    }

    /**
     * @param bs
     */
    public void setBoolaenaVal(boolean[] bs) {
        boolaenaVal = bs;
    }

    /**
     * @param b
     */
    public void setBoolaenVal(boolean b) {
        boolaenVal = b;
    }

    /**
     * @param bs
     */
    public void setBytesaVal(byte[][] bs) {
        bytesaVal = bs;
    }

    /**
     * @param bs
     */
    public void setBytesVal(byte[] bs) {
        bytesVal = bs;
    }

    
    /**
     * @param ds
     */
    public void setDoubleaVal(double[] ds) {
        doubleaVal = ds;
    }

    /**
     * @param d
     */
    public void setDoubleVal(double d) {
        doubleVal = d;
    }

    /**
     * @param fs
     */
    public void setFloataVal(float[] fs) {
        floataVal = fs;
    }

    /**
     * @param f
     */
    public void setFloatVal(float f) {
        floatVal = f;
    }

    /**
     * @param is
     */
    public void setIntaVal(int[] is) {
        intaVal = is;
    }

    /**
     * @param i
     */
    public void setIntVal(int i) {
        intVal = i;
    }

    /**
     * @param ls
     */
    public void setLongaVal(long[] ls) {
        longaVal = ls;
    }

    /**
     * @param l
     */
    public void setLongVal(long l) {
        longVal = l;
    }

    /**
     * @param ses
     */
    public void setShortaVal(short[] ses) {
        shortaVal = ses;
    }

    /**
     * @param s
     */
    public void setShortVal(short s) {
        shortVal = s;
    }

    /**
     * @param strings
     */
    public void setStraVal(String[] strings) {
        straVal = strings;
    }

    /**
     * @param string
     */
    public void setStrVal(String string) {
        strVal = string;
    }

    /**
     * @return
     */
    public SmallEchoStruct getSturctaVal() {
        return sturctaVal;
    }

    /**
     * @return
     */
    public SmallEchoStruct getSturctVal() {
        return sturctVal;
    }

    /**
     * @param struct
     */
    public void setSturctaVal(SmallEchoStruct struct) {
        sturctaVal = struct;
    }

    /**
     * @param struct
     */
    public void setSturctVal(SmallEchoStruct struct) {
        sturctVal = struct;
    }

}
