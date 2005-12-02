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

public class DefaultWCharConverter implements CharConverter {

	static final Log log = LogFactory.getLog(DefaultWCharConverter.class);
	
	private GIOPVersion version;

	DefaultWCharConverter(GIOPVersion version)
	{
		this.version = version;
	}
	
	
	public void write_char(OutputStreamBase out, char c) {
        if (version.minor == 2) {

            //
            // \ufffd15.3.1.6: ... For GIOP 1.2, a wchar is encoded as an
            // unsigned binary octet value, followed by the elements
            // of the octet sequence representing the encoded value of
            // the wchar.
            //
            out.write_octet ((byte) 2);
            int pos = out.__stream_position() -1;
            out.write_octet ((byte) (((int)c) >> 8));
            out.write_octet ((byte) (((int)c) & 0xff));

            if (log.isDebugEnabled ()) {
                log.debug ("write_wchar[1.2]@"+pos
                           +" ("+Integer.toHexString (0xffff & ((int)c))+")");
            }

        } else if (version.minor == 0 || version.minor == 1) {
            out.write_ushort ((short)c);
            int pos = out.__stream_position() -2;

            if (log.isDebugEnabled ()) {
                log.debug ("write_wchar[1.0]@"+pos
                           +" ("+Integer.toHexString (0xffff & ((int)c))+")");
            }
        } else {
            throw new org.omg.CORBA.MARSHAL ("GIOP 1."+version.minor);
        }
	}
	
    void write_wstring (OutputStreamBase out, String value)
    {
        if (version.minor == 0 || version.minor == 1)
            {
                final char[] arr = value.toCharArray();
                final int len = arr.length + 1;
                
                out.write_ulong (len);
                int start = out.__stream_position()-4;
                out.write_wchar_array (arr, 0, arr.length);
                out.write_wchar ((char)0);
                
                if (log.isDebugEnabled ()) {
                    log.debug ("write_wstring[1.0] @ "+start+"-"+out.__stream_position()
                               +" "+(out.__stream_position()-start)+"bytes"
                               +" value="+value);
                }
            }
        else if (version.minor == 2)
            {
                final int len = value.length ();
                
                out.write_ulong (len*2);
                int start = out.__stream_position()-4;
                for (int i = 0; i < len; i++) {
                    out.write_short ((short)value.charAt (i));
                }
                
                if (log.isDebugEnabled ()) {
                    log.debug ("write_wstring[1.2] @ "+start+"-"+out.__stream_position()
                               +" "+(out.__stream_position()-start)+"bytes"
                               +" value="+value);
                }
            }
        else
            {
                throw new org.omg.CORBA.MARSHAL ("GIOP 1."+version.minor);
            }

    }


	public void write_string(OutputStreamBase out, String value) {
		write_wstring(out, value);
	}

	public char read_char(InputStreamBase base) {
		return read_wchar(base);
	}

	public String read_string(InputStreamBase stream, int first_long) {
		return read_wstring(stream, first_long);
	}
	
    String read_wstring (InputStreamBase in, int first_long) 
    {
        if (version.minor == 0 || version.minor == 1) {
            int length = first_long;
            int before = in.__stream_position()-4;
            if (length == 0)
                return "";
            
            char[] data = new char[length];
            in.read_wchar_array (data, 0, length);
            String value = new String (data, 0, length-1);
            
            if (log.isDebugEnabled ()) {
                log.debug ("read_wstring[1.0] @ "+before+"-"+in.__stream_position()
                           +" "+(in.__stream_position()-before)+"bytes"
                           +" value="+value);
            }
            
            return value;

        } else if (version.minor == 2) {

            int length = first_long/2;
            int before = in.__stream_position()-4;
            if (length == 0)
                return "";
            
            char[] data = new char[length];
            for (int i = 0; i < length; i++) {
                data[i] = (char)in.read_ushort ();
            }

            String value = new String (data, 0, length);
            
            if (log.isDebugEnabled ()) {
                log.debug ("read_wstring[1.2] @ "+before+"-"+in.__stream_position()
                           +" "+(in.__stream_position()-before)+"bytes"
                           +" value="+value);
            }
            
            return value;

        } else {
            throw new org.omg.CORBA.MARSHAL ("GIOP 1."+version.minor);
        }

    }

    char read_wchar (InputStreamBase in)
    {
        if (version.minor == 2) {
            int len = in.read_octet ();
            if (len != 2) {
                throw new org.omg.CORBA.MARSHAL("wchar len != 2");
            }
            int high = (int) in.read_octet ();
            int low  = (int) in.read_octet ();

            if (in.__isLittleEndian ()) {
                return (char) ((low << 8) | high);
            } else {
                return (char) ((high << 8) | low);
            }

        } else if (version.minor == 1 || version.minor == 0) {
            char value = (char) in.read_ushort ();
            return value;
        } else {
            throw new org.omg.CORBA.MARSHAL ("GIOP 1."+version.minor);
        }
    }


	public static CharConverter getInstance(GIOPVersion version2) {
		return new DefaultWCharConverter(version2);
	}


}
