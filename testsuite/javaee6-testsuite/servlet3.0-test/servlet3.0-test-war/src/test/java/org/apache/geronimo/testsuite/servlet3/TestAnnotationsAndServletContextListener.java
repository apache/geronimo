/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.testsuite.servlet3;

import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;

public class TestAnnotationsAndServletContextListener extends
        SeleniumTestSupport {

    @Test
    public void testNewServletAnnotationsNullClass() throws Exception {
        // System.out.println("Start testing!");
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);
        selenium.click("link=Test Annotations and ServletContextListener.");
        waitForPageLoad();
        selenium.click("//input[@type='submit']");
        waitForPageLoad();
        assertEquals("Class Viewer Servlet", selenium.getTitle());
        assertEquals("Message:", selenium.getText("xpath=/html/body/font[1]/b"));
        assertEquals(
                "You have input nothing.We set it to the default class :java.lang.Integer.\nThe class java.lang.Integer is valid.The detail information is:",
                selenium.getText("xpath=/html/body/font[2]/b"));
        assertTrue(selenium.isTextPresent("ClassName:"));
        assertTrue(selenium.isTextPresent("java.lang.Integer"));
        assertTrue(selenium.isTextPresent("Extends:"));
        assertTrue(selenium.isTextPresent("class java.lang.Number"));
        assertTrue(selenium.isTextPresent("Implements:"));
        assertTrue(selenium.isTextPresent("interface java.lang.Comparable"));
        assertTrue(selenium.isTextPresent("Methods:"));
        assertTrue(selenium
                .isTextPresent("public static int java.lang.Integer.numberOfLeadingZeros(int)"));
        assertTrue(selenium
                .isTextPresent("public static int java.lang.Integer.numberOfTrailingZeros(int)"));
        assertTrue(selenium
                .isTextPresent("public static int java.lang.Integer.bitCount(int)"));
        assertTrue(selenium
                .isTextPresent("public boolean java.lang.Integer.equals(java.lang.Object)"));
        assertTrue(selenium
                .isTextPresent("public static java.lang.String java.lang.Integer.toString(int,int)"));
        assertTrue(selenium
                .isTextPresent("public static java.lang.String java.lang.Integer.toString(int)"));
        assertTrue(selenium
                .isTextPresent("public java.lang.String java.lang.Integer.toString()"));
        assertTrue(selenium
                .isTextPresent("public int java.lang.Integer.hashCode()"));
        assertTrue(selenium
                .isTextPresent("public static int java.lang.Integer.reverseBytes(int)"));
        assertTrue(selenium
                .isTextPresent("public int java.lang.Integer.compareTo(java.lang.Integer)"));
        assertTrue(selenium
                .isTextPresent("public int java.lang.Integer.compareTo(java.lang.Object)"));
        assertTrue(selenium
                .isTextPresent("public static java.lang.String java.lang.Integer.toHexString(int)"));
        assertTrue(selenium
                .isTextPresent("public static java.lang.Integer java.lang.Integer.decode(java.lang.String) throws java.lang.NumberFormatException"));
        assertTrue(selenium
                .isTextPresent("static void java.lang.Integer.getChars(int,int,char[])"));
        assertTrue(selenium
                .isTextPresent("public static java.lang.Integer java.lang.Integer.valueOf(java.lang.String,int) throws java.lang.NumberFormatException"));
        assertTrue(selenium
                .isTextPresent("public static java.lang.Integer java.lang.Integer.valueOf(java.lang.String) throws java.lang.NumberFormatException"));
    }

    @Test
    public void testNewServletAnnotationsValidClass() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);

        selenium.click("link=Test Annotations and ServletContextListener.");
        waitForPageLoad();
        selenium.type("class", "java.util.ArrayList");
        selenium.click("//input[@type='submit']");
        waitForPageLoad();
        assertEquals("Class Viewer Servlet", selenium.getTitle());
        assertEquals("Message:", selenium.getText("xpath=/html/body/font[1]/b"));
        assertEquals(
                "The class java.util.ArrayList is valid.The detail information is:",
                selenium.getText("xpath=/html/body/font[2]/b"));
        assertEquals("ClassName:",
                selenium.getText("xpath=/html/body/font[3]/b"));
        assertTrue(selenium.isTextPresent("java.util.ArrayList"));
        assertTrue(selenium.isTextPresent("Extends:"));
        assertTrue(selenium.isTextPresent("class java.util.AbstractList"));
        assertTrue(selenium.isTextPresent("Implements:"));
        assertTrue(selenium.isTextPresent("interface java.util.List"));
        assertTrue(selenium.isTextPresent("interface java.util.RandomAccess"));
        assertTrue(selenium.isTextPresent("interface java.lang.Cloneable"));
        assertTrue(selenium.isTextPresent("interface java.io.Serializable"));
        assertTrue(selenium.isTextPresent("Methods:"));
        assertTrue(selenium
                .isTextPresent("public boolean java.util.ArrayList.add(java.lang.Object)"));
        assertTrue(selenium
                .isTextPresent("public void java.util.ArrayList.add(int,java.lang.Object)"));
        assertTrue(selenium
                .isTextPresent("public java.lang.Object java.util.ArrayList.get(int)"));
        assertTrue(selenium
                .isTextPresent("public java.lang.Object java.util.ArrayList.clone()"));
        assertTrue(selenium
                .isTextPresent("public int java.util.ArrayList.indexOf(java.lang.Object)"));
        assertTrue(selenium
                .isTextPresent("public void java.util.ArrayList.clear()"));
        assertTrue(selenium
                .isTextPresent("public boolean java.util.ArrayList.contains(java.lang.Object)"));
        assertTrue(selenium
                .isTextPresent("public boolean java.util.ArrayList.isEmpty()"));
        assertTrue(selenium
                .isTextPresent("public int java.util.ArrayList.lastIndexOf(java.lang.Object)"));
        assertTrue(selenium
                .isTextPresent("public boolean java.util.ArrayList.addAll(int,java.util.Collection)"));
        assertTrue(selenium
                .isTextPresent("public boolean java.util.ArrayList.addAll(java.util.Collection)"));
        assertTrue(selenium
                .isTextPresent("public int java.util.ArrayList.size()"));
        assertTrue(selenium
                .isTextPresent("public java.lang.Object[] java.util.ArrayList.toArray(java.lang.Object[])"));
        assertTrue(selenium
                .isTextPresent("public java.lang.Object[] java.util.ArrayList.toArray()"));
        assertTrue(selenium
                .isTextPresent("public boolean java.util.ArrayList.remove(java.lang.Object)"));
        assertTrue(selenium
                .isTextPresent("public java.lang.Object java.util.ArrayList.remove(int)"));
        assertTrue(selenium
                .isTextPresent("private void java.util.ArrayList.writeObject(java.io.ObjectOutputStream) throws java.io.IOException"));
        assertTrue(selenium
                .isTextPresent("private void java.util.ArrayList.readObject(java.io.ObjectInputStream) throws java.io.IOException,java.lang.ClassNotFoundException"));
        assertTrue(selenium
                .isTextPresent("public java.lang.Object java.util.ArrayList.set(int,java.lang.Object)"));
        assertTrue(selenium
                .isTextPresent("public void java.util.ArrayList.ensureCapacity(int)"));
        assertTrue(selenium
                .isTextPresent("protected void java.util.ArrayList.removeRange(int,int)"));
        assertTrue(selenium
                .isTextPresent("public void java.util.ArrayList.trimToSize()"));
    }

    @Test
    public void testNewServletAnnotationsInvalidClass() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);

        selenium.click("link=Test Annotations and ServletContextListener.");
        waitForPageLoad();
        selenium.type("class", "Integer");
        selenium.click("//input[@type='submit']");
        waitForPageLoad();
        assertEquals("Class Viewer Servlet", selenium.getTitle());
        assertEquals("Message:", selenium.getText("xpath=/html/body/font[1]/b"));
        assertEquals(
                "You have input an invalid class.So we set it to default class:java.lang.String",
                selenium.getText("xpath=/html/body/font[2]/b"));
        assertEquals("ClassName:",
                selenium.getText("xpath=/html/body/font[3]/b"));
        assertTrue(selenium.isTextPresent("java.lang.String"));
        assertTrue(selenium.isTextPresent("Extends:"));
        assertTrue(selenium.isTextPresent("class java.lang.Object"));
        assertTrue(selenium.isTextPresent("Implements:"));
        assertTrue(selenium
                .isTextPresent("interface java.io.Serializable interface java.lang.Comparable interface java.lang.CharSequence"));
        assertTrue(selenium.isTextPresent("Methods:"));

    }

}
