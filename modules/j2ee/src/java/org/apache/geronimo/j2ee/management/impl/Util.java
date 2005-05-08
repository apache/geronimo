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

package org.apache.geronimo.j2ee.management.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
public class Util {

    public static String[] getObjectNames(Kernel kernel, J2eeContext context, String[] j2eeTypes) throws MalformedObjectNameException {
        List objectNames = new LinkedList();
        for (int i = 0; i < j2eeTypes.length; i++) {
            String j2eeType = j2eeTypes[i];
            ObjectName query = NameFactory.getComponentInModuleQuery(null, null, null, null, null, j2eeType,  context);
            objectNames.addAll(kernel.listGBeans(query));
        }
        String[] names = new String[objectNames.size()];
        Iterator iterator = objectNames.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            names[i] = iterator.next().toString();
        }
        return names;
    }

    public static String[] getObjectNames(Kernel kernel, Object parentName, String[] j2eeTypes) {
        List objectNames = new LinkedList();
        for (int i = 0; i < j2eeTypes.length; i++) {
            String j2eeType = j2eeTypes[i];
            String name = parentName + "j2eeType=" + j2eeType + ",*";
            try {
                objectNames.addAll(kernel.listGBeans(new ObjectName(name)));
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException("Malformed ObjectName: " + name);
            }
        }
        String[] names = new String[objectNames.size()];
        Iterator iterator = objectNames.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            names[i] = iterator.next().toString();
        }
        return names;
    }
}
