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

package org.apache.geronimo.crypto.jce;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.geronimo.crypto.asn1.ASN1InputStream;
import org.apache.geronimo.crypto.asn1.ASN1Sequence;
import org.apache.geronimo.crypto.asn1.DEROutputStream;
import org.apache.geronimo.crypto.asn1.x509.X509Name;

public class X509Principal
    extends X509Name
    implements Principal
{
    /**
     * Constructor from an encoded byte array.
     */
    public X509Principal(
        byte[]  bytes)
        throws IOException
    {
        super((ASN1Sequence)(new ASN1InputStream(new ByteArrayInputStream(bytes)).readObject()));
    }

    /**
     * Constructor from an X509Name object.
     */
    public X509Principal(
        X509Name  name)
    {
        super((ASN1Sequence)name.getDERObject());
    }

    /**
     * constructor from a table of attributes.
     * <p>
     * it's is assumed the table contains OID/String pairs.
     */
    public X509Principal(
        Hashtable  attributes)
    {
        super(attributes);
    }

    /**
     * constructor from a table of attributes and a vector giving the
     * specific ordering required for encoding or conversion to a string.
     * <p>
     * it's is assumed the table contains OID/String pairs.
     */
    public X509Principal(
        Vector      ordering,
        Hashtable   attributes)
    {
        super(ordering, attributes);
    }

    /**
     * constructor from a vector of attribute values and a vector of OIDs.
     */
    public X509Principal(
        Vector      oids,
        Vector      values)
    {
        super(oids, values);
    }

    /**
     * takes an X509 dir name as a string of the format "C=AU,ST=Victoria", or
     * some such, converting it into an ordered set of name attributes.
     */
    public X509Principal(
        String  dirName)
    {
        super(dirName);
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU,ST=Victoria", or
     * some such, converting it into an ordered set of name attributes. If reverse
     * is false the dir name will be encoded in the order of the (name, value) pairs
     * presented, otherwise the encoding will start with the last (name, value) pair
     * and work back.
     */
    public X509Principal(
        boolean reverse,
        String  dirName)
    {
        super(reverse, dirName);
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes. lookUp
     * should provide a table of lookups, indexed by lowercase only strings and
     * yielding a DERObjectIdentifier, other than that OID. and numeric oids
     * will be processed automatically.
     * <p>
     * If reverse is true, create the encoded version of the sequence starting
     * from the last element in the string.
     */
    public X509Principal(
        boolean     reverse,
        Hashtable   lookUp,
        String      dirName)
    {
        super(reverse, lookUp, dirName);
    }

    public String getName()
    {
        return this.toString();
    }

    /**
     * return a DER encoded byte array representing this object
     */
    public byte[] getEncoded()
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);

        try
        {
            dOut.writeObject(this);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }

        return bOut.toByteArray();
    }
}
