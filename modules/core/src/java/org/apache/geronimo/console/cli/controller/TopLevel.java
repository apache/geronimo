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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.module.EJBJARInfo;
import org.apache.geronimo.console.cli.module.WARInfo;

/**
 * Top-level menu for working with the DeploymentManager.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/19 01:56:14 $
 */
public class TopLevel extends TextController {
    private static final Log log = LogFactory.getLog(TopLevel.class);

    public TopLevel(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        while(true) {
            if(context.moduleInfo instanceof EJBJARInfo) {
                new WorkWithEJBJAR(context).execute();
                continue;
            } else if(context.moduleInfo instanceof WARInfo) {
                new WorkWithWAR(context).execute();
                continue;
            }
            println("\n\nNo J2EE module is currently selected.");
            println("  "+(context.connected ? "1)" : "--")+" Control existing deployments (review, start, stop, undeploy)");
            println("  2) Select an EJB JAR or WAR to configure, deploy, or redeploy"); //todo: change text when other modules are supported
            println("  "+(context.connected ? "3)" : "--")+" Disconnect from the server.");
            String choice;
            while(true) {
                print("Action ([1-3] or [Q]uit): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("1")) {
                    if(!context.connected) {
                        continue;
                    }
                    new ControlDeployments(context).execute();
                    break;
                } else if(choice.equals("2")) {
                    new SelectModule(context).execute();
                    break;
                } else if(choice.equals("3")) {
                    if(!context.connected) {
                        continue;
                    }
                    context.deployer.release();
                    context.connected = false;
                    println("Released any server resources and disconnected.");
                    break;
                } else if(choice.equals("q")) {
                    return;
                }
            }
        }
    }
}
