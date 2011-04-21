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

import java.io.File;
import java.io.FileOutputStream;

import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.AriesApplicationManager;
import org.apache.aries.application.utils.filesystem.FileSystem;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.ServiceReference;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "eba", name = "resolve", description = "Resolve Aries Application")
public class ResolveApplication extends OsgiCommandSupport {

    @Argument(required = true, description = "Aries Application location")
    String applicationPath;
    
    @Option(name = "-o", aliases = { "--out" }, description = "Location to output the resolved application")
    String resovledApplicationPath;

    protected AriesApplicationManager getAriesApplicationManager() {
        ServiceReference ref = 
            bundleContext.getServiceReference(AriesApplicationManager.class.getName());
        return getService(AriesApplicationManager.class, ref);
    }
    
    @Override
    protected Object doExecute() throws Exception {
        File sourceApplication = new File(applicationPath);
        AriesApplicationManager manager = getAriesApplicationManager();
        AriesApplication application = manager.createApplication(FileSystem.getFSRoot(sourceApplication));  
        
        if (application.isResolved()) {
            System.out.println("Application " + application.getApplicationMetadata().getApplicationSymbolicName() + " is already resolved.");
        } else {
            System.out.println("Attempting to resolve " + application.getApplicationMetadata().getApplicationSymbolicName() + " application.");
            
            AriesApplication resolved = manager.resolve(application);
            File targetApplication = null;
            if (resovledApplicationPath == null) {
                targetApplication = new File(applicationPath + ".tmp");
            } else {
                targetApplication = new File(resovledApplicationPath);
            }
            FileOutputStream os = new FileOutputStream(targetApplication);
            try {
                resolved.store(os);
            } finally {            
                os.close();
            }
            if (resovledApplicationPath == null) {
                targetApplication.renameTo(sourceApplication);
                targetApplication.delete();
            }
            
            System.out.println("Application " + application.getApplicationMetadata().getApplicationSymbolicName() + " is now resolved.");
        }

        return null;
    }
 
}
