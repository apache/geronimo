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

package org.apache.geronimo.connector.deployment;

import java.net.URL;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;

import org.apache.geronimo.deployment.tools.DDBeanRootImpl;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class MockRARDeployable implements DeployableObject {

        private DDBeanRoot root;

        public MockRARDeployable(URL dd) throws DDBeanCreateException {
            root =  new DDBeanRootImpl(this, dd);
        }

        public ModuleType getType() {
            return ModuleType.RAR;
        }

        public DDBeanRoot getDDBeanRoot() {
            return root;
        }

        public DDBean[] getChildBean(String xpath) {
            return root.getChildBean(xpath);
        }

        public String[] getText(String xpath) {
            return root.getText(xpath);
        }

        public Class getClassFromScope(String className) {
            return null;
        }

        public String getModuleDTDVersion() {
            return null;
        }

        public DDBeanRoot getDDBeanRoot(String filename) throws FileNotFoundException, DDBeanCreateException {
            return null;
        }

        public Enumeration entries() {
            return null;
        }

        public InputStream getEntry(String name) {
            return null;
        }
}
