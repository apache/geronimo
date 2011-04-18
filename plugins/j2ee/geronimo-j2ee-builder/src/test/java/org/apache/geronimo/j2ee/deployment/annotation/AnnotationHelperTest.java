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

package org.apache.geronimo.j2ee.deployment.annotation;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.jws.HandlerChain;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContexts;
import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnits;
import javax.xml.bind.JAXBException;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.apache.geronimo.j2ee.deployment.annotation.WebServiceRefAnnotationExample.MyService;
import org.apache.geronimo.testsupport.XmlBeansTestSupport;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * Testcases for each of the various AnnotationHelper class
 */
public class AnnotationHelperTest extends XmlBeansTestSupport {

    private Class[] classes = {EJBAnnotationExample.class, HandlerChainAnnotationExample.class,
        PersistenceContextAnnotationExample.class, PersistenceUnitAnnotationExample.class,
        WebServiceRefAnnotationExample.class, SecurityAnnotationExample.class};

    private ClassFinder classFinder = new TestClassFinder(classes);
    private ClassLoader classLoader = this.getClass().getClassLoader();
    private XmlOptions options = new XmlOptions();

    static class TestClassFinder extends ClassFinder {
        public TestClassFinder(Class [] classes) {
            super(classes);
        }
        public List<Field> findAnnotatedFields(Class<? extends Annotation> arg) {
            List<Field> fields = super.findAnnotatedFields(arg);
            Collections.sort(fields, new FieldComparator());
            return fields;
        }
    }

