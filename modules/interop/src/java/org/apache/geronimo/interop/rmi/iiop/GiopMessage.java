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

import org.omg.GIOP.RequestHeader_1_2;
import org.omg.GIOP.LocateRequestHeader_1_2;
import org.omg.GIOP.ReplyHeader_1_2;

public class GiopMessage
{
    public int size;
    public int type;
    public int giopVersion;
    public RequestHeader_1_2 request;
    public LocateRequestHeader_1_2 locateRequest;
    public ReplyHeader_1_2 reply;
    public boolean httpTunneling;
    public int hiopVersion; //http tunneling version - 1 or 2

    public String toString()
    {
        StringBuffer sb = new StringBuffer("GiopMessage(");
        /* TODO
        if (header != null)
        {
            if (header.GIOP_version != null)
            {
                sb.append("version = ");
                sb.append(header.GIOP_version.major);
                sb.append('.');
                sb.append(header.GIOP_version.minor);
            }
            if ((header.flags & 1) != 0)
            {
                sb.append(", little endian");
            }
            else
            {
                sb.append(", big endian");
            }
            if ((header.flags & 2) != 0)
            {
                sb.append(", fragmented");
            }
            sb.append(", message type = ");
            switch (header.message_type)
            {
              case MsgType_1_1._Request:
                sb.append("Request");
                break;
              case MsgType_1_1._Reply:
                sb.append("Reply");
                break;
              default:
                sb.append(header.message_type);
            }
            sb.append(", message size = ");
            sb.append(header.message_size);
        }
        */
        sb.append(")");
        return sb.toString();
    }
}
