/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.crypto.asn1.x509;

/**
 * class for breaking up an X500 Name into it's component tokens, ala
 * java.util.StringTokenizer. We need this class as some of the
 * lightweight Java environment don't support classes like
 * StringTokenizer.
 */
public class X509NameTokenizer
{
    private String          value;
    private int             index;
    private char            seperator;
    private StringBuilder    buf = new StringBuilder();

    public X509NameTokenizer(
        String  oid)
    {
        this(oid, ',');
    }

    public X509NameTokenizer(
        String  oid,
        char    seperator)
    {
        this.value = oid;
        this.index = -1;
        this.seperator = seperator;
    }

    public boolean hasMoreTokens()
    {
        return (index != value.length());
    }

    public String nextToken()
    {
        if (index == value.length())
        {
            return null;
        }

        int     end = index + 1;
        boolean quoted = false;
        boolean escaped = false;

        buf.setLength(0);

        while (end != value.length())
        {
            char    c = value.charAt(end);

            if (c == '"')
            {
                if (!escaped)
                {
                    quoted = !quoted;
                }
                else
                {
                    buf.append(c);
                }
                escaped = false;
            }
            else
            {
                if (escaped || quoted)
                {
                    buf.append(c);
                    escaped = false;
                }
                else if (c == '\\')
                {
                    escaped = true;
                }
                else if (c == seperator)
                {
                    break;
                }
                else
                {
                    buf.append(c);
                }
            }
            end++;
        }

        index = end;
        return buf.toString().trim();
    }
}
