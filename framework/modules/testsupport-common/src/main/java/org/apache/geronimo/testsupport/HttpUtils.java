/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.testsupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @version $Rev$ $Date$
 */
public class HttpUtils {
    
    public static String doGET(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = doGET(conn);            
            if (conn.getResponseCode() != 200) {
                throw new IOException(reply);
            }            
            return reply;            
        } finally {
            conn.disconnect();
        }
    }
    
    public static String doGET(HttpURLConnection conn) throws IOException {
        conn.setRequestMethod("GET");
        return call(conn, null);        
    }
            
    public static String doPOST(URL url, InputStream input) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = doPOST(conn, input);            
            if (conn.getResponseCode() != 200) {
                throw new IOException(reply);
            }            
            return reply;            
        } finally {
            conn.disconnect();
        }
    }
    
    public static String doPOST(HttpURLConnection conn, InputStream input) throws IOException {
        conn.setRequestMethod("POST");
        return call(conn, input);        
    }
    
    public static String call(HttpURLConnection conn, InputStream input) throws IOException {        
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        conn.setUseCaches(false);
        
        if (input != null) {
            conn.setDoOutput(true);
            
            OutputStream out = conn.getOutputStream();

            byte[] data = new byte[1024];
            int read = 0;
            while ((read = input.read(data, 0, data.length)) != -1) {
                out.write(data, 0, read);
            }

            input.close();

            out.flush();
            out.close();
        } else {
            conn.connect();
        }

        InputStream is = null;
        
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            is = conn.getErrorStream();
        }
        
        StringBuilder buf = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            buf.append(inputLine);
        }
        in.close();
        
        return buf.toString();
    }
    
    /*
    private String getAuthorizationValue(String user, String password) {
        String userPassword = user + ":" + password;
        byte[] encodedUserPassword = Base64.encode(userPassword.getBytes());
        String encodedUserPasswordStr = new String(encodedUserPassword, 0, encodedUserPassword.length);
        return "Basic " + encodedUserPasswordStr;
    }
    */
    
}
