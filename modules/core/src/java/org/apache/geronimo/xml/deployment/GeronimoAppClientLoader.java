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
package org.apache.geronimo.xml.deployment;

import org.apache.geronimo.deployment.model.geronimo.appclient.ApplicationClient;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EJBRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ServiceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestination;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * 
 * @version $Revision: 1.1 $ $Date: 2003/09/05 20:18:03 $
 */
public class GeronimoAppClientLoader {
    private GeronimoJ2EELoader j2eeLoader = new GeronimoJ2EELoader();

    public ApplicationClient load(Document geronimoDoc) {
        Element root = geronimoDoc.getDocumentElement();
        if (!"application-client".equals(root.getTagName())) {
            throw new IllegalArgumentException("Document is not an application-client instance");
        }
        ApplicationClient appClient = new ApplicationClient();
        appClient.setEnvEntry(j2eeLoader.loadEnvEntries(root, new EnvEntry[0]));
        appClient.setEJBRef((EJBRef[])j2eeLoader.loadEJBRefs(root, new EJBRef[0]));
        appClient.setServiceRef((ServiceRef[])j2eeLoader.loadServiceRefs(root, new ServiceRef[0]));
        appClient.setResourceRef((ResourceRef[])j2eeLoader.loadResourceRefs(root, new ResourceRef[0]));
        appClient.setResourceEnvRef((ResourceEnvRef[])j2eeLoader.loadResourceEnvRefs(root, new ResourceEnvRef[0]));
        appClient.setMessageDestinationRef((MessageDestinationRef[]) j2eeLoader.loadMessageDestinationRefs(root, new MessageDestinationRef[0]));
        appClient.setCallbackHandler(LoaderUtil.getChildContent(root, "callback-handler"));
        appClient.setMessageDestination((MessageDestination[])j2eeLoader.loadMessageDestinations(root, new MessageDestination[0]));
        return appClient;
    }
}
