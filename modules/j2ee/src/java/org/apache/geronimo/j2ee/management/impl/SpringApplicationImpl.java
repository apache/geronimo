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

import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

// I know that this should not be here (in the j2ee package) but,
// until someone who knows what they are doing has time to have a look
// at it, this seems the path of least resistance...

/**
 * @version $Rev: 126313 $ $Date: 2005-01-24 21:03:52 +0000 (Mon, 24 Jan 2005) $
 */
public class SpringApplicationImpl {

    public SpringApplicationImpl() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(SpringApplicationImpl.class, "SpringApplication");

	//        infoFactory.addAttribute("kernel", Kernel.class, false);
	//        infoFactory.addAttribute("objectName", String.class, false);

        infoFactory.setConstructor(new String[]{});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
