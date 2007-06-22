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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ejb.EJBObject;

public interface MarshalEJBRemote extends EJBObject {

    public boolean marshalBoolean(boolean p) throws RemoteException;
    public boolean[] marshalBoolean(boolean[] p) throws RemoteException;
    public boolean[][] marshalBoolean(boolean[][] p) throws RemoteException;
    public boolean[][][] marshalBoolean(boolean[][][] p) throws RemoteException;

    public byte marshalByte(byte p) throws RemoteException;
    public byte[] marshalByte(byte[] p) throws RemoteException;
    public byte[][] marshalByte(byte[][] p) throws RemoteException;
    public byte[][][] marshalByte(byte[][][] p) throws RemoteException;

    public char marshalChar(char p) throws RemoteException;
    public char[] marshalChar(char[] p) throws RemoteException;
    public char[][] marshalChar(char[][] p) throws RemoteException;
    public char[][][] marshalChar(char[][][] p) throws RemoteException;

    public short marshalShort(short p) throws RemoteException;
    public short[] marshalShort(short[] p) throws RemoteException;
    public short[][] marshalShort(short[][] p) throws RemoteException;
    public short[][][] marshalShort(short[][][] p) throws RemoteException;

    public int marshalInt(int p) throws RemoteException;
    public int[] marshalInt(int[] p) throws RemoteException;
    public int[][] marshalInt(int[][] p) throws RemoteException;
    public int[][][] marshalInt(int[][][] p) throws RemoteException;

    public long marshalLong(long p) throws RemoteException;
    public long[] marshalLong(long[] p) throws RemoteException;
    public long[][] marshalLong(long[][] p) throws RemoteException;
    public long[][][] marshalLong(long[][][] p) throws RemoteException;

    public float marshalFloat(float p) throws RemoteException;
    public float[] marshalFloat(float[] p) throws RemoteException;
    public float[][] marshalFloat(float[][] p) throws RemoteException;
    public float[][][] marshalFloat(float[][][] p) throws RemoteException;

    public double marshalDouble(double p) throws RemoteException;
    public double[] marshalDouble(double[] p) throws RemoteException;
    public double[][] marshalDouble(double[][] p) throws RemoteException;
    public double[][][] marshalDouble(double[][][] p) throws RemoteException;

    public String marshalString(String p) throws RemoteException;
    public String[] marshalString(String[] p) throws RemoteException;
    public String[][] marshalString(String[][] p) throws RemoteException;
    public String[][][] marshalString(String[][][] p) throws RemoteException;

    public ArrayList<String> marshalString(ArrayList<String> p) throws RemoteException;
    public List<ArrayList<String>> marshalString(List<ArrayList<String>> p) throws RemoteException;
    public Collection<List<ArrayList<String>>> marshalString(Collection<List<ArrayList<String>>> p) throws RemoteException;
}
