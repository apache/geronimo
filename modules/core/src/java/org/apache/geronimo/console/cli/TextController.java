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
package org.apache.geronimo.console.cli;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;

/**
 * Base class for all controllers for text-based screens.  Generally a subclass
 * will paint information to the screen and then accept user input, possibly
 * repeating or invoking other controllers before returning to the caller.
 *
 * @version $Revision: 1.2 $ $Date: 2003/10/20 02:46:35 $
 */
public abstract class TextController {
    protected final DeploymentContext context;

    public TextController(DeploymentContext context) {
        this.context = context;
    }

    protected void newScreen(String title) {
        context.out.println("\n\n------ "+title+" ------");
    }

    protected void print(String s) {
        context.out.print(s);
    }

    protected void println(String s) {
        context.out.println(s);
    }

    protected String truncate(String s, int size) {
        if(s.length() <= size) {
            return s;
        }
        if(size < 3) {
            return "";
        }
        return s.substring(0, size-3)+"...";
    }

    public abstract void execute();

    // Some common utility methods

    protected Target[] available(Target[] all, Target[] selected) {
        List list = new ArrayList();
        for(int i=0; i<all.length; i++) {
            boolean found = false;
            for(int j = 0; j < selected.length; j++) {
                if(all[i].getName().equals(selected[j].getName())) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                list.add(all[i]);
            }
        }
        return (Target[])list.toArray(new Target[list.size()]);
    }

    protected TargetModuleID[] available(TargetModuleID[] all, TargetModuleID[] selected) {
        List list = new ArrayList();
        for(int i=0; i<all.length; i++) {
            boolean found = false;
            for(int j = 0; j < selected.length; j++) {
                if(equals(all[i], selected[j])) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                list.add(all[i]);
            }
        }
        return (TargetModuleID[])list.toArray(new TargetModuleID[list.size()]);
    }

    private static boolean equals(TargetModuleID one, TargetModuleID two) {
        if(one == null) {
            return two == null;
        } else if(two == null) {
            return false;
        }
        return one.getTarget().getName().equals(two.getTarget().getName()) &&
                one.getModuleID().equals(two.getModuleID()) &&
                equals(one.getParentTargetModuleID(), two.getParentTargetModuleID());
    }


    protected boolean confirmModuleAction(String action) throws IOException {
        if(context.modules.length == 0) {
            return false;
        }
        println("");
        String choice;
        while(true) {
            print(action+" "+context.modules.length+" selected module(s)? ");
            context.out.flush();
            choice = context.in.readLine().trim().toLowerCase();
            if(choice.equals("n") || choice.equals("y")) {
                return choice.equals("y");
            }
        }
    }
    /**
     * Marches recursively through the DConfigBean tree to initialize
     * DConfigBeans for all the interesting DDBeans.  Once this is done, and
     * DDBean changes need to be relayed to the DConfigBeans that listn on them.
     */
    protected void initializeDConfigBean(DConfigBean dcb) throws ConfigurationException {
        String[] xpaths = dcb.getXpaths();
        for(int i=0; i<xpaths.length; i++) {
            DDBean[] ddbs = dcb.getDDBean().getChildBean(xpaths[i]);
            for(int j = 0; j < ddbs.length; j++) {
                initializeDConfigBean(dcb.getDConfigBean(ddbs[j]));
            }
        }
    }
}
