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
package org.apache.geronimo.console.cli.controller;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.TextController;

/**
 * List deployed modules for the selected targets
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/19 01:56:14 $
 */
public class ListDeployments extends TextController {
    private static final Log log = LogFactory.getLog(ListDeployments.class);
    ModuleType type = null;
    boolean selected = false;

    public ListDeployments(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        while(true) {
            newScreen("Display Deployments");
            if(!context.connected) {
                println("ERROR: cannot display deployments in disconnected mode.");
                return;
            }
            if(selected) {
                showModules();
                if(!context.connected) {
                    return;
                }
            }
            String choice;
            while(true) {
                context.out.print("Action ([E]JB, [C]lient, [W]eb app, [R]A, [A]pp, A[L]L modules or [B]ack): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("b")) {
                    return;
                } else if(choice.equals("e")) {
                    selected = true;
                    type = ModuleType.EJB;
                    break;
                } else if(choice.equals("c")) {
                    selected = true;
                    type = ModuleType.CAR;
                    break;
                } else if(choice.equals("w")) {
                    selected = true;
                    type = ModuleType.WAR;
                    break;
                } else if(choice.equals("r")) {
                    selected = true;
                    type = ModuleType.RAR;
                    break;
                } else if(choice.equals("a")) {
                    selected = true;
                    type = ModuleType.EAR;
                    break;
                } else if(choice.equals("l")) {
                    selected = true;
                    break;
                }
            }
        }
    }

    private void showModules() {
        TargetModuleID[] ids;
        try {
            if(type != null) {
                ids = context.deployer.getAvailableModules(type, context.targets);
            } else {
                List list = new ArrayList();
                list.addAll(Arrays.asList(context.deployer.getAvailableModules(ModuleType.CAR, context.targets)));
                list.addAll(Arrays.asList(context.deployer.getAvailableModules(ModuleType.EAR, context.targets)));
                list.addAll(Arrays.asList(context.deployer.getAvailableModules(ModuleType.EJB, context.targets)));
                list.addAll(Arrays.asList(context.deployer.getAvailableModules(ModuleType.RAR, context.targets)));
                list.addAll(Arrays.asList(context.deployer.getAvailableModules(ModuleType.WAR, context.targets)));
                ids = (TargetModuleID[])list.toArray(new TargetModuleID[list.size()]);
            }
            println(ids.length == 0 ? "No matching modules found." : "Found "+ids.length+" matching module"+(ids.length == 1 ? "" : "s"));
            for(int i=0; i<ids.length; i++) {
                println("  "+ids[i].toString());
            }
            println("");
        } catch(TargetException e) {
            println("ERROR: "+e.getMessage());
        } catch(IllegalStateException e) {
            println("ERROR: No longer connected to server.");
            context.connected = false;
        }
    }
}
