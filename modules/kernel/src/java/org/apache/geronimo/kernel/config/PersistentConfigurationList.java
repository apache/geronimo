/**
 *
 *  Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.kernel.config;

import java.io.IOException;
import java.util.List;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/04 17:27:00 $
 */
public interface PersistentConfigurationList {
    public static final ObjectName OBJECT_NAME = JMXUtil.getObjectName("geronimo.boot:role=PersistentConfigurationList");

    void save() throws IOException;

    List restore() throws IOException;
}
