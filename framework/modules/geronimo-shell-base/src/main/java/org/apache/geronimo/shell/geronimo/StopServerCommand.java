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

package org.apache.geronimo.shell.geronimo;



import org.apache.felix.gogo.commands.Command;
import org.apache.geronimo.shell.deploy.ConnectCommand;
import org.osgi.framework.Bundle;
/**
 * @version $Rev$ $Date$
 */
@Command(scope = "geronimo", name = "stop-server",  description = "Stop Server")
public class StopServerCommand extends ConnectCommand {

    @Override
    protected Object doExecute() throws Exception {
        //TODO: to stop remote server 
        println("Stopping Geronimo server...");
        Bundle[] bundles = bundleContext.getBundles();
        for(Bundle bundle:bundles){
            if(bundle.getLocation().equals("mvn:org.apache.geronimo.framework/j2ee-system/3.0-SNAPSHOT/car")){
                try {
                    bundle.stop();
                    println("Shutdown request has been issued");
                    super.disconnect();
                }
                catch (Exception e) {
                    log.debug("Failed to request shutdown:", e);
                    println("Unable to shutdown the server: "+e.getMessage());
                }
                
                break;
            }
        }
        
        
        return null;
    }

}
