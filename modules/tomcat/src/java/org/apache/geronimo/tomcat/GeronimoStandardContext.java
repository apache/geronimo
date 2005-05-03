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
package org.apache.geronimo.tomcat;

import java.util.List;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.naming.java.SimpleReadOnlyContext;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.tomcat.valve.ComponentContextValve;
import org.apache.geronimo.tomcat.valve.InstanceContextValve;
import org.apache.geronimo.tomcat.valve.PolicyContextValve;
import org.apache.geronimo.tomcat.valve.TransactionContextValve;
import org.apache.geronimo.transaction.context.TransactionContextManager;

public class GeronimoStandardContext extends StandardContext{
    
    private static final Log log = LogFactory.getLog(GeronimoStandardContext.class);

    private static final long serialVersionUID = 3834587716552831032L;

    private Subject defaultSubject = null;
    
    public void setContextProperties(TomcatContext ctx){
    
        // Create ReadOnlyContext
        javax.naming.Context enc = null;
        Map componentContext = ctx.getComponentContext();
        try {
            if (componentContext != null) {
                for (Iterator iterator = componentContext.values().iterator(); iterator
                        .hasNext();) {
                    Object value = iterator.next();
                    if (value instanceof KernelAwareReference) {
                        ((KernelAwareReference) value).setKernel(ctx.getKernel());
                    }
                    if (value instanceof ClassLoaderAwareReference) {
                        ((ClassLoaderAwareReference) value)
                                .setClassLoader(getLoader().getClassLoader());
                    }
                }
                enc = new SimpleReadOnlyContext(componentContext);
            }
        } catch (NamingException ne) {
            log.error(ne);
        }

        //Set the InstanceContextValve
        InstanceContextValve instanceContextValve = 
            new InstanceContextValve(ctx.getUnshareableResources(),
                    ctx.getApplicationManagedSecurityResources(),
                    ctx.getTrackedConnectionAssociator());
        addValve(instanceContextValve);
            
        // Set ComponentContext valve
        if (enc != null) {
            ComponentContextValve contextValve = new ComponentContextValve(enc);
            addValve(contextValve);
        }

        // Set TransactionContextValve
        TransactionContextManager transactionContextManager = ctx.getTransactionContextManager();
        if (transactionContextManager != null) {
            TransactionContextValve transactionValve = new TransactionContextValve(
                    transactionContextManager);
            addValve(transactionValve);
        }

        //Set a PolicyContext Valve
        SecurityHolder securityHolder = ctx.getSecurityHolder();
        if (securityHolder != null){
            if (securityHolder.getPolicyContextID() != null) {
                
                PolicyContext.setContextID(securityHolder.getPolicyContextID());
                
                /**
                 * Register our default subject with the ContextManager
                 */
                DefaultPrincipal defaultPrincipal = securityHolder.getDefaultPrincipal();
                if (defaultPrincipal != null){
                    defaultSubject = ConfigurationUtil.generateDefaultSubject(defaultPrincipal);
                    ContextManager.registerSubject(defaultSubject);
                    SubjectId id = ContextManager.getSubjectId(defaultSubject);
                    defaultSubject.getPrincipals().add(new IdentificationPrincipal(id));       
                }
                
                PolicyContextValve policyValve = new PolicyContextValve(
                    securityHolder.getPolicyContextID());
                addValve(policyValve);
            }    
        }
        
        // Add User Defined Valves
        List valveChain = ctx.getValveChain();
        if (valveChain != null){
            Iterator iterator = valveChain.iterator();
            while(iterator.hasNext()){
                addValve((Valve)iterator.next());
            }
        }


    }

    public synchronized void start() throws LifecycleException {
        super.start();
    }

    public synchronized void stop() throws LifecycleException {
        // Remove the defaultSubject
        if (defaultSubject != null){
            ContextManager.unregisterSubject(defaultSubject);
        }
        
       super.stop();
    }
    
    
}
