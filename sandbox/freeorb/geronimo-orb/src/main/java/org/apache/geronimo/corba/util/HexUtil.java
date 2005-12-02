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

package org.apache.geronimo.corba.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

/** Utility routines for ascii-hex to binary convertion. */
public class HexUtil
{
    public static byte[] hexToByteArray (String value)
    {
        return hexToByteArray (value, 0, value.length ());
    }

    public static byte[] hexToByteArray (String value, int offset, int len)
    {
        if ((len & 1) != 0)
            throw new org.omg.CORBA.BAD_PARAM ();

        byte[] result = new byte[len/2];

        for (int i = 0; i < len/2; i++) {

            char high = value.charAt (offset+(i*2));
            char low  = value.charAt (offset+(i*2)+1);

            result[i] = (byte) ((charToInt (high) << 4) | charToInt (low));

            // System.out.println ("["+i+"] " + high + "" + low + " = " + (int)result[i]);
        }

        return result;
    }

    static int charToInt (char ch)
    {
        if (ch >= '0' && ch <= '9')
            return ch-'0';

        if (ch >= 'A' && ch <= 'F')
            return 10+ch-'A';

        if (ch >= 'a' && ch <= 'f')
            return 10+ch-'a';

        throw new org.omg.CORBA.BAD_PARAM ();
    }

	public static String byteArrayToHex (byte[] data)
	{
		return byteArrayToHex(data, 0, data.length);
	}
	
	public static String byteArrayToHex (byte[] data, int off, int len)
    {
        StringBuffer buf = new StringBuffer ();
        for (int i = 0; i < len; i++) {
            byte b = data[off+i];
            buf.append (intToChar ((b >> 4) & 0xf));
            buf.append (intToChar (b & 0xf));
        }
        return buf.toString ();
    }

    static char intToChar (int val) {

        switch (val) {
        case 0: return '0';
        case 1: return '1';
        case 2: return '2';
        case 3: return '3';
        case 4: return '4';
        case 5: return '5';
        case 6: return '6';
        case 7: return '7';
        case 8: return '8';
        case 9: return '9';
        case 10: return 'A';
        case 11: return 'B';
        case 12: return 'C';
        case 13: return 'D';
        case 14: return 'E';
        case 15: return 'F';
        default:
        throw new org.omg.CORBA.BAD_PARAM ();
        }
    }

	public static String printArray (byte[] data, int off, int len)
	{
		return printArray("", data, off, len);
	}
	
	public static void printHex (PrintWriter out, String pfx, byte[] data)
	{
		out.print(printArray(pfx, data, 0, data.length));
	}
	
	/** Generate nice readable hex dump of a binary stream with both hex and printable chars. */
	public static String printArray (String pfx, byte[] data, int off, int len)
    {
        StringBuffer result = new StringBuffer ();

        int maxhex = Integer.toHexString (off+len).length ();
        int maxnum = Integer.toString (off+len).length ();

        for (int i = 0; i < len; i += 16) {
    		result.append(pfx);
    
            int start = off+i;
            int end = start+16;
            if (end > off+len) {
                end = off+len;
            }

            String pos = Integer.toHexString (start);
            while (pos.length() < maxhex) {
                pos = " " + pos;
            }
            result.append (pos); result.append (":");

            pos = ""+start;
            while (pos.length() < maxnum) {
                pos = " " + pos;
            }
            result.append (pos); result.append (": ");

            for (int k = start; k < start+16; k++) {
                
                if (k < end) {
                    byte b = data[k];
                    result.append (intToChar ((b >> 4) & 0xf));
                    result.append (intToChar (b & 0xf));
                } else {
                    result.append ("  ");
                }

                result.append (' ');
            }
            
            result.append (' ');

            for (int k = start; k < start+16; k++) {
                
                if (k < end) {
                    byte b = data[k];
                    if ((int)b >= 32 && ((int)b) < 127) {
                        result.append ((char) b);
                    } else {
                        result.append ('.');
                    }
                } else {
                    result.append (' ');
                }
            }

            result.append ('\n');
        }

        return result.toString ();
    }

	
	static byte get(ByteBuffer[] buffers, int idx)
	{
		try {
		for (int i = 0; i < buffers.length; i++) {
			if (idx < buffers[i].remaining()) {
				return buffers[i].get(buffers[i].position() + idx);
			} else {
				idx -= buffers[i].remaining();
			}
		}
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		} catch (Error e) {
			e.printStackTrace();
			throw e;
		}
		
		throw new ArrayIndexOutOfBoundsException();
	}
	
	public static void printHex(PrintStream out, String pfx, ByteBuffer[] data) {
		
		int off = 0;
		int len = 0;
		
		for (int i = 0; i < data.length; i++) {
			len += data[i].remaining();
		}
		
        StringBuffer result = new StringBuffer ();

        int maxhex = Integer.toHexString (off+len).length ();
        int maxnum = Integer.toString (off+len).length ();

        for (int i = 0; i < len; i += 16) {
    		out.print(pfx);
    
            int start = off+i;
            int end = start+16;
            if (end > off+len) {
                end = off+len;
            }

            String pos = Integer.toHexString (start);
            while (pos.length() < maxhex) {
                pos = " " + pos;
            }
            out.print (pos); out.print (":");

            pos = ""+start;
            while (pos.length() < maxnum) {
                pos = " " + pos;
            }
            out.print (pos); out.print (": ");

            for (int k = start; k < start+16; k++) {
                
                if (k < end) {
                    byte b = get (data, k);
                    out.print (intToChar ((b >> 4) & 0xf));
                    out.print (intToChar (b & 0xf));
                } else {
                    out.print ("  ");
                }

                out.print (' ');
            }
            
            out.print (' ');

            for (int k = start; k < start+16; k++) {
                
                if (k < end) {
                    byte b = get (data, k);
                    if ((int)b >= 32 && ((int)b) < 127) {
                        out.print ((char) b);
                    } else {
                        out.print ('.');
                    }
                } else {
                    out.print (' ');
                }
            }

            out.print ('\n');
        }
	}    
}
