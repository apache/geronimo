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
package org.apache.geronimo.enterprise.deploy.tool;

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.shared.ModuleType;

/**
 * The base class for all DeployableObject implementations.  Each subclass
 * defines how to get specific deployment descriptors.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/04 05:24:21 $
 */
public abstract class AbstractDeployableObject implements DeployableObject {
    private JarFile jar;
    private ModuleType type;
    private DDBeanRoot defaultRoot;
    private ClassLoader loader;

    public AbstractDeployableObject(JarFile jar, ModuleType type, DDBeanRoot defaultRoot, ClassLoader loader) {
        this.jar = jar;
        this.type = type;
        this.defaultRoot = defaultRoot;
        this.loader = loader;
    }

    public ModuleType getType() {
        return type;
    }

    public DDBeanRoot getDDBeanRoot() {
        return defaultRoot;
    }

    public DDBean[] getChildBean(String xpath) {
        return defaultRoot.getChildBean(xpath);
    }

    public String[] getText(String xpath) {
        return defaultRoot.getText(xpath);
    }

    public Class getClassFromScope(String className) {
        try {
            return loader.loadClass(className);
        } catch(ClassNotFoundException e) {
            return null;
        }
    }

    public String getModuleDTDVersion() {
        return defaultRoot.getModuleDTDVersion();
    }

    public Enumeration entries() {
        return new JarEnumerator(jar.entries());
    }

    public InputStream getEntry(String name) {
        try {
            return jar.getInputStream(jar.getEntry(name));
        } catch(IOException e) {
            return null;
        }
    }

    private static class JarEnumerator implements Enumeration {
        private Enumeration entries;

        public JarEnumerator(Enumeration entries) {
            this.entries = entries;
        }

        public boolean hasMoreElements() {
            return entries.hasMoreElements();
        }

        public Object nextElement() {
            return ((JarEntry)entries.nextElement()).getName();
        }
    }
}
