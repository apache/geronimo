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

package org.apache.geronimo.validator.ejb;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;

import javax.enterprise.deploy.shared.ModuleType;

import org.apache.geronimo.validator.AbstractValidator;
import org.apache.geronimo.validator.Validator;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.xmlbeans.XmlObject;

/**
 * The validator class for validating an EJB JAR.  Right now just does enough
 * to prove that this whole thing works.
 *
 * @version $Revision: 1.5 $ $Date: 2004/02/25 09:57:29 $
 */
public class EjbValidator extends AbstractValidator {
    public Class[] getTestClasses() {
        return new Class[]{
            SessionBeanTests.class,
        };
    }

    /**
     * To try me, pass an EJB JAR file name as the only argument.
     */
    public static void main(String[] args) {
        try {
            ClassLoader loader = new URLClassLoader(new URL[]{new File(args[0]).toURL()});
            InputStream in = loader.getResourceAsStream("META-INF/ejb-jar.xml");

            EjbJarDocument jar = EjbJarDocument.Factory.parse(in);
            Validator v =new EjbValidator();
            v.initialize(new PrintWriter(new OutputStreamWriter(System.out), true), args[0], loader, ModuleType.EJB, new XmlObject[]{jar}, null);
            System.out.println("Validation Result: "+v.validate());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
