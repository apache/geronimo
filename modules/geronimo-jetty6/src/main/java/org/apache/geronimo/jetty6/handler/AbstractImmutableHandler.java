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


package org.apache.geronimo.jetty6.handler;

import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractImmutableHandler extends AbstractHandler {
    protected final AbstractHandler next;

    protected AbstractImmutableHandler(AbstractHandler next) {
        this.next = next;
    }

    protected void doStart() throws Exception {
        next.start();
    }

    protected void doStop() throws Exception {
        next.stop();
    }

    public void setServer(Server server) {
        super.setServer(server);
        next.setServer(server);
    }

    public void lifecycleCommand(LifecycleCommand lifecycleCommand) throws Exception {
        if (next instanceof AbstractImmutableHandler) {
            ((AbstractImmutableHandler)next).lifecycleCommand(lifecycleCommand);
        } else {
            lifecycleCommand.lifecycleMethod();
        }
    }


}