    static class FieldComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return compare((Field)o1, (Field)o2);
        }
        public int compare(Field o1, Field o2) {
            String field1 = o1.getDeclaringClass().getName() + "/" + o1.getName();
            String field2 = o2.getDeclaringClass().getName() + "/" + o2.getName();
            return field1.compareTo(field2);
        }
    }

    public void testEJBAnnotationHelper() throws Exception {

        //-------------------------------------------------
        // Ensure annotations are discovered correctly
        //-------------------------------------------------
        List<Class<?>> annotatedClasses = classFinder.findAnnotatedClasses(EJBs.class);
        assertNotNull(annotatedClasses);
        assertEquals(1, annotatedClasses.size());
        assertTrue(annotatedClasses.contains(EJBAnnotationExample.class));

        List<Method> annotatedMethods = classFinder.findAnnotatedMethods(EJB.class);
        assertNotNull(annotatedMethods);
        assertEquals(2, annotatedMethods.size());
        assertTrue(annotatedMethods.contains(EJBAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod1", new Class[]{int.class})));
        assertTrue(annotatedMethods.contains(EJBAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod2", new Class[]{String.class})));

        List<Field> annotatedFields = classFinder.findAnnotatedFields(EJB.class);
        assertNotNull(annotatedFields);
        assertEquals(6, annotatedFields.size());
        assertTrue(annotatedFields.contains(EJBAnnotationExample.class.getDeclaredField("annotatedField1")));
        assertTrue(annotatedFields.contains(EJBAnnotationExample.class.getDeclaredField("annotatedField2")));
        assertTrue(annotatedFields.contains(EJBAnnotationExample.class.getDeclaredField("annotatedField3")));
        assertTrue(annotatedFields.contains(EJBAnnotationExample.class.getDeclaredField("annotatedField4")));
        assertTrue(annotatedFields.contains(EJBAnnotationExample.class.getDeclaredField("annotatedField5")));
        assertTrue(annotatedFields.contains(EJBAnnotationExample.class.getDeclaredField("annotatedField6")));

        //-------------------------------------------------
        // Ensure annotations are processed correctly
        //-------------------------------------------------
//        URL srcXML = classLoader.getResource("annotation/empty-web-src.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXML, options);
//        WebAppDocument webAppDoc = (WebAppDocument) xmlObject.changeType(WebAppDocument.type);
        WebApp webApp = load("annotation/empty-web-src.xml", WebApp.class);
        EJBAnnotationHelper.processAnnotations(webApp, classFinder);
        URL expectedXML = classLoader.getResource("annotation/ejb-expected.xml");
        XmlObject expected = XmlObject.Factory.parse(expectedXML);
//        log.debug("[@EJB Source XML] " + '\n' + webApp.toString() + '\n');
//        log.debug("[@EJB Expected XML]" + '\n' + expected.toString() + '\n');
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(webApp, expected, problems);
        assertTrue("Differences: " + problems, ok);
    }


    public void testHandlerChainAnnotationHelper() throws Exception {

        //-------------------------------------------------
        // Ensure annotations are discovered correctly
        //-------------------------------------------------
        List<Class<?>> annotatedClasses = classFinder.findAnnotatedClasses(HandlerChain.class);
        assertNotNull(annotatedClasses);
        assertEquals(1, annotatedClasses.size());
        assertTrue(annotatedClasses.contains(HandlerChainAnnotationExample.class));

        List<Method> annotatedMethods = classFinder.findAnnotatedMethods(HandlerChain.class);
        assertNotNull(annotatedMethods);
        assertEquals(3, annotatedMethods.size());
        assertTrue(annotatedMethods.contains(HandlerChainAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod1", new Class[]{String.class})));
        assertTrue(annotatedMethods.contains(HandlerChainAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod2", new Class[]{int.class})));

        List<Field> annotatedFields = classFinder.findAnnotatedFields(HandlerChain.class);
        assertNotNull(annotatedFields);
        assertEquals(2, annotatedFields.size());
        assertTrue(annotatedFields.contains(HandlerChainAnnotationExample.class.getDeclaredField("annotatedField1")));
        assertTrue(annotatedFields.contains(HandlerChainAnnotationExample.class.getDeclaredField("annotatedField2")));

        //-------------------------------------------------
        // Ensure annotations are processed correctly
        //-------------------------------------------------
//        URL srcXML = classLoader.getResource("annotation/handler-chain-src.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXML, options);
//        WebAppDocument webAppDoc = (WebAppDocument) xmlObject.changeType(WebAppDocument.type);
//        WebAppType webApp = webAppDoc.getWebApp();
//        AnnotatedWebApp annotatedWebApp = new AnnotatedWebApp(webApp);
        WebApp webApp = load("annotation/handler-chain-src.xml", WebApp.class);
        HandlerChainAnnotationHelper.processAnnotations(webApp, classFinder);
        URL expectedXML = classLoader.getResource("annotation/handler-chain-expected.xml");
        XmlObject expected = XmlObject.Factory.parse(expectedXML);
//        log.debug("[@HandlerChain Source XML] " + '\n' + webApp.toString() + '\n');
//        log.debug("[@HandlerChain Expected XML]" + '\n' + expected.toString() + '\n');
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(webApp, expected, problems);
        assertTrue("Differences: " + problems, ok);
    }


    public void testPersistenceContextAnnotationHelper() throws Exception {

        //-------------------------------------------------
        // Ensure annotations are discovered correctly
        //-------------------------------------------------
        List<Class<?>> annotatedClasses = classFinder.findAnnotatedClasses(PersistenceContexts.class);
        assertNotNull(annotatedClasses);
        assertEquals(1, annotatedClasses.size());
        assertTrue(annotatedClasses.contains(PersistenceContextAnnotationExample.class));

        List<Method> annotatedMethods = classFinder.findAnnotatedMethods(PersistenceContext.class);
        assertNotNull(annotatedMethods);
        assertEquals(2, annotatedMethods.size());
        assertTrue(annotatedMethods.contains(PersistenceContextAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod1", new Class[]{String.class})));
        assertTrue(annotatedMethods.contains(PersistenceContextAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod2", new Class[]{String.class})));

        List<Field> annotatedFields = classFinder.findAnnotatedFields(PersistenceContext.class);
        assertNotNull(annotatedFields);
        assertEquals(2, annotatedFields.size());
        assertTrue(annotatedFields.contains(PersistenceContextAnnotationExample.class.getDeclaredField("annotatedField1")));
        assertTrue(annotatedFields.contains(PersistenceContextAnnotationExample.class.getDeclaredField("annotatedField2")));

        //-------------------------------------------------
        // Ensure annotations are processed correctly
        //-------------------------------------------------
//        URL srcXML = classLoader.getResource("annotation/empty-web-src.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXML, options);
//        WebAppDocument webAppDoc = (WebAppDocument) xmlObject.changeType(WebAppDocument.type);
//        WebAppType webApp = webAppDoc.getWebApp();
//        AnnotatedWebApp annotatedWebApp = new AnnotatedWebApp(webApp);
        WebApp webApp = load("annotation/empty-web-src.xml", WebApp.class);
        PersistenceContextAnnotationHelper.processAnnotations(webApp, classFinder);
        URL expectedXML = classLoader.getResource("annotation/persistence-context-expected.xml");
        XmlObject expected = XmlObject.Factory.parse(expectedXML);
//        log.debug("[@PersistenceContext Source XML] " + '\n' + webApp.toString() + '\n');
//        log.debug("[@PersistenceContext Expected XML]" + '\n' + expected.toString() + '\n');
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(webApp, expected, problems);
        assertTrue("Differences: " + problems, ok);
    }


    public void testPersistenceUnitAnnotationHelper() throws Exception {

        List<Class<?>> annotatedClasses = classFinder.findAnnotatedClasses(PersistenceUnits.class);
        assertNotNull(annotatedClasses);
        assertEquals(1, annotatedClasses.size());
        assertTrue(annotatedClasses.contains(PersistenceUnitAnnotationExample.class));

        List<Method> annotatedMethods = classFinder.findAnnotatedMethods(PersistenceUnit.class);
        assertNotNull(annotatedMethods);
        assertEquals(2, annotatedMethods.size());
        assertTrue(annotatedMethods.contains(PersistenceUnitAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod1", new Class[]{int.class})));
        assertTrue(annotatedMethods.contains(PersistenceUnitAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod2", new Class[]{boolean.class})));

        List<Field> annotatedFields = classFinder.findAnnotatedFields(PersistenceUnit.class);
        assertNotNull(annotatedFields);
        assertEquals(2, annotatedFields.size());
        assertTrue(annotatedFields.contains(PersistenceUnitAnnotationExample.class.getDeclaredField("annotatedField1")));
        assertTrue(annotatedFields.contains(PersistenceUnitAnnotationExample.class.getDeclaredField("annotatedField2")));

        //-------------------------------------------------
        // Ensure annotations are processed correctly
        //-------------------------------------------------
//        URL srcXML = classLoader.getResource("annotation/empty-web-src.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXML, options);
//        WebAppDocument webAppDoc = (WebAppDocument) xmlObject.changeType(WebAppDocument.type);
//        WebAppType webApp = webAppDoc.getWebApp();
//        AnnotatedWebApp annotatedWebApp = new AnnotatedWebApp(webApp);
        WebApp webApp = load("annotation/empty-web-src.xml", WebApp.class);
        PersistenceUnitAnnotationHelper.processAnnotations(webApp, classFinder);
        URL expectedXML = classLoader.getResource("annotation/persistence-unit-expected.xml");
        XmlObject expected = XmlObject.Factory.parse(expectedXML);
//        log.debug("[@PersistenceUnit Source XML] " + '\n' + webApp.toString() + '\n');
//        log.debug("[@PersistenceUnit Expected XML]" + '\n' + expected.toString() + '\n');
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(webApp, expected, problems);
        assertTrue("Differences: " + problems, ok);
    }


    public void testWebServiceRefAnnotationHelper() throws Exception {

        //-------------------------------------------------
        // Ensure annotations are discovered correctly
        //-------------------------------------------------
        List<Class<?>> annotatedClasses = classFinder.findAnnotatedClasses(WebServiceRefs.class);
        assertNotNull(annotatedClasses);
        assertEquals(1, annotatedClasses.size());
        assertTrue(annotatedClasses.contains(WebServiceRefAnnotationExample.class));

        List<Method> annotatedMethods = classFinder.findAnnotatedMethods(WebServiceRef.class);
        assertNotNull(annotatedMethods);
        assertEquals(5, annotatedMethods.size());
        assertTrue(annotatedMethods.contains(WebServiceRefAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod1", new Class[]{MyService.class})));
        assertTrue(annotatedMethods.contains(WebServiceRefAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod2", new Class[]{Service.class})));
        assertTrue(annotatedMethods.contains(HandlerChainAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod1", new Class[]{String.class})));
        assertTrue(annotatedMethods.contains(HandlerChainAnnotationExample.class.getDeclaredMethod("setAnnotatedMethod2", new Class[]{int.class})));

        List<Field> annotatedFields = classFinder.findAnnotatedFields(WebServiceRef.class);
        assertNotNull(annotatedFields);
        assertEquals(4, annotatedFields.size());
        assertTrue(annotatedFields.contains(WebServiceRefAnnotationExample.class.getDeclaredField("annotatedField1")));
        assertTrue(annotatedFields.contains(WebServiceRefAnnotationExample.class.getDeclaredField("annotatedField2")));
        assertTrue(annotatedFields.contains(HandlerChainAnnotationExample.class.getDeclaredField("annotatedField1")));
        assertTrue(annotatedFields.contains(HandlerChainAnnotationExample.class.getDeclaredField("annotatedField2")));

        //-------------------------------------------------
        // Ensure annotations are processed correctly
        //-------------------------------------------------
//        URL srcXML = classLoader.getResource("annotation/empty-web-src.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXML, options);
//        WebAppDocument webAppDoc = (WebAppDocument) xmlObject.changeType(WebAppDocument.type);
//        WebAppType webApp = webAppDoc.getWebApp();
//        AnnotatedWebApp annotatedWebApp = new AnnotatedWebApp(webApp);
        WebApp webApp = load("annotation/empty-web-src.xml", WebApp.class);
        WebServiceRefAnnotationHelper.processAnnotations(webApp, classFinder);
        URL expectedXML = classLoader.getResource("annotation/webservice-ref-expected.xml");
        XmlObject expected = XmlObject.Factory.parse(expectedXML);
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(webApp, expected, problems);
        assertTrue("Differences: " + problems, ok);
    }


    public void testSecurityAnnotationHelper() throws Exception {

        //-------------------------------------------------
        // Ensure annotations are discovered correctly
        //-------------------------------------------------
        List<Class<?>> annotatedClasses = classFinder.findAnnotatedClasses(DeclareRoles.class);
        assertNotNull(annotatedClasses);
        assertEquals(1, annotatedClasses.size());
        assertTrue(annotatedClasses.contains(SecurityAnnotationExample.class));

        annotatedClasses.clear();
        annotatedClasses = classFinder.findAnnotatedClasses(RunAs.class);
        assertNotNull(annotatedClasses);
        assertEquals(1, annotatedClasses.size());
        assertTrue(annotatedClasses.contains(SecurityAnnotationExample.class));

        //-------------------------------------------------
        // Ensure annotations are processed correctly
        //-------------------------------------------------
//        URL srcXML = classLoader.getResource("annotation/empty-web-src.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXML, options);
//        WebAppDocument webAppDoc = (WebAppDocument) xmlObject.changeType(WebAppDocument.type);
//        WebAppType webApp = webAppDoc.getWebApp();
        WebApp webApp = load("annotation/empty-web-src.xml", WebApp.class);
        SecurityAnnotationHelper.processAnnotations(webApp, classFinder);
        URL expectedXML = classLoader.getResource("annotation/security-expected.xml");
        XmlObject expected = XmlObject.Factory.parse(expectedXML);
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(webApp, expected, problems);
        assertTrue("Differences: " + problems, ok);

//        srcXML = classLoader.getResource("annotation/security-src.xml");
//        xmlObject = XmlObject.Factory.parse(srcXML, options);
//        webAppDoc = (WebAppDocument) xmlObject.changeType(WebAppDocument.type);
//        webApp = webAppDoc.getWebApp();
        webApp = load("annotation/security-src.xml", WebApp.class);
        SecurityAnnotationHelper.processAnnotations(webApp, classFinder);
        expectedXML = classLoader.getResource("annotation/security-expected-1.xml");
        expected = XmlObject.Factory.parse(expectedXML);
//        log.debug("[Security Source XML] " + '\n' + webApp.toString() + '\n');
//        log.debug("[Security Expected XML]" + '\n' + expected.toString() + '\n');
        problems = new ArrayList();
        ok = compareXmlObjects(webApp, expected, problems);
        assertTrue("Differences: " + problems, ok);
    }

    private boolean compareXmlObjects(WebApp webApp, XmlObject expected, List problems) throws JAXBException, XmlException {
        String xml = JaxbJavaee.marshal(WebApp.class, webApp);
        log.debug("[Source XML] " + '\n' + xml + '\n');
        log.debug("[Expected XML]" + '\n' + expected.toString() + '\n');
        XmlObject actual = XmlObject.Factory.parse(xml);
        return compareXmlObjects(actual, expected, problems);
    }

    private <T> T load(String url, Class<T> clazz) throws Exception {
        URL srcXml = classLoader.getResource(url);
        InputStream in = srcXml.openStream();
        try {
            return (T) JaxbJavaee.unmarshalJavaee(clazz, in);
        } finally {
            in.close();
        }
    }

}
