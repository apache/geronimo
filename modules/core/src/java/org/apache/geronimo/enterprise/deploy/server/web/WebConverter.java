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
package org.apache.geronimo.enterprise.deploy.server.web;

import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.model.geronimo.web.WebApp;
import org.apache.geronimo.enterprise.deploy.server.j2ee.J2EEConverter;
import org.apache.geronimo.enterprise.deploy.server.DConfigBeanLookup;

/**
 * Maps DConfigBeans to POJOs and vice versa.
 *
 * When converting POJOs to DConfigBeans, we ignore everything except the
 * Geronimo-specific content.  That way, we don't have to listen on changes
 * on every single element in the whole standard DD.
 *
 * When converting DConfigBeans to POJOs, we use the matching DDBeans to
 * look up all the info that isn't covered in the Geronimo DD for each
 * DConfigBean.  Note this means that the standard DD content may be out of
 * sync when loaded, but they'll be cleaned up when the DD is saved.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/07 17:16:36 $
 */
public class WebConverter extends J2EEConverter {
    private static final Log log = LogFactory.getLog(WebConverter.class);

    /**
     * Convert Geronimo POJOs and the web.xml content into DConfigBeans
     *
     * @param custom    The geronimo POJOs
     * @param standard  The web.xml content
     * @param lookup    DConfigBeans need this so they can use the connection to the server
     * @return          The DConfigBeanRoot for the Geronimo web DD
     */
    public static WebAppRoot loadDConfigBeans(WebApp custom, DDBeanRoot standard, DConfigBeanLookup lookup) throws ConfigurationException {
        WebAppRoot root = new WebAppRoot(standard, lookup);
        WebAppBean webApp = (WebAppBean)root.getDConfigBean(standard.getChildBean(WebAppRoot.WEB_APP_XPATH)[0]);
        assignEnvironmentRefs(webApp, custom, webApp.getDDBean());
        return root;
    }

    /**
     * Converts populated DConfigBeans into Geronimo POJOs so the DD can be
     * written.
     *
     * @param root The DConfigBeanRoot
     * @return     The populated Geronimo web POJOs
     */
    public static WebApp storeDConfigBeans(WebAppRoot root) throws ConfigurationException {
        if(root == null || root.getWebApp() == null) {
            throw new ConfigurationException("Insufficient configuration information to save.");
        }
        WebApp app = new WebApp();
        loadDisplayable(root.getWebApp().getDDBean(), app);
        storeJndiEnvironment(app, root.getWebApp());
        //todo: all other J2EE web app properties
//        jar.setVersion(root.getEjbJar().getDDBean().getAttributeValue("version"));
        return app;
    }
}
