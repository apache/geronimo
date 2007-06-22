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

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

public class JavaPrimitives {

    private static final int MAX_CHAR_VALUE=255;    // Use ASCII-1 and ASCII-2 sets only
    private static int maxArraySize;
    private static Random random;                   // Pseudo-random number generator

    // Arrays of primitives
    private static boolean[] booleanArray1;
    private static byte[] byteArray1;
    private static char[] charArray1;
    private static short[] shortArray1;
    private static int[] intArray1;
    private static long[] longArray1;
    private static float[] floatArray1;
    private static double[] doubleArray1;

    // Two-dimensional arrays of primitives
    private static boolean[][] boolean2dArray1;
    private static byte[][] byte2dArray1;
    private static char[][] char2dArray1;
    private static short[][] short2dArray1;
    private static int[][] int2dArray1;
    private static long[][] long2dArray1;
    private static float[][] float2dArray1;
    private static double[][] double2dArray1;

    // Three-dimensional arrays of primitives
    private static boolean[][][] boolean3dArray1;
    private static byte[][][] byte3dArray1;
    private static char[][][] char3dArray1;
    private static short[][][] short3dArray1;
    private static int[][][] int3dArray1;
    private static long[][][] long3dArray1;
    private static float[][][] float3dArray1;
    private static double[][][] double3dArray1;

    //
    // Constructors
    //
    public JavaPrimitives(int seed, int size) {
        random = new Random(seed);                  // Repeatable
        maxArraySize = size;
        initArrays();
    }
    private JavaPrimitives() {
        random = new Random();                      // Not as repeatable since timestamp used as seed
        maxArraySize = 25;
        initArrays();
    }

