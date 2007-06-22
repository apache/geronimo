/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.testsuite.corba.marshal;

import javax.ejb.SessionContext;
import javax.ejb.SessionBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class MarshalEJBSessionBean implements SessionBean {

    private SessionContext ctx;

    public void setSessionContext(SessionContext ctx) {
        this.ctx = ctx;
    }

    public void ejbCreate() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() {
    }

    //
    // Methods specifically used to test the marshalling and unmarshalling of java primitives
    // and arrays of java primitives
    //
    public boolean marshalBoolean(boolean p) {
        return p;
    }
    public boolean[] marshalBoolean(boolean[] p) {
        return p;
    }
    public boolean[][] marshalBoolean(boolean[][] p) {
        return p;
    }
    public boolean[][][] marshalBoolean(boolean[][][] p) {
        return p;
    }

    public byte marshalByte(byte p) {
        return p;
    }
    public byte[] marshalByte(byte[] p) {
        return p;
    }
    public byte[][] marshalByte(byte[][] p) {
        return p;
    }
    public byte[][][] marshalByte(byte[][][] p) {
        return p;
    }

    public char marshalChar(char p) {
        return p;
    }
    public char[] marshalChar(char[] p) {
        return p;
    }
    public char[][] marshalChar(char[][] p) {
        return p;
    }
    public char[][][] marshalChar(char[][][] p) {
        return p;
    }

    public short marshalShort(short p) {
        return p;
    }
    public short[] marshalShort(short[] p) {
        return p;
    }
    public short[][] marshalShort(short[][] p) {
        return p;
    }
    public short[][][] marshalShort(short[][][] p) {
        return p;
    }

    public int marshalInt(int p) {
        return p;
    }
    public int[] marshalInt(int[] p) {
        return p;
    }
    public int[][] marshalInt(int[][] p) {
        return p;
    }
    public int[][][] marshalInt(int[][][] p) {
        return p;
    }

    public long marshalLong(long p) {
        return p;
    }
    public long[] marshalLong(long[] p) {
        return p;
    }
    public long[][] marshalLong(long[][] p) {
        return p;
    }
    public long[][][] marshalLong(long[][][] p) {
        return p;
    }

    public float marshalFloat(float p) {
        return p;
    }
    public float[] marshalFloat(float[] p) {
        return p;
    }
    public float[][] marshalFloat(float[][] p) {
        return p;
    }
    public float[][][] marshalFloat(float[][][] p) {
        return p;
    }

    public double marshalDouble(double p) {
        return p;
    }
    public double[] marshalDouble(double[] p) {
        return p;
    }
    public double[][] marshalDouble(double[][] p) {
        return p;
    }
    public double[][][] marshalDouble(double[][][] p) {
        return p;
    }

    //
    // Methods specifically used to test the marshalling and unmarshalling of java strings
    // and arrays of java strings
    //
    public String marshalString(String p) {
        return p;
    }
    public String[] marshalString(String[] p) {
        return p;
    }
    public String[][] marshalString(String[][] p) {
        return p;
    }
    public String[][][] marshalString(String[][][] p) {
        return p;
    }
    public ArrayList<String> marshalString(ArrayList<String> p) {
        return p;
    }
    public List<ArrayList<String>> marshalString(List<ArrayList<String>> p) {
        return p;
    }
    public Collection<List<ArrayList<String>>> marshalString(Collection<List<ArrayList<String>>> p) {
        return p;
    }
}
