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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

public class JavaStrings {

    private static int maxArraySize;
    private static Random random;                           // Pseudo-random number generator
    private static final int MAX_STRING_SIZE=10000;

    private static final int[] charTable = new int[224];    // Printable characters only (32-256)

    // Arrays of Strings
    private static String[] stringArray1;
    private static ArrayList<String> stringArrayList1;

    // Two-dimensional arrays of Strings
    private static String[][] string2dArray1;
    private static List<ArrayList<String>> string2dArrayList1;

    // Three-dimensional arrays of Strings
    private static String[][][] string3dArray1;
    private static Collection<List<ArrayList<String>>> string3dArrayList1;

    //
    // Constructors
    //
    public JavaStrings(int seed, int size) {
        random = new Random(seed);                  // Repeatable
        maxArraySize = size;
        initArrays();
    }
    private JavaStrings() {
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
        // String
        //
        System.out.println();
        String string1 = randomString();
        String string2 = "";
        try {
            string2 = marshalEJBRemote.marshalString(string1);
            System.out.println("String passed to EJB: " + string1);
            if (!string1.equals(string2)) {
                throw new Exception("String test failure: " + string1);
            }
        }
        catch (RemoteException re) {
            throw new Exception("String test RemoteException: " + string1 + " " + re);
        }

        //
        // String array
        //
        try {
            String[] stringArray2 = marshalEJBRemote.marshalString(stringArray1);
            System.out.println("String array size passed to EJB: " + stringArray1.length);
            if (!Arrays.equals(stringArray1,stringArray2)) {
                throw new Exception("String array test failure: " + Arrays.toString(stringArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("String array test RemoteException: " + Arrays.toString(stringArray1) + " " + re);
        }

        //
        // String array list
        //
        try {
            ArrayList<String> stringArrayList2 = marshalEJBRemote.marshalString(stringArrayList1);
            System.out.println("String array list size passed to EJB: " + stringArrayList1.size());
            if (!stringArrayList1.equals(stringArrayList2)) {
                throw new Exception("String array list test failure: " + Arrays.toString(stringArrayList1.toArray()));
            }
        }
        catch (RemoteException re) {
            throw new Exception("String array list test RemoteException: " + Arrays.toString(stringArrayList1.toArray()) + " " + re);
        }

        //
        // String two-dimensional array
        //
        try {
            String[][] string2dArray2 = marshalEJBRemote.marshalString(string2dArray1);
            System.out.println("String two-dimensional array size passed to EJB: " + string2dArray1.length);
            if (!Arrays.deepEquals(string2dArray1,string2dArray2)) {
                throw new Exception("String two-dimensional array test failure: " + Arrays.deepToString(string2dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("String two-dimensional array test RemoteException: " + Arrays.deepToString(string2dArray1) + " " + re);
        }

        //
        // String two-dimensional array list
        //
        try {
            List<ArrayList<String>> string2dArrayList2 = marshalEJBRemote.marshalString(string2dArrayList1);
            System.out.println("String two-dimensional array list size passed to EJB: " + string2dArrayList1.size());
            if (!string2dArrayList1.equals(string2dArrayList2)) {
                throw new Exception("String two-dimensional array list failure: " + Arrays.toString(string2dArrayList1.toArray()));
            }
        }
        catch (RemoteException re) {
            throw new Exception("String two-dimensional array list test RemoteException: " + Arrays.toString(string2dArrayList1.toArray()) + " " + re);
        }

        //
        // String three-dimensional array
        //
        try {
            String[][][] string3dArray2 = marshalEJBRemote.marshalString(string3dArray1);
            System.out.println("String three-dimensional array size passed to EJB: " + string3dArray1.length);
            if (!Arrays.deepEquals(string3dArray1,string3dArray2)) {
                throw new Exception("String three-dimensional array test failure: " + Arrays.deepToString(string3dArray1));
            }
        }
        catch (RemoteException re) {
            throw new Exception("String three-dimensional array test RemoteException: " + Arrays.deepToString(string3dArray1) + " " + re);
        }

        //
        // String three-dimensional array list
        //
        try {
            Collection<List<ArrayList<String>>> string3dArrayList2 = marshalEJBRemote.marshalString(string3dArrayList1);
            System.out.println("String three-dimensional array list size passed to EJB: " + string3dArrayList1.size());
            if (!string3dArrayList1.equals(string3dArrayList2)) {
                throw new Exception("String three-dimensional array list failure: " + Arrays.toString(string3dArrayList1.toArray()));
            }
        }
        catch (RemoteException re) {
            throw new Exception("String three-dimensional array list test RemoteException: " + Arrays.toString(string3dArrayList1.toArray()) + " " + re);
        }

    }


    //
    // randomString()
    //
    private String randomString() {
        String randomString = "";
        for (int ii=0; ii<random.nextInt(MAX_STRING_SIZE); ii++ ) {
            randomString = randomString + (char)charTable[random.nextInt(charTable.length)];
        }
        return randomString;
    }


    //
    // initArrays()
    //
    private void initArrays() {

        // charTable array
        for (int ii=0; ii<charTable.length; ii++) {
            charTable[ii] = ii+32;
        }
        // String array
        stringArray1 = new String[random.nextInt(maxArraySize)];
        for (int ii=0; ii<stringArray1.length; ii++) {
            stringArray1[ii] = randomString();
        }
        // String array list
        stringArrayList1 = new ArrayList<String>();
        int arraySize = random.nextInt(maxArraySize);
        for (int ii=0; ii<arraySize; ii++) {
            stringArrayList1.add(randomString());
        }
        // String two-dimensional array
        string2dArray1 = new String[random.nextInt(maxArraySize)][];
        for (int ii=0; ii<string2dArray1.length; ii++) {
            string2dArray1[ii] = new String[random.nextInt(maxArraySize)];
            for (int jj=0; jj<string2dArray1[ii].length; jj++) {
                string2dArray1[ii][jj] = randomString();
            }
        }
        // String two-dimensional array list
        string2dArrayList1 = new ArrayList();
        int arraySize1 = random.nextInt(maxArraySize);
        int arraySize2 = random.nextInt(maxArraySize);
        for (int ii=0; ii<arraySize1; ii++) {
            ArrayList<String> stringArrayList = new ArrayList<String>();
            for (int jj=0; jj<arraySize2; jj++) {
                stringArrayList.add(randomString());
            }
            string2dArrayList1.add(stringArrayList);
        }
        // String three-dimensional array
        string3dArray1 = new String[random.nextInt(maxArraySize)][][];
        for (int ii=0; ii<string3dArray1.length; ii++) {
            string3dArray1[ii] = new String[random.nextInt(maxArraySize)][];
            for (int jj=0; jj<string3dArray1[ii].length; jj++) {
                string3dArray1[ii][jj] = new String[random.nextInt(maxArraySize)];
                for (int kk=0; kk<string3dArray1[ii][jj].length; kk++) {
                    string3dArray1[ii][jj][kk] = randomString();
                }
            }
        }
        // String three-dimensional array list
        string3dArrayList1 = new ArrayList();
        arraySize1 = random.nextInt(maxArraySize);
        arraySize2 = random.nextInt(maxArraySize);
        int arraySize3 = random.nextInt(maxArraySize);
        for (int ii=0; ii<arraySize1; ii++) {
            List<ArrayList<String>> stringArrayList1= new ArrayList();
            for (int jj=0; jj<arraySize2; jj++) {
                ArrayList<String> stringArrayList2 = new ArrayList<String>();
                for (int kk=0; kk<arraySize3; kk++) {
                    stringArrayList2.add(randomString());
                }
                stringArrayList1.add(stringArrayList2);
            }
            string3dArrayList1.add(stringArrayList1);
        }
    }
}
