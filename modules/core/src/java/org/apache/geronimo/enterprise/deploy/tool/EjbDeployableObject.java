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

package org.apache.geronimo.enterprise.deploy.tool;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;
import org.dom4j.io.SAXReader;
import org.dom4j.DocumentException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A DeployableObject implementation for EJB JARs.  This knows how to load and
 * validate the deployment descriptors for EJB (currently v2.1 only).
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:43 $
 */
public class EjbDeployableObject extends AbstractDeployableObject {
    private final static Log log = LogFactory.getLog(EjbDeployableObject.class);
    private final static String EJB_JAR_DD = "META-INF/ejb-jar.xml";

    public EjbDeployableObject(JarFile jar, ClassLoader loader) {
        super(jar, ModuleType.EJB, createEjbJarRoot(jar), loader);
    }

    public DDBeanRoot getDDBeanRoot(String filename) throws FileNotFoundException, DDBeanCreateException {
        if(filename.equals(EJB_JAR_DD)) {
            return super.getDDBeanRoot();
        } else {
            return null; //todo: Web Services DDs
        }
    }

    private static DDBeanRootImpl createEjbJarRoot(JarFile jar) {
        DDBeanRootImpl beanRoot = createDDBeanRoot(jar, EJB_JAR_DD);
        Element root = beanRoot.getDocument().getRootElement();
        if(!root.getName().equals("ejb-jar") || !root.attribute("version").getValue().equals("2.1")) {
            log.error("Not an EJB 2.1 deployment descriptor");
            return null;
        }
        return beanRoot;
    }

    private static DDBeanRootImpl createDDBeanRoot(JarFile jar, String fileName) {
        try {
            InputStream in = jar.getInputStream(jar.getEntry(fileName));
            SAXReader reader = new SAXReader();
            Document document = reader.read(in);
            return new DDBeanRootImpl(document);
        } catch(IOException e) {
            log.error("Unable to locate DD "+fileName+" in EJB JAR", e);
            return null;
        } catch(DocumentException e) {
            log.error("Unable to parse DD "+fileName+" in EJB JAR", e);
            return null;
        }
    }

}
