/**
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
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
package org.apache.geronimo.ui.commands;

import org.apache.geronimo.core.internal.GeronimoServer;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;

/**
 * 
 * 
 */
public class SetUsernameCommand extends ServerCommand {

    protected String name;

    protected String oldName;

    GeronimoServer gs;

    /**
     * @param server
     * @param name
     */
    public SetUsernameCommand(IServerWorkingCopy server, String name) {
        super(server, name);
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.wst.server.ui.internal.command.ServerCommand#execute()
     */
    public void execute() {
        gs = (GeronimoServer) server.getAdapter(GeronimoServer.class);
        if (gs == null) {
            gs = (GeronimoServer) server.loadAdapter(GeronimoServer.class,
                    new NullProgressMonitor());
        }
        oldName = gs.getAdminID();
        gs.setAdminID(name);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.wst.server.ui.internal.command.ServerCommand#undo()
     */
    public void undo() {
        if (gs != null) {
            gs.setAdminID(oldName);
        }
    }

}
