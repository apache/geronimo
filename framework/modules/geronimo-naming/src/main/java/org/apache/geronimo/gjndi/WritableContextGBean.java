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
package org.apache.geronimo.gjndi;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.xbean.naming.context.ContextAccess;
import org.apache.xbean.naming.context.WritableContext;

import javax.naming.NamingException;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = "Context")
public class WritableContextGBean extends WritableContext {

    public WritableContextGBean(@ParamAttribute(name="nameInNamespace")String nameInNamespace) throws NamingException {
        super(nameInNamespace, Collections.<String, Object>emptyMap(), ContextAccess.MODIFIABLE, false);
    }

}
