/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.deployment.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.security.auth.login.FailedLoginException;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginList;
import org.apache.geronimo.system.plugin.PluginMetadata;

/**
 * The CLI deployer logic to start.
 *
 * @version $Rev: 356097 $ $Date: 2005-12-11 20:29:03 -0500 (Sun, 11 Dec 2005) $
 */
public class CommandListConfigurations extends AbstractCommand {
    public CommandListConfigurations() {
        super("search-plugins", "3. Geronimo Plugins", "[MavenRepoURL]",
                "Lists the Geronimo plugins available in a Maven repository "+
                "and lets you select a plugin to download and install.  This "+
                "is used to add new functionality to the Geronimo server.  If " +
                "no repository is specified the default repositories will be " +
                "listed to select from, but this means there must have been " +
                "some default repositories set (by hand or by having the " +
                "console update to the latest defaults).");
    }

    //todo: provide a way to handle a username and password for the remote repo?

    public CommandListConfigurations(String command, String group, String helpArgumentList, String helpText) {
        super(command, group, helpArgumentList, helpText);
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] args) throws DeploymentException {
        DeploymentManager dmgr = connection.getDeploymentManager();
        if(dmgr instanceof GeronimoDeploymentManager) {
            GeronimoDeploymentManager mgr = (GeronimoDeploymentManager) dmgr;
            try {
                String repo = null;
                if(args.length == 1) {
                    repo = args[0];
                } else {
                    repo = getRepository(out, new BufferedReader(new InputStreamReader(System.in)), mgr);
                }
                PluginList data;
                URL repository;
                try {
                    repository = new URL(repo);
                    data = mgr.listPlugins(repository, null, null);
                } catch (IOException e) {
                    throw new DeploymentException("Unable to list configurations", e);
                } catch (FailedLoginException e) {
                    throw new DeploymentException("Invalid login for Maven repository '"+repo+"'");
                }
                Map categories = new HashMap();
                List available = new ArrayList();
                for (int i = 0; i < data.getPlugins().length; i++) {
                    PluginMetadata metadata = data.getPlugins()[i];
                    List list = (List) categories.get(metadata.getCategory());
                    if(list == null) {
                        list = new ArrayList();
                        categories.put(metadata.getCategory(), list);
                    }
                    list.add(metadata);
                }
                for (Iterator it = categories.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String category = (String) entry.getKey();
                    List items = (List) entry.getValue();
                    out.println();
                    out.print(DeployUtils.reformat(category, 4, 72));
                    for (int i = 0; i < items.size(); i++) {
                        PluginMetadata metadata = (PluginMetadata) items.get(i);
                        String prefix = "    ";
                        if(!metadata.isInstalled() && metadata.isEligible()) {
                            available.add(metadata);
                            prefix = Integer.toString(available.size());
                            if(available.size() < 10) {
                                prefix += " ";
                            }
                            prefix += ": ";
                        }
                        out.print(DeployUtils.reformat(prefix+metadata.getName()+" ("+metadata.getVersion()+")", 8, 72));
                        out.flush();
                    }
                }
                if(available.size() == 0) {
                    out.println();
                    out.println("No plugins from this site are eligible for installation.");
                    return;
                }
                out.println();
                out.print("Install Service [enter number or 'q' to quit]: ");
                out.flush();
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String answer = in.readLine();
                if(answer.equalsIgnoreCase("q")) {
                    return;
                }
                int selection = Integer.parseInt(answer);
                PluginMetadata target = ((PluginMetadata) available.get(selection - 1));
                long start = System.currentTimeMillis();
                Object key = mgr.startInstall(PluginList.createInstallList(data, target.getModuleId()), null, null);
                DownloadResults results = CommandInstallCAR.showProgress(mgr, key);
                int time = (int)(System.currentTimeMillis() - start) / 1000;
                out.println();
                if(!results.isFailed()) {
                    out.println(DeployUtils.reformat("**** Installation Complete!", 4, 72));
                    for (int i = 0; i < results.getDependenciesPresent().length; i++) {
                        Artifact uri = results.getDependenciesPresent()[i];
                        out.print(DeployUtils.reformat("Used existing: "+uri, 4, 72));
                    }
                    for (int i = 0; i < results.getDependenciesInstalled().length; i++) {
                        Artifact uri = results.getDependenciesInstalled()[i];
                        out.print(DeployUtils.reformat("Installed new: "+uri, 4, 72));
                    }
                    out.println();
                    out.println(DeployUtils.reformat("Downloaded "+(results.getTotalDownloadBytes()/1024)+" kB in "+time+"s ("+results.getTotalDownloadBytes()/(1024*time)+" kB/s)", 4, 72));
                }
                if(results.isFinished() && !results.isFailed()) {
                    out.print(DeployUtils.reformat("Now starting "+target.getModuleId()+"...", 4, 72));
                    out.flush();
                    new CommandStart().execute(out, connection, new String[]{target.getModuleId().toString()});
                }
            } catch (IOException e) {
                throw new DeploymentException("Unable to install configuration", e);
            } catch(NumberFormatException e) {
                throw new DeploymentException("Invalid response");
            }
        } else {
            throw new DeploymentException("Cannot list repositories when connected to "+connection.getServerURI());
        }
    }

    private String getRepository(PrintWriter out, BufferedReader in, GeronimoDeploymentManager mgr) throws IOException, DeploymentException {
        URL[] all = mgr.getRepositories();
        if(all.length == 0) {
            throw new DeploymentException("No default repositories available.  Please either specify the repository " +
                    "URL on the command line, or go into the console Plugin page and update the list of available " +
                    "repositories.");
        }
        out.println();
        out.println("Select repository:");
        for (int i = 0; i < all.length; i++) {
            URL url = all[i];
            out.println("  "+(i+1)+". "+url);
        }
        out.println();
        out.print("Enter Repository Number: ");
        out.flush();
        String entry = in.readLine().trim();
        int index = Integer.parseInt(entry);
        return all[index-1].toString();
    }
}
