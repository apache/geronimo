/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.j2ee.deployment;

import java.net.URI;
import java.net.URL;

import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision: 1.2 $ $Date: 2004/08/09 04:19:35 $
 */
public class Module {
    protected final String name;
    protected final URI uri;
    protected URL altSpecDD;
    protected XmlObject specDD;
    protected URL altVendorDD;
    protected XmlObject vendorDD;

    public Module(String name, URI uri) {
        assert name != null: "Module name is null";
        this.name = name;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public URI getURI() {
        return uri;
    }

    public URL getAltSpecDD() {
        return altSpecDD;
    }
    
    public void setAltSpecDD(URL altSpecDD) {
        this.altSpecDD = altSpecDD;
    }
    
    public XmlObject getSpecDD() {
        return specDD;
    }

    public void setSpecDD(XmlObject specDD) {
        this.specDD = specDD;
    }

    public URL getAltVendorDD() {
        return altVendorDD;
    }
    
    public void setAltVendorDD(URL altVendorDD) {
        this.altVendorDD = altVendorDD;
    }
    
    public XmlObject getVendorDD() {
        return vendorDD;
    }

    public void setVendorDD(XmlObject vendorDD) {
        this.vendorDD = vendorDD;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Module) {
            Module module = (Module) obj;
            return name.equals(module.name);
        }
        return false;
    }
    
}
