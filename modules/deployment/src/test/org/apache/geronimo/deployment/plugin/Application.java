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
package org.apache.geronimo.deployment.plugin;

import java.util.Enumeration;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.enterprise.deploy.model.J2eeApplicationObject;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;

/**
 * 
 * 
 * @version $Revision: 1.1 $ $Date: 2004/01/21 20:37:28 $
 */
public class Application implements J2eeApplicationObject {
    private final DDBeanRoot root;

        public Application(DDBeanRoot root) {
            this.root = root;
        }

        public void addXpathListener(ModuleType type, String xpath, XpathListener xpl) {
            throw new UnsupportedOperationException();
        }

        public Enumeration entries() {
            throw new UnsupportedOperationException();
        }

        public DDBean[] getChildBean(ModuleType type, String xpath) {
            throw new UnsupportedOperationException();
        }

        public DDBean[] getChildBean(String xpath) {
            throw new UnsupportedOperationException();
        }

        public Class getClassFromScope(String className) {
            throw new UnsupportedOperationException();
        }

        public DDBeanRoot getDDBeanRoot() {
            return root;
        }

        public DDBeanRoot getDDBeanRoot(String filename) throws FileNotFoundException, DDBeanCreateException {
            throw new UnsupportedOperationException();
        }

        public DeployableObject getDeployableObject(String uri) {
            throw new UnsupportedOperationException();
        }

        public DeployableObject[] getDeployableObjects() {
            throw new UnsupportedOperationException();
        }

        public DeployableObject[] getDeployableObjects(ModuleType type) {
            throw new UnsupportedOperationException();
        }

        public InputStream getEntry(String name) {
            throw new UnsupportedOperationException();
        }

        public String getModuleDTDVersion() {
            throw new UnsupportedOperationException();
        }

        public String[] getModuleUris() {
            throw new UnsupportedOperationException();
        }

        public String[] getModuleUris(ModuleType type) {
            throw new UnsupportedOperationException();
        }

        public String[] getText(ModuleType type, String xpath) {
            throw new UnsupportedOperationException();
        }

        public String[] getText(String xpath) {
            throw new UnsupportedOperationException();
        }

        public ModuleType getType() {
            throw new UnsupportedOperationException();
        }

        public void removeXpathListener(ModuleType type, String xpath, XpathListener xpl) {
            throw new UnsupportedOperationException();
        }
}
