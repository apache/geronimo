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

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.module.EJBJARInfo;
import org.apache.geronimo.console.cli.module.WARInfo;

/**
 * Selects a J2EE module to deploy/edit/etc.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/19 01:56:14 $
 */
public class SelectModule extends TextController {
    private static final Log log = LogFactory.getLog(SelectModule.class);

    public SelectModule(DeploymentContext context) {
        super(context);
    }

    public void execute() { //todo: handle more than JAR/WAR
        context.out.println("\nCurrent directory is "+context.saveDir);
        context.out.println("Select an EJB JAR or WAR file to load.");
        String choice;
        File file;
        while(true) {
            context.out.print("File Name: ");
            context.out.flush();
            try {
                choice = context.in.readLine().trim();
            } catch(IOException e) {
                log.error("Unable to read user input", e);
                return;
            }
            file = new File(context.saveDir, choice);
            if(!file.canRead() || file.isDirectory()) {
                context.out.println("ERROR: cannot read from this file.  Please try again.");
                continue;
            }
            context.saveDir = file.getParentFile();
            break;
        }

        if(file.getName().endsWith(".jar")) {
            context.moduleInfo = new EJBJARInfo(context);
        } else if(file.getName().endsWith(".war")) {
            context.moduleInfo = new WARInfo(context);
        } else {
            context.out.println("ERROR: Expecting file name to end in .jar or .war");
        }
        try {
            context.moduleInfo.file = file;
            context.moduleInfo.jarFile = new JarFile(file);
        } catch(IOException e) {
            context.out.println("ERROR: "+file+" is not a valid JAR file!");
            context.moduleInfo = null;
            return;
        }
        if(!context.moduleInfo.initialize()) {
            context.moduleInfo = null;
            return;
        }
    }
}
