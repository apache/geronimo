/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.security;

import java.security.Policy;
import java.util.Collection;
import java.util.Iterator;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.security.jacc.GeronimoPolicy;
import org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory;
import org.apache.geronimo.security.jacc.PolicyContextHandlerContainerSubject;
import org.apache.geronimo.security.jacc.PolicyContextHandlerHttpServletRequest;
import org.apache.geronimo.security.jacc.PolicyContextHandlerSOAPMessage;
import org.apache.geronimo.security.realm.AutoMapAssistant;
import org.apache.geronimo.security.util.ConfigurationUtil;


/**
 * An MBean that maintains a list of security realms.
 *
 * @version $Rev$ $Date$
 */
public class SecurityServiceImpl implements SecurityService {

    private final Log log = LogFactory.getLog(SecurityService.class);

    private final ConcurrentHashMap mappersMap = new ConcurrentHashMap();

    /**
     * Permissions that protect access to sensitive security information
     */
    public static final GeronimoSecurityPermission CONFIGURE = new GeronimoSecurityPermission("configure");

    public SecurityServiceImpl(String policyConfigurationFactory,
                               Collection mappers) throws PolicyContextException, ClassNotFoundException {
        /**
         *  @see "JSR 115 4.6.1" Container Subject Policy Context Handler
         */
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerContainerSubject(), true);
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerSOAPMessage(), true);
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerHttpServletRequest(), true);

        if (policyConfigurationFactory != null) {
            System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", policyConfigurationFactory);
        }
        PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        GeronimoPolicyConfigurationFactory geronimoPolicyConfigurationFactory = (GeronimoPolicyConfigurationFactory) factory;
        Policy.setPolicy(new GeronimoPolicy(geronimoPolicyConfigurationFactory));
        if (mappers != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(CONFIGURE);
            }
            ((ReferenceCollection) mappers).addReferenceCollectionListener(new ReferenceCollectionListener() {

                public void memberAdded(ReferenceCollectionEvent event) {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(CONFIGURE);
                    }
                    AutoMapAssistant assistant = (AutoMapAssistant) event.getMember();
                    mappersMap.put(assistant.getRealmName(), assistant);
                }

                public void memberRemoved(ReferenceCollectionEvent event) {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(CONFIGURE);
                    }
                    AutoMapAssistant assistant = (AutoMapAssistant) event.getMember();
                    mappersMap.remove(assistant.getRealmName());
                }
            });
            for (Iterator iterator = mappers.iterator(); iterator.hasNext();) {
                AutoMapAssistant assistant = (AutoMapAssistant) iterator.next();
                mappersMap.put(assistant.getRealmName(), assistant);
            }
        }
        log.info("Security service started");
    }

    public AutoMapAssistant getMapper(String name) {
        return (AutoMapAssistant) mappersMap.get(name);
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(SecurityServiceImpl.class);

        infoFactory.addAttribute("policyConfigurationFactory", String.class, true);

        infoFactory.addReference("Mappers", AutoMapAssistant.class);
        infoFactory.addOperation("getMapper", new Class[]{String.class});

        infoFactory.setConstructor(new String[]{"policyConfigurationFactory", "Mappers"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
