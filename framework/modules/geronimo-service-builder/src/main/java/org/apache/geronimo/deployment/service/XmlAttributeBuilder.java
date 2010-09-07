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
package org.apache.geronimo.deployment.service;

import org.apache.xmlbeans.XmlObject;
import org.apache.geronimo.common.DeploymentException;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public interface XmlAttributeBuilder {

    String getNamespace();

    Object getValue(XmlObject xmlObject, XmlObject enclosing, String type, Bundle bundle) throws DeploymentException;

}
