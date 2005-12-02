/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.corba.codeset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.corba.io.GIOPVersion;
import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.io.OutputStreamBase;


public class DefaultCharConverter
    implements CharConverter
{
	static final Log log = LogFactory.getLog(DefaultCharConverter.class);
	
	
	public boolean equals(Object other) {
		if (other instanceof DefaultCharConverter) {
			return ((DefaultCharConverter)other).giop_minor == giop_minor;
		}
		
		return false;
	}
	
	public int hashCode() {
		return giop_minor;
	}
	
    int giop_minor;
    DefaultCharConverter (int major, int minor)
    {
        if (major != 1) 
            throw new org.omg.CORBA.NO_IMPLEMENT ("GIOP "+major+"."+minor);

        giop_minor = minor;
    }

    public void write_char (OutputStreamBase out, char c)
    {
        out.write_octet ((byte) c);
    }

    public void write_string (OutputStreamBase out, String value)
    {
        final char[] arr = value.toCharArray();
        final int len = arr.length + 1;

        out.write_ulong(len);
        out.write_char_array (arr, 0, arr.length);

        out.write_char ((char)0);
    }


    public char read_char (InputStreamBase in)
    {
        char value = (char)in.read_octet();
        if (value == (char) 0)
            throw new org.omg.CORBA.MARSHAL ("null character in string");
        return value;
    }

    public String read_string (InputStreamBase in, int length)
    {
        int before = in.__stream_position()-4;
        if (length == 0)
            throw new org.omg.CORBA.MARSHAL ("zero-length string data");

        if (length == -1) {
            throw new org.omg.CORBA.MARSHAL ("negative string length");
        }

        char[] data = new char[length-1];
        in.read_char_array (data, 0, data.length);

        if (in.read_octet() != 0)
            throw new org.omg.CORBA.MARSHAL ("missing null-terminator");

        String value = new String (data, 0, data.length);

        if (log.isDebugEnabled ()) {
            log.debug ("read_string @ "+before+"-"+in.__stream_position()
                       +" "+(in.__stream_position()-before)+"bytes"
                       +" value="+value);
        }

        return value;
    }

	public static CharConverter getInstance(GIOPVersion version) {
		return new DefaultCharConverter(version.major, version.minor);
	}



}
