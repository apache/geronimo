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
import javax.enterprise.deploy.spi.Target;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;

/**
 * Select targets to operate on.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/19 01:56:14 $
 */
public class SelectServer extends TextController {
    private static final Log log = LogFactory.getLog(SelectServer.class);
    private Target[] all;
    private Target[] available;
    public SelectServer(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        all = context.deployer.getTargets();
        if(all.length == 0) {
            println("  ERROR: No targets available.");
            println("         Perhaps the deployment manager is not connected?");
            return;
        }
        while(true) {
            available = available(all, context.targets);
            newScreen("Select Targets");
            println((context.targets.length == 0 ? "No" : String.valueOf(context.targets.length))+" target"+(context.targets.length != 1 ? "s" : "")+" currently selected"+((context.targets.length > 0 ? ":" : ".")));
            for(int i=0; i<context.targets.length; i++) {
                println("  "+(i+1)+") "+context.targets[i].getName()+" ("+truncate(context.targets[i].getDescription(), 66-context.targets[i].getName().length())+")");
            }
            String choice;
            while(true) {
                context.out.print("Action ("+(context.targets.length > 0 ? "Remove Target [1"+(context.targets.length > 1 ? "-"+context.targets.length : "")+"] or " : "")+(available.length > 0 ? "[A]dd Target or " : "")+"[B]ack): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("a") && available.length > 0) {
                    new AddServer(context, all).execute();
                    break;
                } else if(choice.equals("b")) {
                    return;
                } else {
                    int i = 0;
                    try {
                        i = Integer.parseInt(choice);
                    } catch(NumberFormatException e) {
                        continue;
                    }
                    if(i < 1 || i > context.targets.length) {
                        println("  ERROR: There are only "+context.targets.length+" target(s) selected");
                    } else {
                        Target[] list = new Target[context.targets.length-1];
                        System.arraycopy(context.targets, 0, list, 0, i-1);
                        System.arraycopy(context.targets, i, list, i-1, context.targets.length-i);
                        context.targets = list;
                        break;
                    }
                }
            }
        }
    }
}
