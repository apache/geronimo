/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.aries.shell;

import java.util.Set;

import org.apache.aries.application.management.ApplicationContext;
import org.apache.aries.application.management.ApplicationContextManager;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "eba", name = "list", description = "List Applications")
public class ListApplication extends ApplicationCommandSupport {

    @Argument(required = false, description = "Application name")
    String applicationName;
    
    @Override
    protected Object doExecute() throws Exception {
        ApplicationContextManager contextManager = getApplicationContextManager();
        if (applicationName == null) {
            System.out.println("EBA Applications:");
            Set<ApplicationContext> contexts = contextManager.getApplicationContexts();
            for (ApplicationContext context : contexts) {
                System.out.println(context.getApplication().getApplicationMetadata().getApplicationScope() + "\t" + context.getApplicationState());
            }
        } else {
            ApplicationContext context = findApplicationContext(contextManager, applicationName);
            if (context == null) {
                System.out.println("EBA " + applicationName + " not found");
            } else {
                System.out.println("EBA: " + applicationName);
                System.out.println("Bundles : " + context.getApplicationContent());
                System.out.println("State   : " + context.getApplicationState());
            }
        }
        return null;
    }

}
