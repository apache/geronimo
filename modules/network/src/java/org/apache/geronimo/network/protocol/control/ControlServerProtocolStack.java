/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.network.protocol.control;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.protocol.AcceptableProtocolStack;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;


/**
 * @version $Revision: 1.3 $ $Date: 2004/03/17 03:12:00 $
 */
public class ControlServerProtocolStack extends AcceptableProtocolStack implements ControlServerListener, BootstrapChef {

    final static private Log log = LogFactory.getLog(ControlServerProtocolStack.class);

    protected ControlServerProtocolWaiter waiter;

    public Object push(Object object) {
        if (object instanceof ControlServerProtocol) {
            ((ControlServerProtocol) object).setControlServerListener(this);
            ((ControlServerProtocol) object).setBootstrapChef(this);
        } else if (object instanceof ControlServerProtocolWaiter) {
            waiter = (ControlServerProtocolWaiter) object;
        }
        return super.push(object);
    }

    public Object pop() {
        Protocol result = (Protocol) super.pop();

        if (result instanceof ControlServerProtocol) {
            ((ControlServerProtocol) result).setControlServerListener(null);
        } else if (result instanceof ControlServerProtocolWaiter) {
            waiter = null;
        }
        return result;
    }

    public void setup() throws ProtocolException {
        if (waiter == null) throw new IllegalStateException("Waiter not added to stack");

        super.setup();
    }

    public void shutdown() {
        log.trace("Shutdown");
        try {
            drain();
        } catch (ProtocolException e) {
        }
    }

    public Collection createMenu(ControlContext context) {
        return waiter.createMenu(context);
    }
}
