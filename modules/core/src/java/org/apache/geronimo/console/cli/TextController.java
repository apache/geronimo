/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
 * @version $Rev$ $Date$
 */
public abstract class TextController {
    protected final DeploymentContext context;

    public TextController(DeploymentContext context) {
        this.context = context;
    }

    protected boolean ensureConnected() {
        if( context.connected ) {
            return true;
        }
        context.out.println("Not connected.");
        return false;
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
        if ( null == s ) {
            return "";
        }
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
