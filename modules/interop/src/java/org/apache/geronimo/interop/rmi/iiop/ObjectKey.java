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

import org.apache.geronimo.interop.security.*;
import org.apache.geronimo.interop.util.*;
import java.util.*;

public class ObjectKey
{
    public static final int TYPE_MANAGER = 'M';

    public static final int TYPE_SESSION = 'S';

    public int type;
    public String username = "";
    public String password = "";
    public String component = "";
    public String sessionID = "";

    public byte[] encode()
    {
        int un = username.length();
        int pn = password.length();
        int cn = component.length();
        int sn = sessionID.length();
        StringBuffer keyBuffer = new StringBuffer(12 + un + pn + cn + sn);
        keyBuffer.append("U=");
        keyBuffer.append(username);
        keyBuffer.append("\tP=");
        keyBuffer.append(password);
        keyBuffer.append("\tC=");
        keyBuffer.append(component);
        if (sn > 0)
        {
            keyBuffer.append("\tS=");
            keyBuffer.append(sessionID);
        }
        byte[] bytes = SecurityInfo.encode(keyBuffer.toString());
        bytes[0] = (byte)type;
        return bytes;
    }

    public void decode(byte[] bytes)
    {
        type = bytes.length == 0 ? 0 : bytes[0];
        String key = SecurityInfo.decode(bytes);
        List items = ListUtil.getListWithSeparator(key, "\t");
        for (Iterator i = items.iterator(); i.hasNext();)
        {
            String item = (String)i.next();
            if (item.startsWith("U="))
            {
                username = item.substring(2);
            }
            else if (item.startsWith("P="))
            {
                password = item.substring(2);
            }
            else if (item.startsWith("C="))
            {
                component = item.substring(2);
            }
            else if (item.startsWith("S="))
            {
                sessionID = item.substring(2);
            }
        }
    }

    public void checkPassword()
    {
        User.getInstance(username).login(password);
    }
}
