/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment.model.connector;

import org.apache.geronimo.deployment.model.j2ee.Displayable;

/**
 * JavaBean for the root ra.xml tag connector
 *  
 * @version $Revision: 1.2 $ $Date: 2003/11/10 20:49:51 $
 */
public class Connector extends Displayable {
    private String vendorName;
    private String eisType;
    private String resourceAdapterVersion;
    private License license;
    private ResourceAdapter resourceAdapter;
    private String version;
    
    public Connector() {
    }
    
    public License getLicense() {
        return license;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public String getResourceAdapterVersion() {
        return resourceAdapterVersion;
    }

    public String getVendorName() {
        return vendorName;
    }

    public String getVersion() {
        return version;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public void setResourceAdapter(ResourceAdapter resourceadapater) {
        resourceAdapter = resourceadapater;
    }

    public void setResourceAdapterVersion(String string) {
        resourceAdapterVersion = string;
    }

    public void setVendorName(String string) {
        vendorName = string;
    }

    public void setVersion(String string) {
        version = string;
    }

    public String getEisType() {
        return eisType;
    }

    public void setEisType(String string) {
        eisType = string;
    }
}
