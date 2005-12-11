/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi;

import org.apache.geronimo.interop.util.StringUtil;


public class RmiTrace {
    public static final boolean ENABLED = true;
    public static final boolean CONNECT = true;

    public RmiTrace() {
    }

    public static void receive(String host, byte[] data) {
        dump(formatReceiveHeader(host), data);
    }

    public static void send(String host, byte[] data) {
        dump(formatSendHeader(host), data);
    }

    public static void dump(String header, byte[] data) {
        traceRmiHeader(header);
        StringBuffer dataBuffer = new StringBuffer(44);
        StringBuffer textBuffer = new StringBuffer(20);
        for (int i = 0; i < data.length; i++) {
            int d = (data[i] + 0x100) & 0xff;
            String h = StringUtil.padLeft(Integer.toHexString(d).toUpperCase(), '0', 2);
            dataBuffer.append(h);
            if (i % 4 == 3 && i % 20 != 19) {
                dataBuffer.append(' ');
            }
            char c = (char) d;
            if (c < 32 || c > 127) {
                c = '.';
            }
            textBuffer.append(c);
            if (i % 20 == 19) {
                traceRmi(StringUtil.padRight(dataBuffer.toString(), ' ', 44), textBuffer.toString());
                dataBuffer.setLength(0);
                textBuffer.setLength(0);
            }
        }
        if (dataBuffer.length() != 0) {
            traceRmi(StringUtil.padRight(dataBuffer.toString(), ' ', 44), textBuffer.toString());
        }
    }

    // format methods

    protected static String formatReceiveHeader(String host) {
        String msg;
        msg = "RmiTrace.formatReceiveHeader(): host: " + host;
        return msg;
    }

    protected static String formatSendHeader(String host) {
        String msg;
        msg = "RmiTrace.formatSendHeader(): host: " + host;
        return msg;
    }


    // log methods

    public static void traceConnect(String endpoint) {
        System.out.println("RmiTrace.traceConnect(): endpoint: " + endpoint);
    }

    public static void traceDisconnect(String endpoint) {
        System.out.println("RmiTrace.traceDisconnect(): endpoint: " + endpoint);
    }

    protected static void traceRmiHeader(String header) {
        System.out.println("RmiTrace.traceRmiHeader(): header: " + header);
    }

    protected static void traceRmi(String data, String text) {
        System.out.println("RmiTrace.traceRmi(): data: " + data + ", text: " + text);
    }

}
