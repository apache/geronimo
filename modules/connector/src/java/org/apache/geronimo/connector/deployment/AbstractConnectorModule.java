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

package org.apache.geronimo.connector.deployment;

import java.net.URI;
import java.util.jar.JarInputStream;
import java.util.jar.JarEntry;
import java.io.IOException;
import java.io.InputStream;

import javax.management.ObjectName;

import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationCallback;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.xmlbeans.XmlException;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/09 07:10:25 $
 *
 * */
public abstract class AbstractConnectorModule implements DeploymentModule {
    public final static String BASE_RESOURCE_ADAPTER_NAME = "geronimo.management:J2eeType=ResourceAdapter,name=";
    protected final static String BASE_CONNECTION_MANAGER_FACTORY_NAME = "geronimo.management:J2eeType=ConnectionManager,name=";
    protected static final String BASE_MANAGED_CONNECTION_FACTORY_NAME = "geronimo.management:J2eeType=ManagedConnectionFactory,name=";
    protected static final String BASE_REALM_BRIDGE_NAME = "geronimo.security:service=RealmBridge,name=";
    private static final String BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME = "geronimo.security:service=Realm,type=PasswordCredential,name=";
    protected static final String BASE_ADMIN_OBJECT_NAME = "geronimo.management:service=AdminObject,name=";
    protected final URI configID;
    protected final ObjectName connectionTrackerNamePattern;
    protected InputStream moduleArchive;
    protected GerConnectorDocument geronimoConnectorDocument;

    public AbstractConnectorModule(URI configID, InputStream moduleArchive, GerConnectorDocument geronimoConnectorDocument, ObjectName connectionTrackerNamePattern) {
        this.configID = configID;
        this.moduleArchive = moduleArchive;
        this.geronimoConnectorDocument = geronimoConnectorDocument;
        this.connectionTrackerNamePattern = connectionTrackerNamePattern;
    }

    public void init() throws DeploymentException {
    }

    public void generateClassPath(ConfigurationCallback callback) throws DeploymentException {
        URI moduleBase = URI.create(configID.toString() + "/");
        JarInputStream jarInputStream;
        try {
            jarInputStream = new JarInputStream(moduleArchive);
            for (JarEntry entry; (entry = jarInputStream.getNextJarEntry()) != null; jarInputStream.closeEntry()) {
                String name = entry.getName();
                if (name.endsWith("/")) {
                    continue;
                }
                if (name.equals("META-INF/ra.xml")) {
                    getConnectorDocument(jarInputStream);
                    continue;
                }
                if (name.endsWith(".jar")) {
                    callback.addFile(moduleBase.resolve(name), jarInputStream);
                }
                //native libraries?
            }
        } catch (IOException e) {
            throw new DeploymentException(e);
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }
    }

    protected abstract void getConnectorDocument(JarInputStream jarInputStream) throws XmlException, IOException, DeploymentException;

    public void complete() {
    }
}
