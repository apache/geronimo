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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.activation;

import java.io.InputStream;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ActivationDataFlavorTest extends TestCase {
    public void testMimeTypeConstructorWithoutClass() {
        ActivationDataFlavor adf = new ActivationDataFlavor("application/*", null);
        assertEquals("application/*", adf.getMimeType());
        assertEquals(InputStream.class, adf.getRepresentationClass());
    }

    public void testMimeTypeConstructorWithClass() {
        ActivationDataFlavor adf = new ActivationDataFlavor("application/x-java-serialized-object; class=java.lang.Object", null);
        assertEquals("application/x-java-serialized-object; class=java.lang.Object", adf.getMimeType());
        assertEquals(InputStream.class, adf.getRepresentationClass());
    }

    public void testHumanName() {
        ActivationDataFlavor adf = new ActivationDataFlavor("text/html", "Human Name");
        assertEquals("Human Name", adf.getHumanPresentableName());
        adf.setHumanPresentableName("Name 2");
        assertEquals("Name 2", adf.getHumanPresentableName());
        adf = new ActivationDataFlavor("text/html", null);
        assertNull(adf.getHumanPresentableName());
    }
}
