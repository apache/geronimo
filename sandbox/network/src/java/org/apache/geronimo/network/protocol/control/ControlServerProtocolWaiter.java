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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.geronimo.network.protocol.ProtocolStack;
import org.apache.geronimo.network.protocol.control.commands.CreateInstanceMenuItem;
import org.apache.geronimo.network.protocol.control.commands.MenuItem;


/**
 * @version $Rev$ $Date$
 */
public class ControlServerProtocolWaiter extends ProtocolStack implements BootstrapChef {

    public Collection createMenu(ControlContext context) {
        ArrayList list = new ArrayList();
        for (int i = 0; i < size(); i++) {
            Object object = get(i);
            if (!(object instanceof BootstrapCook)) return null;

            Collection menu = ((BootstrapCook) object).cook(context);
            Iterator iter = menu.iterator();
            while (iter.hasNext()) {
                MenuItem item = (MenuItem) iter.next();
                if (item instanceof CreateInstanceMenuItem) {
                    list.add(item);
                }
            }

            iter = menu.iterator();
            while (iter.hasNext()) {
                MenuItem item = (MenuItem) iter.next();
                if (!(item instanceof CreateInstanceMenuItem)) {
                    list.add(item);
                }
            }
        }
        return list;
    }
}
