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
package org.apache.geronimo.interop.rmi.iiop;

public abstract class Protocol
{
    public static final int IIOP  = 1;
    public static final int IIOPS = 2;
    public static final int HTTP  = 3;
    public static final int HTTPS = 4;

    public static String getName(int protocol)
    {
        switch (protocol)
        {
            case IIOP: return "iiop";
            case IIOPS: return "iiop";
            case HTTP: return "http";
            case HTTPS: return "https";
            default: throw new IllegalArgumentException("protocol = " + protocol);
        }
    }

    public static String getScheme(int protocol)
    {
        switch (protocol)
        {
            case IIOP: return "iiop:";
            case IIOPS: return "iiop:";
            case HTTP: return "http:";
            case HTTPS: return "https:";
            default: throw new IllegalArgumentException("protocol = " + protocol);
        }
    }

    public static int getNumber(String protocol)
    {
        if (protocol.equals("iiop"))
        {
            return IIOP;
        }
        else if (protocol.equals("iiops"))
        {
            return IIOPS;
        }
        else if (protocol.equals("http"))
        {
            return HTTP;
        }
        else if (protocol.equals("https"))
        {
            return HTTPS;
        }
        else
        {
            throw new IllegalArgumentException("protocol = " + protocol);
        }
    }
}