    //
    // marshal()
    //
    public void marshal() throws Exception {

        //
        // Get access to the MarshalEJB
        //
        Context ctx = new InitialContext();
        Object o = ctx.lookup("java:comp/env/MarshalEJB");
        MarshalEJBHome marshalEJBHome = (MarshalEJBHome) PortableRemoteObject.narrow(o, MarshalEJBHome.class);
        MarshalEJBRemote marshalEJBRemote = marshalEJBHome.create();

        //
        // boolean primitive
        //
        System.out.println();
        boolean boolean1 = random.nextBoolean();
        boolean boolean2 = random.nextBoolean();
        try {
            boolean2 = marshalEJBRemote.marshalBoolean(boolean1);
            System.out.println("boolean passed to EJB: " + boolean1);
            if (boolean1!=boolean2) {
                throw new Exception("boolean test failure: " + boolean1);
            }
        }
        catch (RemoteException re) {
            throw new Exception("boolean test RemoteException: " + boolean1 + " " + re);
        }

        //
        // boolean array
        //
        try {
            boolean[] booleanArray2 = marshalEJBRemote.marshalBoolean(booleanArray1);
            System.out.println("boolean array size passed to EJB: " + booleanArray1.length);
            if (!Arrays.equals(booleanArray1,booleanArray2)) {
                throw new Exception("boolean array test failure: " + Arrays.toString(booleanArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("boolean array test RemoteException: " + Arrays.toString(booleanArray1) + " " + re);
        }

        //
        // boolean two-dimensional array
        //
        try {
            boolean[][] boolean2dArray2 = marshalEJBRemote.marshalBoolean(boolean2dArray1);
            System.out.println("boolean two-dimensional array size passed to EJB: " + boolean2dArray1.length);
            if (!Arrays.deepEquals(boolean2dArray1,boolean2dArray2)) {
                throw new Exception("boolean two-dimensional array test failure: " + Arrays.deepToString(boolean2dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("boolean two-dimensional array test RemoteException: " + Arrays.deepToString(boolean2dArray1) + " " + re);
        }

        //
        // boolean three-dimensional array
        //
        try {
            boolean[][][] boolean3dArray2 = marshalEJBRemote.marshalBoolean(boolean3dArray1);
            System.out.println("boolean three-dimensional array size passed to EJB: " + boolean3dArray1.length);
            if (!Arrays.deepEquals(boolean3dArray1,boolean3dArray2)) {
                throw new Exception("boolean three-dimensional array test failure: " + Arrays.deepToString(boolean3dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("boolean three-dimensional array test RemoteException: " + Arrays.deepToString(boolean3dArray1) + " " + re);
        }

        //
        // byte primitive
        //
        System.out.println();
        byte byte1 = (byte) random.nextInt();
        byte byte2 = (byte) random.nextInt();
        try {
            byte2 = marshalEJBRemote.marshalByte(byte1);
            System.out.println("byte passed to EJB: " + byte1);
            if (byte1!=byte2) {
                throw new Exception("byte test failure: " + byte1);
            }
        }
        catch (RemoteException re) {
            throw new Exception("byte test RemoteException: " + byte1 + " " + re);
        }

        //
        // byte array
        //
        try {
            byte[] byteArray2 = marshalEJBRemote.marshalByte(byteArray1);
            System.out.println("byte array size passed to EJB: " + byteArray1.length);
            if (!Arrays.equals(byteArray1,byteArray2)) {
                throw new Exception("byte array test failure: " + Arrays.toString(byteArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("byte array test RemoteException: " + Arrays.toString(byteArray1) + " " + re);
        }

        //
        // byte two-dimensional array
        //
        try {
            byte[][] byte2dArray2 = marshalEJBRemote.marshalByte(byte2dArray1);
            System.out.println("byte two-dimensional array size passed to EJB: " + byte2dArray1.length);
            if (!Arrays.deepEquals(byte2dArray1,byte2dArray2)) {
                throw new Exception("byte two-dimensional array test failure: " + Arrays.deepToString(byte2dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("byte two-dimensional array test RemoteException: " + Arrays.deepToString(byte2dArray1) + " " + re);
        }

        //
        // byte three-dimensional array
        //
        try {
            byte[][][] byte3dArray2 = marshalEJBRemote.marshalByte(byte3dArray1);
            System.out.println("byte three-dimensional array size passed to EJB: " + byte3dArray1.length);
            if (!Arrays.deepEquals(byte3dArray1,byte3dArray2)) {
                throw new Exception("byte three-dimensional array test failure: " + Arrays.deepToString(byte3dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("byte three-dimensional array test RemoteException: " + Arrays.deepToString(byte3dArray1) + " " + re);
        }

        //
        // char primitive
        //
        System.out.println();
        char char1 = (char) random.nextInt(MAX_CHAR_VALUE);
        char char2 = (char) random.nextInt(MAX_CHAR_VALUE);
        try {
            char2 = marshalEJBRemote.marshalChar(char1);
            System.out.println("char passed to EJB: " + char1 + " (" + (int)char1 + ")" );
            if (char1!=char2) {
                throw new Exception("char test failure: " + char1 + " (" + (int)char1 + ")");
            }
        }
        catch (RemoteException re) {
            throw new Exception("char test RemoteException: " + char1 + " " + " (" + (int)char2 + ")" + re);
        }

        //
        // char array
        //
        try {
            char[] charArray2 = marshalEJBRemote.marshalChar(charArray1);
            System.out.println("char array size passed to EJB: " + charArray1.length);
            if (!Arrays.equals(charArray1,charArray2)) {
                throw new Exception("char array test failure");
            }
        }
        catch (RemoteException re) {
            throw new Exception("char array test RemoteException: " + re);
        }

        //
        // char two-dimensional array
        //
        try {
            char[][] char2dArray2 = marshalEJBRemote.marshalChar(char2dArray1);
            System.out.println("char two-dimensional array size passed to EJB: " + char2dArray1.length);
            if (!Arrays.deepEquals(char2dArray1,char2dArray2)) {
                throw new Exception("char two-dimensional array test failure: " + Arrays.deepToString(char2dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("char two-dimensional array test RemoteException: " + Arrays.deepToString(char2dArray1) + " " + re);
        }

        //
        // char three-dimensional array
        //
        try {
            char[][][] char3dArray2 = marshalEJBRemote.marshalChar(char3dArray1);
            System.out.println("char three-dimensional array size passed to EJB: " + char3dArray1.length);
            if (!Arrays.deepEquals(char3dArray1,char3dArray2)) {
                throw new Exception("char three-dimensional array test failure: " + Arrays.deepToString(char3dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("char three-dimensional array test RemoteException: " + Arrays.deepToString(char3dArray1) + " " + re);
        }

        //
        // short primitive
        //
        System.out.println();
        short short1 = (short) random.nextInt();
        short short2 = (short) random.nextInt();
        try {
            short2 = marshalEJBRemote.marshalShort(short1);
            System.out.println("short passed to EJB: " + short1);
            if (short1!=short2) {
                throw new Exception("short test failure: " + short1);
            }
        }
        catch (RemoteException re) {
            throw new Exception("short test RemoteException: " + short1 + " " + re);
        }

        //
        // short array
        //
        try {
            short[] shortArray2 = marshalEJBRemote.marshalShort(shortArray1);
            System.out.println("short array size passed to EJB: " + shortArray1.length);
            if (!Arrays.equals(shortArray1,shortArray2)) {
                throw new Exception("short array test failure: " + Arrays.toString(shortArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("short array test RemoteException: " + Arrays.toString(shortArray1) + " " + re);
        }

        //
        // short two-dimensional array
        //
        try {
            short[][] short2dArray2 = marshalEJBRemote.marshalShort(short2dArray1);
            System.out.println("short two-dimensional array size passed to EJB: " + short2dArray1.length);
            if (!Arrays.deepEquals(short2dArray1,short2dArray2)) {
                throw new Exception("short two-dimensional array test failure: " + Arrays.deepToString(short2dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("short two-dimensional array test RemoteException: " + Arrays.deepToString(short2dArray1) + " " + re);
        }

        //
        // short three-dimensional array
        //
        try {
            short[][][] short3dArray2 = marshalEJBRemote.marshalShort(short3dArray1);
            System.out.println("short three-dimensional array size passed to EJB: " + short3dArray1.length);
            if (!Arrays.deepEquals(short3dArray1,short3dArray2)) {
                throw new Exception("short three-dimensional array test failure: " + Arrays.deepToString(short3dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("short three-dimensional array test RemoteException: " + Arrays.deepToString(short3dArray1) + " " + re);
        }

        //
        // int primitive
        //
        System.out.println();
        int int1 = random.nextInt();
        int int2 = random.nextInt();
        try {
            int2 = marshalEJBRemote.marshalInt(int1);
            System.out.println("int passed to EJB: " + int1);
            if (int1!=int2) {
                throw new Exception("int test failure: " + int1);
            }
        }
        catch (RemoteException re) {
            throw new Exception("int test RemoteException: " + int1 + " " + re);
        }

        //
        // int array
        //
        try {
            int[] intArray2 = marshalEJBRemote.marshalInt(intArray1);
            System.out.println("int array size passed to EJB: " + intArray1.length);
            if (!Arrays.equals(intArray1,intArray2)) {
                throw new Exception("int array test failure: " + Arrays.toString(intArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("int array test RemoteException: " + Arrays.toString(intArray1) + " " + re);
        }

        //
        // int two-dimensional array
        //
        try {
            int[][] int2dArray2 = marshalEJBRemote.marshalInt(int2dArray1);
            System.out.println("int two-dimensional array size passed to EJB: " + int2dArray1.length);
            if (!Arrays.deepEquals(int2dArray1,int2dArray2)) {
                throw new Exception("int two-dimensional array test failure: " + Arrays.deepToString(int2dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("int two-dimensional array test RemoteException: " + Arrays.deepToString(int2dArray1) + " " + re);
        }

        //
        // int three-dimensional array
        //
        try {
            int[][][] int3dArray2 = marshalEJBRemote.marshalInt(int3dArray1);
            System.out.println("int three-dimensional array size passed to EJB: " + int3dArray1.length);
            if (!Arrays.deepEquals(int3dArray1,int3dArray2)) {
                throw new Exception("int three-dimensional array test failure: " + Arrays.deepToString(int3dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("int three-dimensional array test RemoteException: " + Arrays.deepToString(int3dArray1) + " " + re);
        }

        //
        // long primitive
        //
        System.out.println();
        long long1 = random.nextLong();
        long long2 = random.nextLong();
        try {
            long2 = marshalEJBRemote.marshalLong(long1);
            System.out.println("long passed to EJB: " + long1);
            if (long1!=long2) {
                throw new Exception("long test failure: " + long1);
            }
        }
        catch (RemoteException re) {
            throw new Exception("long test RemoteException: " + long1 + " " + re);
        }

        //
        // long array
        //
        try {
            long[] longArray2 = marshalEJBRemote.marshalLong(longArray1);
            System.out.println("long array size passed to EJB: " + longArray1.length);
            if (!Arrays.equals(longArray1,longArray2)) {
                throw new Exception("long array test failure: " + Arrays.toString(longArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("long array test RemoteException: " + Arrays.toString(longArray1) + " " + re);
        }

        //
        // long two-dimensional array
        //
        try {
            long[][] long2dArray2 = marshalEJBRemote.marshalLong(long2dArray1);
            System.out.println("long two-dimensional array size passed to EJB: " + long2dArray1.length);
            if (!Arrays.deepEquals(long2dArray1,long2dArray2)) {
                throw new Exception("long two-dimensional array test failure: " + Arrays.deepToString(long2dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("long two-dimensional array test RemoteException: " + Arrays.deepToString(long2dArray1) + " " + re);
        }

        //
        // long three-dimensional array
        //
        try {
            long[][][] long3dArray2 = marshalEJBRemote.marshalLong(long3dArray1);
            System.out.println("long three-dimensional array size passed to EJB: " + long3dArray1.length);
            if (!Arrays.deepEquals(long3dArray1,long3dArray2)) {
                throw new Exception("long three-dimensional array test failure: " + Arrays.deepToString(long3dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("long three-dimensional array test RemoteException: " + Arrays.deepToString(long3dArray1) + " " + re);
        }

        //
        // float primitive
        //
        System.out.println();
        float float1 = random.nextFloat();
        float float2 = random.nextFloat();
        try {
            float2 = marshalEJBRemote.marshalFloat(float1);
            System.out.println("float passed to EJB: " + float1);
            if (float1!=float2) {
                throw new Exception("float test failure: " + float1);
            }
        }
        catch (RemoteException re) {
            throw new Exception("float test RemoteException: " + float1 + " " + re);
        }

        //
        // float array
        //
        try {
            float[] floatArray2 = marshalEJBRemote.marshalFloat(floatArray1);
            System.out.println("float array size passed to EJB: " + floatArray1.length);
            if (!Arrays.equals(floatArray1,floatArray2)) {
                throw new Exception("float array test failure: " + Arrays.toString(floatArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("float array test RemoteException: " + Arrays.toString(floatArray1) + " " + re);
        }

        //
        // float two-dimensional array
        //
        try {
            float[][] float2dArray2 = marshalEJBRemote.marshalFloat(float2dArray1);
            System.out.println("float two-dimensional array size passed to EJB: " + float2dArray1.length);
            if (!Arrays.deepEquals(float2dArray1,float2dArray2)) {
                throw new Exception("float two-dimensional array test failure: " + Arrays.deepToString(float2dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("float two-dimensional array test RemoteException: " + Arrays.deepToString(float2dArray1));
        }

        //
        // float three-dimensional array
        //
        try {
            float[][][] float3dArray2 = marshalEJBRemote.marshalFloat(float3dArray1);
            System.out.println("float three-dimensional array size passed to EJB: " + float3dArray1.length);
            if (!Arrays.deepEquals(float3dArray1,float3dArray2)) {
                throw new Exception("float three-dimensional array test failure: " + Arrays.deepToString(float3dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("float three-dimensional array test RemoteException: " + Arrays.deepToString(float3dArray1) + " " + re);
        }

        //
        // double primitive
        //
        System.out.println();
        double double1 = random.nextDouble();
        double double2 = random.nextDouble();
        try {
            double2 = marshalEJBRemote.marshalDouble(double1);
            System.out.println("double passed to EJB: " + double1);
            if (double1!=double2) {
                throw new Exception("double test failure: " + double1);
            }
        }
        catch (RemoteException re) {
            throw new Exception("double test RemoteException: " + double1 + " " + re);
        }

        //
        // double array
        //
        try {
            double[] doubleArray2 = marshalEJBRemote.marshalDouble(doubleArray1);
            System.out.println("double array size passed to EJB: " + doubleArray1.length);
            if (!Arrays.equals(doubleArray1,doubleArray2)) {
                throw new Exception("double array test failure: " + Arrays.toString(doubleArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("double array test RemoteException: " + Arrays.toString(doubleArray1) + " " + re);
        }

        //
        // double two-dimensional array
        //
        try {
            double[][] double2dArray2 = marshalEJBRemote.marshalDouble(double2dArray1);
            System.out.println("double two-dimensional array size passed to EJB: " + double2dArray1.length);
            if (!Arrays.deepEquals(double2dArray1,double2dArray2)) {
                throw new Exception("double two-dimensional array test failure: " + Arrays.deepToString(double2dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("double two-dimensional array test RemoteException: " + Arrays.deepToString(double2dArray1) + " " + re);
        }

        //
        // double three-dimensional array
        //
        try {
            double[][][] double3dArray2 = marshalEJBRemote.marshalDouble(double3dArray1);
            System.out.println("double three-dimensional array size passed to EJB: " + double3dArray1.length);
            if (!Arrays.deepEquals(double3dArray1,double3dArray2)) {
                throw new Exception("double three-dimensional array test failure: " + Arrays.deepToString(double3dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("double three-dimensional array test RemoteException: " + Arrays.deepToString(double3dArray1) + " " + re);
        }
    }


    //
    // initArrays()
    //
    private void initArrays() {

        // boolean array
        booleanArray1 = new boolean[random.nextInt(maxArraySize)];
        for (int ii=0; ii<booleanArray1.length; ii++) {
            booleanArray1[ii] = random.nextBoolean();
        }
        // boolean two-dimensional array
        boolean2dArray1 = new boolean[random.nextInt(maxArraySize)][];
        for (int ii=0; ii<boolean2dArray1.length; ii++) {
            boolean2dArray1[ii] = new boolean[random.nextInt(maxArraySize)];
            for (int jj=0; jj<boolean2dArray1[ii].length; jj++) {
                boolean2dArray1[ii][jj] = random.nextBoolean();
            }
        }
        // boolean three-dimensional array
        boolean3dArray1 = new boolean[random.nextInt(maxArraySize)][][];
        for (int ii=0; ii<boolean3dArray1.length; ii++) {
            boolean3dArray1[ii] = new boolean[random.nextInt(maxArraySize)][];
            for (int jj=0; jj<boolean3dArray1[ii].length; jj++) {
                boolean3dArray1[ii][jj] = new boolean[random.nextInt(maxArraySize)];
                for (int kk=0; kk<boolean3dArray1[ii][jj].length; kk++) {
                    boolean3dArray1[ii][jj][kk] = random.nextBoolean();
                }
            }
        }

        // byte array
        byteArray1 = new byte[random.nextInt(maxArraySize)];
        random.nextBytes(byteArray1);
        // byte two-dimensional array
        byte2dArray1 = new byte[random.nextInt(maxArraySize)][];
        for (int ii=0; ii<byte2dArray1.length; ii++) {
            byte2dArray1[ii] = new byte[random.nextInt(maxArraySize)];
            random.nextBytes(byte2dArray1[ii]);
        }
        // byte three-dimensional array
        byte3dArray1 = new byte[random.nextInt(maxArraySize)][][];
        for (int ii=0; ii<byte3dArray1.length; ii++) {
            byte3dArray1[ii] = new byte[random.nextInt(maxArraySize)][];
            for (int jj=0; jj<byte3dArray1[ii].length; jj++) {
                byte3dArray1[ii][jj] = new byte[random.nextInt(maxArraySize)];
                random.nextBytes(byte3dArray1[ii][jj]);
            }
        }

        // char array
        charArray1 = new char[random.nextInt(maxArraySize)];
        for (int ii=0; ii<charArray1.length; ii++) {
            charArray1[ii] = (char) random.nextInt(MAX_CHAR_VALUE);
        }
        // char two-dimensional array
        char2dArray1 = new char[random.nextInt(maxArraySize)][];
        for (int ii=0; ii<char2dArray1.length; ii++) {
            char2dArray1[ii] = new char[random.nextInt(maxArraySize)];
            for (int jj=0; jj<char2dArray1[ii].length; jj++) {
                char2dArray1[ii][jj] = (char) random.nextInt(MAX_CHAR_VALUE);
            }
        }
        // char three-dimensional array
        char3dArray1 = new char[random.nextInt(maxArraySize)][][];
        for (int ii=0; ii<char3dArray1.length; ii++) {
            char3dArray1[ii] = new char[random.nextInt(maxArraySize)][];
            for (int jj=0; jj<char3dArray1[ii].length; jj++) {
                char3dArray1[ii][jj] = new char[random.nextInt(maxArraySize)];
                for (int kk=0; kk<char3dArray1[ii][jj].length; kk++) {
                    char3dArray1[ii][jj][kk] = (char) random.nextInt(MAX_CHAR_VALUE);
                }
            }
        }

        // short array
        shortArray1 = new short[random.nextInt(maxArraySize)];
        for (int ii=0; ii<shortArray1.length; ii++) {
            shortArray1[ii] = (short) random.nextInt();
        }
        // short two-dimensional array
        short2dArray1 = new short[random.nextInt(maxArraySize)][];
        for (int ii=0; ii<short2dArray1.length; ii++) {
            short2dArray1[ii] = new short[random.nextInt(maxArraySize)];
            for (int jj=0; jj<short2dArray1[ii].length; jj++) {
                short2dArray1[ii][jj] = (short) random.nextInt();
            }
        }
        // short three-dimensional array
        short3dArray1 = new short[random.nextInt(maxArraySize)][][];
        for (int ii=0; ii<short3dArray1.length; ii++) {
            short3dArray1[ii] = new short[random.nextInt(maxArraySize)][];
            for (int jj=0; jj<short3dArray1[ii].length; jj++) {
                short3dArray1[ii][jj] = new short[random.nextInt(maxArraySize)];
                for (int kk=0; kk<short3dArray1[ii][jj].length; kk++) {
                    short3dArray1[ii][jj][kk] = (short) random.nextInt();
                }
            }
        }

        // int array
        intArray1 = new int[random.nextInt(maxArraySize)];
        for (int ii=0; ii<intArray1.length; ii++) {
            intArray1[ii] = random.nextInt();
        }
        // int two-dimensional array
        int2dArray1 = new int[random.nextInt(maxArraySize)][];
        for (int ii=0; ii<int2dArray1.length; ii++) {
            int2dArray1[ii] = new int[random.nextInt(maxArraySize)];
            for (int jj=0; jj<int2dArray1[ii].length; jj++) {
                int2dArray1[ii][jj] = random.nextInt();
            }
        }
        // int three-dimensional array
        int3dArray1 = new int[random.nextInt(maxArraySize)][][];
        for (int ii=0; ii<int3dArray1.length; ii++) {
            int3dArray1[ii] = new int[random.nextInt(maxArraySize)][];
            for (int jj=0; jj<int3dArray1[ii].length; jj++) {
                int3dArray1[ii][jj] = new int[random.nextInt(maxArraySize)];
                for (int kk=0; kk<int3dArray1[ii][jj].length; kk++) {
                    int3dArray1[ii][jj][kk] = random.nextInt();
                }
            }
        }

        // long array
        longArray1 = new long[random.nextInt(maxArraySize)];
        for (int ii=0; ii<longArray1.length; ii++) {
            longArray1[ii] = random.nextLong();
        }
        // long two-dimensional array
        long2dArray1 = new long[random.nextInt(maxArraySize)][];
        for (int ii=0; ii<long2dArray1.length; ii++) {
            long2dArray1[ii] = new long[random.nextInt(maxArraySize)];
            for (int jj=0; jj<long2dArray1[ii].length; jj++) {
                long2dArray1[ii][jj] = random.nextLong();
            }
        }
        // long three-dimensional array
        long3dArray1 = new long[random.nextInt(maxArraySize)][][];
        for (int ii=0; ii<long3dArray1.length; ii++) {
            long3dArray1[ii] = new long[random.nextInt(maxArraySize)][];
            for (int jj=0; jj<long3dArray1[ii].length; jj++) {
                long3dArray1[ii][jj] = new long[random.nextInt(maxArraySize)];
                for (int kk=0; kk<long3dArray1[ii][jj].length; kk++) {
                    long3dArray1[ii][jj][kk] = random.nextLong();
                }
            }
        }

        // float array
        floatArray1 = new float[random.nextInt(maxArraySize)];
        for (int ii=0; ii<floatArray1.length; ii++) {
            floatArray1[ii] = random.nextFloat();
        }
        // float two-dimensional array
        float2dArray1 = new float[random.nextInt(maxArraySize)][];
        for (int ii=0; ii<float2dArray1.length; ii++) {
            float2dArray1[ii] = new float[random.nextInt(maxArraySize)];
            for (int jj=0; jj<float2dArray1[ii].length; jj++) {
                float2dArray1[ii][jj] = random.nextFloat();
            }
        }
        // float three-dimensional array
        float3dArray1 = new float[random.nextInt(maxArraySize)][][];
        for (int ii=0; ii<float3dArray1.length; ii++) {
            float3dArray1[ii] = new float[random.nextInt(maxArraySize)][];
            for (int jj=0; jj<float3dArray1[ii].length; jj++) {
                float3dArray1[ii][jj] = new float[random.nextInt(maxArraySize)];
                for (int kk=0; kk<float3dArray1[ii][jj].length; kk++) {
                    float3dArray1[ii][jj][kk] = random.nextFloat();
                }
            }
        }

        // double array
        doubleArray1 = new double[random.nextInt(maxArraySize)];
        for (int ii=0; ii<doubleArray1.length; ii++) {
            doubleArray1[ii] = random.nextDouble();
        }
        // double two-dimensional array
        double2dArray1 = new double[random.nextInt(maxArraySize)][];
        for (int ii=0; ii<double2dArray1.length; ii++) {
            double2dArray1[ii] = new double[random.nextInt(maxArraySize)];
            for (int jj=0; jj<double2dArray1[ii].length; jj++) {
                double2dArray1[ii][jj] = random.nextDouble();
            }
        }
        // double three-dimensional array
        double3dArray1 = new double[random.nextInt(maxArraySize)][][];
        for (int ii=0; ii<double3dArray1.length; ii++) {
            double3dArray1[ii] = new double[random.nextInt(maxArraySize)][];
            for (int jj=0; jj<double3dArray1[ii].length; jj++) {
                double3dArray1[ii][jj] = new double[random.nextInt(maxArraySize)];
                for (int kk=0; kk<double3dArray1[ii][jj].length; kk++) {
                    double3dArray1[ii][jj][kk] = random.nextDouble();
                }
            }
        }
    }
}
