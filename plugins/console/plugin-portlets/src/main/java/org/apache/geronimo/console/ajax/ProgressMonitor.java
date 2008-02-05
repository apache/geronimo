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

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.apache.geronimo.console.car.ManagementHelper;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
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
        //DownloadResults results = getPluginInstaller().checkOnInstall(downloadKey);
        ScriptProxy scriptProxy = new ScriptProxy();
        scriptProxy.addScriptSession(scriptSession);
        
        PluginInstallerGBean pluginInstallerInternal = (PluginInstallerGBean) getPluginInstaller();
        DownloadResults results = pluginInstallerInternal.checkOnInstall(downloadKey, false);
        
        //In the event results.isFinished is passed in true during polling
        scriptProxy.addFunctionCall("setMainMessage", results.getCurrentMessage());
        scriptProxy.addFunctionCall("setProgressCurrentFile", results.getCurrentFile());
        
        while (!results.isFinished()) {
            // update the progress bar
            scriptProxy.addFunctionCall("setProgressCurrentFile", results.getCurrentFile());
            scriptProxy.addFunctionCall("setProgressPercent", results.getCurrentFilePercent());
            scriptProxy.addFunctionCall("setMainMessage", results.getCurrentMessage());
            
            // get an update on the download progress, sleep time reduce to poll faster for smaller files
            Thread.sleep(100);
            //results = getPluginInstaller().checkOnInstall(downloadKey);
            results = pluginInstallerInternal.checkOnInstall(downloadKey, false);
        }
        
        if(results.isFailed()) {
            scriptProxy.addFunctionCall("setErrorMessage", results.getFailure().toString());
            throw new Exception("Unable to install configuration", results.getFailure());
        }
        
        //Fills bar at the end in the event the poller didn't catch the 100
        scriptProxy.addFunctionCall("setProgressFull");
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
