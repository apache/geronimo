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
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;

/**
 * The screen that lets you distribute, deploy, or redeploy the current module.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/19 01:56:14 $
 */
public class DeploymentOptions extends TextController {
    private static final Log log = LogFactory.getLog(DeploymentOptions.class);

    public DeploymentOptions(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        while(true) {
            newScreen("Deploy Module");
            if(!context.connected) {
                println("ERROR: cannot deploy in disconnected mode.");
                return;
            }
            context.out.println((context.targets.length == 0 ? "No" : String.valueOf(context.targets.length))+" target"+(context.targets.length != 1 ? "s" : "")+" currently selected.");
            context.out.println("  1) Select targets (usually servers or clusters) to work with");
            context.out.println("  "+(context.targets.length > 0 ? "2)" : "--")+" Distribute "+context.moduleInfo.file.getName()+" to selected targets");
            context.out.println("  "+(context.targets.length > 0 ? "UN" : "--")+" Deploy "+context.moduleInfo.file.getName()+" to selected targets");
            context.out.println("  "+(context.targets.length > 0 ? "UN" : "--")+" Redeploy "+context.moduleInfo.file.getName()+" to selected targets");
            String choice;
            while(true) {
                context.out.print("Action ([1"+(context.targets.length > 0 ? "-4" : "")+"] or [B]ack): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                try {
                    if(choice.equals("1")) {
                        new SelectServer(context).execute();
                        break;
                    } else if(choice.equals("2")) {
                        distribute();
                        break;
                    } else if(choice.equals("3")) { //todo
                        break;
                    } else if(choice.equals("4")) { //todo
                        break;
                    } else if(choice.equals("b")) {
                        return;
                    }
                } catch(ConfigurationException e) {
                    log.error("Server action failed", e);
                } catch(IOException e) {
                    log.error("Server action failed", e);
                }
            }
        }
    }

    private void distribute() throws ConfigurationException, IOException {
        File dd = spool();
        context.deployer.distribute(context.targets, context.moduleInfo.file, dd);
    }

    private File spool() throws IOException, ConfigurationException {
        File temp = File.createTempFile("gerdd", "xml");
        temp.deleteOnExit();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
        context.serverModule.save(out);
        out.flush();
        out.close();
        return temp;
    }
}
