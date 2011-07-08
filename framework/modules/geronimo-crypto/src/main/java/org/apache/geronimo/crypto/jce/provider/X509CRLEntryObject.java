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


package org.apache.geronimo.crypto.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.X509CRLEntry;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.crypto.asn1.DERObjectIdentifier;
import org.apache.geronimo.crypto.asn1.DEROutputStream;
import org.apache.geronimo.crypto.asn1.x509.TBSCertList;
import org.apache.geronimo.crypto.asn1.x509.X509Extension;
import org.apache.geronimo.crypto.asn1.x509.X509Extensions;

/**
 * The following extensions are listed in RFC 2459 as relevant to CRL Entries
 *
 * ReasonCode
 * Hode Instruction Code
 * Invalidity Date
 * Certificate Issuer (critical)
 */
public class X509CRLEntryObject extends X509CRLEntry
{
    private TBSCertList.CRLEntry c;

    public X509CRLEntryObject(
        TBSCertList.CRLEntry c)
    {
        this.c = c;
    }

    /**
     * Will return true if any extensions are present and marked
     * as critical as we currently dont handle any extensions!
     */
    public boolean hasUnsupportedCriticalExtension()
    {
        Set extns = getCriticalExtensionOIDs();
        if ( extns != null && !extns.isEmpty() )
        {
            return true;
        }

        return false;
    }

    private Set getExtensionOIDs(boolean critical)
    {
        X509Extensions extensions = c.getExtensions();

        if ( extensions != null )
        {
            HashSet            set = new HashSet();
            Enumeration        e = extensions.oids();

            while (e.hasMoreElements())
            {
                DERObjectIdentifier    oid = (DERObjectIdentifier)e.nextElement();
                X509Extension        ext = extensions.getExtension(oid);

                if (critical == ext.isCritical())
                {
                    set.add(oid.getId());
                }
            }

            return set;
        }

        return null;
    }

    public Set getCriticalExtensionOIDs()
    {
        return getExtensionOIDs(true);
    }

    public Set getNonCriticalExtensionOIDs()
    {
        return getExtensionOIDs(false);
    }

    public byte[] getExtensionValue(String oid)
    {
        X509Extensions exts = c.getExtensions();

        if (exts != null)
        {
            X509Extension ext = exts.getExtension(new DERObjectIdentifier(oid));

            if (ext != null)
            {
                try
                {
                    return ext.getValue().getEncoded();
                }
                catch (Exception e)
                {
                    throw new RuntimeException("error encoding " + e.getMessage(), e);
                }
            }
        }

        return null;
    }

    public byte[] getEncoded()
        throws CRLException
    {
        ByteArrayOutputStream    bOut = new ByteArrayOutputStream();
        DEROutputStream            dOut = new DEROutputStream(bOut);

        try
        {
            dOut.writeObject(c);

            return bOut.toByteArray();
        }
        catch (IOException e)
        {
            throw (CRLException)new CRLException(e.getMessage()).initCause(e);
        }
    }

    public BigInteger getSerialNumber()
    {
        return c.getUserCertificate().getValue();
    }

    public Date getRevocationDate()
    {
        return c.getRevocationDate().getDate();
    }

    public boolean hasExtensions()
    {
        return c.getExtensions() != null;
    }

    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        String nl = System.getProperty("line.separator");

        buf.append("      userCertificate: " + this.getSerialNumber() + nl);
        buf.append("       revocationDate: " + this.getRevocationDate() + nl);


        X509Extensions extensions = c.getExtensions();

        if ( extensions != null )
        {
            Enumeration e = extensions.oids();
            if ( e.hasMoreElements() )
            {
                buf.append("   crlEntryExtensions:" + nl);

                while ( e.hasMoreElements() )
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    X509Extension ext = extensions.getExtension(oid);
                    buf.append(ext);
                }
            }
        }

        return buf.toString();
    }
}
