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
package org.apache.geronimo.naming.java;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.transaction.UserTransaction;

import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.JNDIEnvironmentRefs;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.kernel.deployment.DeploymentException;

/**
 *
 *
 * @version $Revision: 1.11 $ $Date: 2004/01/12 06:19:52 $
 */
public class ComponentContextBuilder {

    private final ReferenceFactory referenceFactory;
    private final UserTransaction userTransaction;
    private static final String ENV = "env/";

    public ComponentContextBuilder(ReferenceFactory referenceFactory, UserTransaction userTransaction) {
        this.userTransaction = userTransaction;
        this.referenceFactory = referenceFactory;
    }

    /**
     * Build a component context from definitions contained in POJOs read from
     * a deployment descriptor.
     * @param refs the source reference definitions
     * @return a Context that can be bound to java:comp
     */
    public ReadOnlyContext buildContext(JNDIEnvironmentRefs refs) throws DeploymentException {
        ReadOnlyContext readOnlyContext = new ReadOnlyContext();
        buildEnvEntries(readOnlyContext, refs.getEnvEntry());
        buildEJBRefs(readOnlyContext, refs.getGeronimoEJBRef());
        buildEJBLocalRefs(readOnlyContext, refs.getGeronimoEJBLocalRef());
        buildResourceRefs(readOnlyContext, refs.getGeronimoResourceRef());

        if (userTransaction != null) {
            try {
                readOnlyContext.internalBind("UserTransaction", userTransaction);
            } catch (NamingException e) {
                throw new DeploymentException("could not bind UserTransaction", e);
            }
        }
        return readOnlyContext;
    }

    private static void buildEnvEntries(ReadOnlyContext readOnlyContext, EnvEntry[] envEntries) throws DeploymentException {
        for (int i = 0; i < envEntries.length; i++) {
            EnvEntry entry = envEntries[i];
            String name = entry.getEnvEntryName();
            String type = entry.getEnvEntryType();
            String value = entry.getEnvEntryValue();
            Object mapEntry;
            try {
                if (value == null) {
                    mapEntry = null;
                } else if ("java.lang.String".equals(type)) {
                    mapEntry = value;
                } else if ("java.lang.Character".equals(type)) {
                    mapEntry = new Character(value.charAt(0));
                } else if ("java.lang.Boolean".equals(type)) {
                    mapEntry = Boolean.valueOf(value);
                } else if ("java.lang.Byte".equals(type)) {
                    mapEntry = Byte.valueOf(value);
                } else if ("java.lang.Short".equals(type)) {
                    mapEntry = Short.valueOf(value);
                } else if ("java.lang.Integer".equals(type)) {
                    mapEntry = Integer.valueOf(value);
                } else if ("java.lang.Long".equals(type)) {
                    mapEntry = Long.valueOf(value);
                } else if ("java.lang.Float".equals(type)) {
                    mapEntry = Float.valueOf(value);
                } else if ("java.lang.Double".equals(type)) {
                    mapEntry = Double.valueOf(value);
                } else {
                    throw new AssertionError("Invalid class for env-entry " + name + ", " + type);
                }
            } catch (NumberFormatException e) {
                throw new DeploymentException("Invalid numeric value for env-entry " + name + ", value=" + value);
            }
            try {
                readOnlyContext.internalBind(ENV + name, mapEntry);
            } catch (NamingException e) {
                throw new DeploymentException("Could not bind", e);
            }
        }
    }

    private void buildEJBRefs(ReadOnlyContext readOnlyContext, EjbRef[] ejbRefs) throws DeploymentException {
        for (int i = 0; i < ejbRefs.length; i++) {
            EjbRef ejbRef = ejbRefs[i];
            String name = ejbRef.getEJBRefName();
            Reference ref = null;
            try {
                ref = referenceFactory.getReference(ejbRef.getEJBLink(), ejbRef);
            } catch (NamingException e) {
                throw new DeploymentException("Could not construct reference to " + ejbRef.getJndiName() + ", " + e.getMessage());
            }
            try {
                readOnlyContext.internalBind(ENV + name, ref);
            } catch (NamingException e) {
                throw new DeploymentException("could not bind", e);
            }
        }
    }

    private void buildEJBLocalRefs(ReadOnlyContext readOnlyContext, EjbLocalRef[] ejbLocalRefs) throws DeploymentException {
        for (int i = 0; i < ejbLocalRefs.length; i++) {
            EjbLocalRef ejbLocalRef = ejbLocalRefs[i];
            String name = ejbLocalRef.getEJBRefName();
            Reference ref = null;
            try {
                ref = referenceFactory.getReference(ejbLocalRef.getEJBLink(), ejbLocalRef);
            } catch (NamingException e) {
                throw new DeploymentException("Could not construct reference to " + ejbLocalRef.getJndiName() + ", " + e.getMessage());
            }
            try {
                readOnlyContext.internalBind(ENV + name, ref);
            } catch (NamingException e) {
                throw new DeploymentException("Could not bind", e);
            }
        }
    }

    private void buildResourceRefs(ReadOnlyContext readOnlyContext, ResourceRef[] resRefs) throws DeploymentException {
        for (int i=0; i < resRefs.length; i++) {
            ResourceRef resRef = resRefs[i];
            String name = resRef.getResRefName();
            String type = resRef.getResType();
            Object ref;
            if ("java.net.URL".equals(type)) {
                try {
                    ref = new URL(resRef.getURLRef());
                } catch (MalformedURLException e) {
                    throw new DeploymentException("Invalid URL for resource-ref "+name, e);
                }
            } else {
                try {
                    ref = referenceFactory.getReference(null, resRef);
                } catch (NamingException e) {
                    throw new DeploymentException("Could not construct reference to " + resRef.getJndiName());
                }
            }
            try {
                readOnlyContext.internalBind(ENV + name, ref);
            } catch (NamingException e) {
                throw new DeploymentException("Could not bind", e);
            }
        }
    }
}
