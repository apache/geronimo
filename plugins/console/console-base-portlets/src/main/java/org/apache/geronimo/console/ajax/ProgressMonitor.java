/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.console.ajax;

import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.proxy.ScriptProxy;

/**
 * Provides information to an AJAX client during server side activities.
 */
@RemoteProxy
public class ProgressMonitor {
    
    private PluginInstaller pluginInstaller = null;

    @RemoteMethod
    public void getProgressInfo(Integer downloadKey) throws Exception {
        // DWR objects
        WebContext wctx = WebContextFactory.get();
        ScriptSession scriptSession = wctx.getScriptSession();
        
        DownloadResults results = getPluginInstaller().checkOnInstall(downloadKey);
        ScriptProxy scriptProxy = new ScriptProxy();
        scriptProxy.addScriptSession(scriptSession);
        
        while (!results.isFinished()) {
            // update the progress bar
            scriptProxy.addFunctionCall("setProgressPercent", results.getCurrentFilePercent());
            scriptProxy.addFunctionCall("setMainMessage", results.getCurrentMessage());
            
            // get an update on the download progress
            Thread.sleep(1000);
            results = getPluginInstaller().checkOnInstall(downloadKey);
        }
        
        if(results.isFailed()) {
            throw new Exception("Unable to install configuration", results.getFailure());
        }
        
        // store the download results in the http sesssion
        wctx.getSession(true).setAttribute("console.plugins.DownloadResults", results);

        scriptProxy.addFunctionCall("setFinished");
    }
    
    private synchronized PluginInstaller getPluginInstaller() throws Exception {
        if (pluginInstaller == null) {
            Kernel kernel = KernelRegistry.getSingleKernel();
            Set<AbstractName> pluginInstallers = kernel.listGBeans(new AbstractNameQuery(PluginInstaller.class.getName()));
            if (pluginInstallers.size() == 0) {
                throw new IllegalStateException("No plugin installer registered");
            }
            pluginInstaller = (PluginInstaller) kernel.getGBean(pluginInstallers.iterator().next());
        }
        return pluginInstaller;
    }
}
