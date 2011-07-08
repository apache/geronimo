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

package org.apache.geronimo.crypto.asn1.util;

import java.util.Enumeration;

import org.apache.geronimo.crypto.asn1.*;
import org.apache.geronimo.crypto.encoders.Hex;

public class ASN1Dump
{
    private static String  TAB = "    ";

    /**
     * dump a DER object as a formatted string with indentation
     *
     * @param obj the DERObject to be dumped out.
     */
    static String _dumpAsString(
        String      indent,
        DERObject   obj)
    {
        if (obj instanceof ASN1Sequence)
        {
            StringBuilder    buf = new StringBuilder();
            Enumeration     e = ((ASN1Sequence)obj).getObjects();
            String          tab = indent + TAB;

            buf.append(indent);
            if (obj instanceof BERConstructedSequence)
            {
                buf.append("BER ConstructedSequence");
            }
            else if (obj instanceof DERConstructedSequence)
            {
                buf.append("DER ConstructedSequence");
            }
            else if (obj instanceof BERSequence)
            {
                buf.append("BER Sequence");
            }
            else if (obj instanceof DERSequence)
            {
                buf.append("DER Sequence");
            }
            else
            {
                buf.append("Sequence");
            }

            buf.append(System.getProperty("line.separator"));

            while (e.hasMoreElements())
            {
                Object  o = e.nextElement();

                if (o == null || o.equals(new DERNull()))
                {
                    buf.append(tab);
                    buf.append("NULL");
                    buf.append(System.getProperty("line.separator"));
                }
                else if (o instanceof DERObject)
                {
                    buf.append(_dumpAsString(tab, (DERObject)o));
                }
                else
                {
                    buf.append(_dumpAsString(tab, ((DEREncodable)o).getDERObject()));
                }
            }
            return buf.toString();
        }
        else if (obj instanceof DERTaggedObject)
        {
            StringBuilder    buf = new StringBuilder();
            String          tab = indent + TAB;

            buf.append(indent);
            if (obj instanceof BERTaggedObject)
            {
                buf.append("BER Tagged [");
            }
            else
            {
                buf.append("Tagged [");
            }

            DERTaggedObject o = (DERTaggedObject)obj;

            buf.append(Integer.toString(o.getTagNo()));
            buf.append("]");

            if (!o.isExplicit())
            {
                buf.append(" IMPLICIT ");
            }

            buf.append(System.getProperty("line.separator"));

            if (o.isEmpty())
            {
                buf.append(tab);
                buf.append("EMPTY");
                buf.append(System.getProperty("line.separator"));
            }
            else
            {
                buf.append(_dumpAsString(tab, o.getObject()));
            }

            return buf.toString();
        }
        else if (obj instanceof DERConstructedSet)
        {
            StringBuilder    buf = new StringBuilder();
            Enumeration     e = ((ASN1Set)obj).getObjects();
            String          tab = indent + TAB;

            buf.append(indent);
            buf.append("ConstructedSet");
            buf.append(System.getProperty("line.separator"));

            while (e.hasMoreElements())
            {
                Object  o = e.nextElement();

                if (o == null)
                {
                    buf.append(tab);
                    buf.append("NULL");
                    buf.append(System.getProperty("line.separator"));
                }
                else if (o instanceof DERObject)
                {
                    buf.append(_dumpAsString(tab, (DERObject)o));
                }
                else
                {
                    buf.append(_dumpAsString(tab, ((DEREncodable)o).getDERObject()));
                }
            }
            return buf.toString();
        }
        else if (obj instanceof BERSet)
        {
            StringBuilder    buf = new StringBuilder();
            Enumeration     e = ((ASN1Set)obj).getObjects();
            String          tab = indent + TAB;

            buf.append(indent);
            buf.append("BER Set");
            buf.append(System.getProperty("line.separator"));

            while (e.hasMoreElements())
            {
                Object  o = e.nextElement();

                if (o == null)
                {
                    buf.append(tab);
                    buf.append("NULL");
                    buf.append(System.getProperty("line.separator"));
                }
                else if (o instanceof DERObject)
                {
                    buf.append(_dumpAsString(tab, (DERObject)o));
                }
                else
                {
                    buf.append(_dumpAsString(tab, ((DEREncodable)o).getDERObject()));
                }
            }
            return buf.toString();
        }
        else if (obj instanceof DERSet)
        {
            StringBuilder    buf = new StringBuilder();
            Enumeration     e = ((ASN1Set)obj).getObjects();
            String          tab = indent + TAB;

            buf.append(indent);
            buf.append("DER Set");
            buf.append(System.getProperty("line.separator"));

            while (e.hasMoreElements())
            {
                Object  o = e.nextElement();

                if (o == null)
                {
                    buf.append(tab);
                    buf.append("NULL");
                    buf.append(System.getProperty("line.separator"));
                }
                else if (o instanceof DERObject)
                {
                    buf.append(_dumpAsString(tab, (DERObject)o));
                }
                else
                {
                    buf.append(_dumpAsString(tab, ((DEREncodable)o).getDERObject()));
                }
            }
            return buf.toString();
        }
        else if (obj instanceof DERObjectIdentifier)
        {
            return indent + "ObjectIdentifier(" + ((DERObjectIdentifier)obj).getId() + ")" + System.getProperty("line.separator");
        }
        else if (obj instanceof DERBoolean)
        {
            return indent + "Boolean(" + ((DERBoolean)obj).isTrue() + ")" + System.getProperty("line.separator");
        }
        else if (obj instanceof DERInteger)
        {
            return indent + "Integer(" + ((DERInteger)obj).getValue() + ")" + System.getProperty("line.separator");
        }
        else if (obj instanceof BERConstructedOctetString)
        {
            return indent + "BER Constructed Octet String" + "[" + ((ASN1OctetString)obj).getOctets().length + "] " + System.getProperty("line.separator");
        }
        else if (obj instanceof DEROctetString)
        {
            return indent + "DER Octet String" + "[" + ((ASN1OctetString)obj).getOctets().length + "] " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERBitString)
        {
            return indent + "DER Bit String" + "[" + ((DERBitString)obj).getBytes().length + ", " + ((DERBitString)obj).getPadBits() + "] " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERIA5String)
        {
            return indent + "IA5String(" + ((DERIA5String)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERUTF8String)
        {
            return indent + "UTF8String(" + ((DERUTF8String)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERPrintableString)
        {
            return indent + "PrintableString(" + ((DERPrintableString)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERVisibleString)
        {
            return indent + "VisibleString(" + ((DERVisibleString)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERBMPString)
        {
            return indent + "BMPString(" + ((DERBMPString)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERT61String)
        {
            return indent + "T61String(" + ((DERT61String)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERUTCTime)
        {
            return indent + "UTCTime(" + ((DERUTCTime)obj).getTime() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERGeneralizedTime)
        {
            return indent + "GeneralizedTime(" + ((DERGeneralizedTime)obj).getTime() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERUnknownTag)
        {
            return indent + "Unknown " + Integer.toString(((DERUnknownTag)obj).getTag(), 16) + " " + new String(Hex.encode(((DERUnknownTag)obj).getData())) + System.getProperty("line.separator");
        }
        else
        {
            return indent + obj.toString() + System.getProperty("line.separator");
        }
    }

    /**
     * dump out a DER object as a formatted string
     *
     * @param obj the DERObject to be dumped out.
     */
    public static String dumpAsString(
        Object   obj)
    {
        if (obj instanceof DERObject)
        {
            return _dumpAsString("", (DERObject)obj);
        }
        else if (obj instanceof DEREncodable)
        {
            return _dumpAsString("", ((DEREncodable)obj).getDERObject());
        }

        return "unknown object type " + obj.toString();
    }
}
