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
package org.apache.geronimo.enterprise.deploy.provider;

import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;

/**
 * The Geronimo implementation of the JSR-88 DeploymentFactory.  This is
 * analagous to a JDBC driver.  It provides access to usable
 * DeploymentManager instances, either disconnected, or connected to a
 * specific Geronimo server based on a URL, username, and password.
 *
 * URLs for this DeploymentFactory should look like this for disconnected
 * mode:
 *
 * <pre>deployer:geronimo:</pre>
 *
 * For connected mode, they should look like this:
 *
 * <pre>deployer:geronimo://server:port/application</pre>
 *
 * (Note that connected mode is not yet implemented)
 *
 * @version $Revision: 1.2 $
 */
public class GeronimoDeploymentFactory implements DeploymentFactory {
    static { // Auto-registers a GeronimoDeploymentFactory
        DeploymentFactoryManager.getInstance().registerDeploymentFactory(new GeronimoDeploymentFactory());
    }
    // All Geronimo URLs must start with this
    private final static String URI_PREFIX = "deployer:geronimo:";

    /**
     * Ensures that the URI starts with the blessed Geronimo prefix.
     */
    public boolean handlesURI(String uri) {
        return uri.startsWith(URI_PREFIX);
    }

    /**
     * Validates that the entire URI is well-formed, by splitting it up into
     * it components.  Assumes that handlesURI has already returned true.
     *
     * @return The components of the URI, or <tt>null</tt> if the URI is not valid.
     */
    private Address parseURI(String uri) {
        if(uri.equals(URI_PREFIX)) {
            return new Address();
        }
        if(!uri.startsWith(URI_PREFIX+"//")) {
            return null;
        }
        String end = uri.substring(URI_PREFIX.length()+2);
        String server = null, port = null, application = null;
        int pos = end.indexOf('/');
        if(pos > -1) { // includes an application
            if(end.indexOf(',', pos+1) > -1) {
                return null;
            }
            application = end.substring(pos+1);
            end = end.substring(0, pos);
        }
        pos = end.indexOf(':');
        if(pos > -1) { // includes a port
            if(end.indexOf(':', pos+1) > -1) {
                return null;
            }
            port = end.substring(pos+1);
            end = end.substring(0, pos);
        }
        server = end;
        Address add = new Address();
        add.server = server;
        try {
            add.port = port == null ? null : new Integer(port);
        } catch(NumberFormatException e) {
            return null;
        }
        add.application = application;
        return add;
    }

    /**
     * Currently always returns a disconnected DeploymentManager, but will
     * eventually return a connected one.
     *
     * @throws DeploymentManagerCreationException Occurs when the specified
     *         URI, username, and password are not valid to connect to a
     *         Geronimo server.
     */
    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        if(!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI for "+getDisplayName()+" "+getProductVersion()+" DeploymentFactory ("+uri+"), expecting "+URI_PREFIX+"...");
        }
        Address add = parseURI(uri);
        if(add == null) {
            throw new DeploymentManagerCreationException("Invalid URI for "+getDisplayName()+" "+getProductVersion()+" DeploymentFactory ("+uri+"), expecting "+URI_PREFIX+"//server:port/application");
        }
        if(add.server != null) {
            if(add.port != null || add.application != null) {
                System.err.println("WARNING: Currently, the port and application parts of the URL are ignored.");
            }
            try {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                //todo: Figure out a way around this (either make everything try the current CL as well as the TCCL, or set/unset the TCCL on every operation...)
                System.err.println("Replacing Context ClassLoader: "+old);
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                return new GeronimoDeploymentManager(new JmxServerConnection(add.server));
            } catch(Exception e) {
                e.printStackTrace();
                throw new DeploymentManagerCreationException("Unable to connect to Geronimo server at "+uri+": "+e.getMessage());
            }
        } else {
            return new GeronimoDeploymentManager(new NoServerConnection());
        }
    }

    /**
     * Returns a connected DeploymentManager.
     *
     * @throws DeploymentManagerCreationException Occurs when the specified
     *         URI is not a Geronimo URI
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if(!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI for "+getDisplayName()+" "+getProductVersion()+" DeploymentFactory ("+uri+"), expecting "+URI_PREFIX+"...");
        }
        return new GeronimoDeploymentManager(new NoServerConnection());
    }

    public String getDisplayName() {
        return "Geronimo";
    }

    public String getProductVersion() {
        return "1.0";
    }

    /**
     * All instances of this class are equivalent.
     *
     * @return The same value, always.
     */
    public int hashCode() {
        return 42;
    }

    /**
     * All instances of this class are equivalent.
     *
     * @return True of the argument is a GeronimoDeploymentFactory
     */
    public boolean equals(Object o) {
        return o instanceof GeronimoDeploymentFactory;
    }

    public String toString() {
        return getDisplayName()+" "+getProductVersion()+" DeploymentFactory";
    }

    private static class Address {
        public String server;
        public Integer port;
        public String application;
    }
}
