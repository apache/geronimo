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

import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision: 1.1 $ $Date: 2004/05/19 20:53:59 $
 */
public class Module {
    protected final String name;
    protected final URI uri;
    protected XmlObject specDD;
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

    public XmlObject getSpecDD() {
        return specDD;
    }

    public void setSpecDD(XmlObject specDD) {
        this.specDD = specDD;
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
