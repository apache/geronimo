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
package org.apache.geronimo.system.configuration;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev$ $Date$
 */
public class ServerOverrideTest extends TestCase {
    private static final Log log = LogFactory.getLog(ServerOverrideTest.class);
    
    public void testBasics() throws Exception {
        GBeanOverride pizza = new GBeanOverride("Pizza", true);
        assertTrue(pizza.isLoad());

        pizza.setLoad(false);
        assertFalse(pizza.isLoad());

        pizza.setAttribute("cheese", "mozzarella");
        assertEquals("mozzarella", pizza.getAttribute("cheese"));

        AbstractNameQuery pizzaOvenQuery = getAbstractNameQuery(":name=PizzaOven");
        ReferencePatterns pizzaOvenPatterns = new ReferencePatterns(Collections.singleton(pizzaOvenQuery));
        pizza.setReferencePatterns("oven", pizzaOvenPatterns);
        assertEquals(pizzaOvenPatterns, pizza.getReferencePatterns("oven"));

        AbstractNameQuery toasterOvenQuery = getAbstractNameQuery(":name=ToasterOven,*");
        AbstractNameQuery[] queries = new AbstractNameQuery[]{pizzaOvenQuery, toasterOvenQuery};
        ReferencePatterns ovenPatterns = getReferencePatterns(queries);
        pizza.setReferencePatterns("oven", ovenPatterns);
        assertEquals(ovenPatterns, pizza.getReferencePatterns("oven"));

        ConfigurationOverride dinnerMenu = new ConfigurationOverride(new Artifact("test","Dinner Menu","1.0","car"), true);
        assertTrue(dinnerMenu.isLoad());

        dinnerMenu.setLoad(false);
        assertFalse(dinnerMenu.isLoad());

        dinnerMenu.addGBean(pizza);
        assertSame(pizza, dinnerMenu.getGBean("Pizza"));

        ServerOverride restaurant = new ServerOverride();
        restaurant.addConfiguration(dinnerMenu);
        assertSame(dinnerMenu, restaurant.getConfiguration(new Artifact("test","Dinner Menu","1.0","car")));
    }

    private ReferencePatterns getReferencePatterns(AbstractNameQuery[] queries) {
        Set querySet = new LinkedHashSet(Arrays.asList(queries));
        return new ReferencePatterns(querySet);
    }

    private AbstractNameQuery getAbstractNameQuery(String pizzaOvenString) throws MalformedObjectNameException {
        ObjectName pizzaOvenPattern = new ObjectName(pizzaOvenString);
        return new AbstractNameQuery(null, pizzaOvenPattern.getKeyPropertyList(), Collections.EMPTY_SET);
    }

    public void testGBeanXml() throws Exception {
        GBeanOverride pizza = new GBeanOverride("Pizza", true);
        assertCopyIdentical(pizza);

        pizza.setLoad(false);
        assertCopyIdentical(pizza);

        pizza.setAttribute("cheese", "mozzarella");
        assertCopyIdentical(pizza);

        pizza.setAttribute("size", "x-large");
        assertCopyIdentical(pizza);

        AbstractNameQuery pizzaOvenQuery = getAbstractNameQuery(":name=PizzaOven");
        ReferencePatterns pizzaOvenPatterns = new ReferencePatterns(Collections.singleton(pizzaOvenQuery));
        pizza.setReferencePatterns("oven", pizzaOvenPatterns);
        assertCopyIdentical(pizza);

        AbstractNameQuery toasterOvenQuery = getAbstractNameQuery(":name=ToasterOven,*");
        AbstractNameQuery[] queries = new AbstractNameQuery[]{pizzaOvenQuery, toasterOvenQuery};
        ReferencePatterns ovenPatterns = getReferencePatterns(queries);
        pizza.setReferencePatterns("oven", ovenPatterns);
        assertCopyIdentical(pizza);
    }

    public void testConfigurationXml() throws Exception {
        ConfigurationOverride dinnerMenu = new ConfigurationOverride(new Artifact("test","Dinner Menu","1.0","car"), true);
        assertCopyIdentical(dinnerMenu);

        dinnerMenu.setLoad(false);
        assertCopyIdentical(dinnerMenu);

        GBeanOverride pizza = new GBeanOverride("Pizza", false);
        pizza.setAttribute("cheese", "mozzarella");
        pizza.setAttribute("size", "x-large");
        pizza.setAttribute("emptyString", "");
        pizza.setClearAttribute("greenPeppers");
        pizza.setNullAttribute("pineapple");

        AbstractNameQuery pizzaOvenQuery = getAbstractNameQuery(":name=PizzaOven");
        AbstractNameQuery toasterOvenQuery = getAbstractNameQuery(":name=ToasterOven,*");
        AbstractNameQuery[] queries = new AbstractNameQuery[]{pizzaOvenQuery, toasterOvenQuery};
        ReferencePatterns ovenPatterns = getReferencePatterns(queries);
        pizza.setReferencePatterns("oven", ovenPatterns);
        pizza.setClearReference("microwave");

        assertCopyIdentical(dinnerMenu);

        dinnerMenu.addGBean(pizza);
        assertCopyIdentical(dinnerMenu);

        GBeanOverride garlicCheeseBread = new GBeanOverride("Garlic Cheese Bread", true);
        ReferencePatterns toasterOvenPatterns = new ReferencePatterns(Collections.singleton(toasterOvenQuery));
        garlicCheeseBread.setReferencePatterns("oven", toasterOvenPatterns);
        dinnerMenu.addGBean(garlicCheeseBread);
        assertCopyIdentical(dinnerMenu);
    }

    public void testServerXml() throws Exception {
        ServerOverride restaurant = new ServerOverride();
        assertCopyIdentical(restaurant);

        ConfigurationOverride dinnerMenu = new ConfigurationOverride(new Artifact("test","Dinner Menu","1.0","car"), false);
        restaurant.addConfiguration(dinnerMenu);
        GBeanOverride pizza = new GBeanOverride("Pizza", false);
        pizza.setAttribute("cheese", "mozzarella");
        pizza.setAttribute("size", "x-large");
        pizza.setAttribute("emptyString", "");
        pizza.setClearAttribute("greenPeppers");
        pizza.setNullAttribute("pineapple");
        AbstractNameQuery pizzaOvenQuery = getAbstractNameQuery(":name=PizzaOven");
        AbstractNameQuery toasterOvenQuery = getAbstractNameQuery(":name=ToasterOven,*");
        AbstractNameQuery[] queries = new AbstractNameQuery[]{pizzaOvenQuery, toasterOvenQuery};
        ReferencePatterns ovenPatterns = getReferencePatterns(queries);
        pizza.setReferencePatterns("oven", ovenPatterns);
        pizza.setClearReference("microwave");
        dinnerMenu.addGBean(pizza);
        GBeanOverride garlicCheeseBread = new GBeanOverride("Garlic Cheese Bread", true);
        ReferencePatterns toasterOvenPatterns = new ReferencePatterns(Collections.singleton(toasterOvenQuery));
        garlicCheeseBread.setReferencePatterns("oven", toasterOvenPatterns);
        dinnerMenu.addGBean(garlicCheeseBread);
        assertCopyIdentical(restaurant);

        ConfigurationOverride drinkMenu = new ConfigurationOverride(new Artifact("test","Drink Menu","1.0","car"), false);
        restaurant.addConfiguration(drinkMenu);
        GBeanOverride beer = new GBeanOverride("Beer", true);
        pizza.setReferencePatterns("glass", getReferencePatterns(new AbstractNameQuery[] {
            getAbstractNameQuery(":name=PintGlass"),
            getAbstractNameQuery(":name=BeerStein"),
            getAbstractNameQuery(":name=BeerBottle"),
            getAbstractNameQuery(":name=BeerCan")
        }));
        drinkMenu.addGBean(beer);
        GBeanOverride wine = new GBeanOverride("Wine", true);
        wine.setReferencePatterns("glass", getReferencePatterns(new AbstractNameQuery[] {
            getAbstractNameQuery(":name=WineGlass"),
            getAbstractNameQuery(":name=WineBottle"),
            getAbstractNameQuery(":name=BoxWine")
        }));
        drinkMenu.addGBean(wine);
        assertCopyIdentical(restaurant);
    }

    private static final String REFERENCE_XML =
            "        <gbean name=\"EJBBuilder\">\n" +
                    "            <attribute name=\"listener\">?name=JettyWebContainer</attribute>\n" +
                    "            <reference name=\"ServiceBuilders\">\n" +
                    "                <pattern>\n" +
                    "                    <name>GBeanBuilder</name>\n" +
                    "                </pattern>\n" +
                    "                <pattern>\n" +
                    "                    <name>PersistenceUnitBuilder</name>\n" +
                    "                </pattern>\n" +
                    "            </reference>\n" +
                    "            <reference name=\"WebServiceBuilder\">\n" +
                    "                <pattern>\n" +
                    "                    <name>CXFBuilder</name>\n" +
                    "                </pattern>\n" +
                    "            </reference>\n" +
                    "        </gbean>";

    public void testReferenceXml() throws Exception {
        InputStream in = new ByteArrayInputStream(REFERENCE_XML.getBytes());
        Element gbeanElement = parseXml(in, "gbean");
        GBeanOverride gbean = new GBeanOverride(gbeanElement);
        assertCopyIdentical(gbean);
    }

    private void assertCopyIdentical(ServerOverride server) throws Exception {
        ServerOverride copy = copy(server);
        assertIdentical(server, copy);
    }

    private void assertCopyIdentical(ConfigurationOverride configuration) throws Exception {
        ConfigurationOverride copy = copy(configuration);
        assertIdentical(configuration, copy);
    }

    private void assertCopyIdentical(GBeanOverride gbean) throws Exception {
        GBeanOverride copy = copy(gbean);
        assertIdentical(gbean, copy);
    }

    private void assertIdentical(ServerOverride expected, ServerOverride actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertNotSame(expected, actual);

        Map expectedGBeans = expected.getConfigurations();
        Map actualGBeans = actual.getConfigurations();
        assertEquals(expectedGBeans.size(), actualGBeans.size());

        for (Iterator iterator = expectedGBeans.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object name = entry.getKey();
            ConfigurationOverride expectedConfiguration = (ConfigurationOverride) entry.getValue();
            ConfigurationOverride actualConfiguration = (ConfigurationOverride) actualGBeans.get(name);
            assertIdentical(expectedConfiguration, actualConfiguration);
        }
    }

    private void assertIdentical(ConfigurationOverride expected, ConfigurationOverride actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertNotSame(expected, actual);
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.isLoad(), actual.isLoad());

        Map expectedGBeans = expected.getGBeans();
        Map actualGBeans = actual.getGBeans();
        assertEquals(expectedGBeans.size(), actualGBeans.size());

        for (Iterator iterator = expectedGBeans.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object name = entry.getKey();
            GBeanOverride expectedGBean = (GBeanOverride) entry.getValue();
            GBeanOverride actualGBean = (GBeanOverride) actualGBeans.get(name);
            assertIdentical(expectedGBean, actualGBean);
        }
    }

    private void assertIdentical(GBeanOverride expected, GBeanOverride actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertNotSame(expected, actual);
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.isLoad(), actual.isLoad());
        assertEquals(expected.getAttributes(), actual.getAttributes());
        assertEquals(expected.getClearAttributes(), actual.getClearAttributes());
        assertEquals(expected.getNullAttributes(), actual.getNullAttributes());
        assertEquals(expected.getReferences(), actual.getReferences());
        assertEquals(expected.getClearReferences(), actual.getClearReferences());
    }

    private ServerOverride copy(ServerOverride server) throws Exception {
        Document doc = createDocument();
        return new ServerOverride(readElement(server.writeXml(doc), "attributes"));
    }

    private ConfigurationOverride copy(ConfigurationOverride configuration) throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("temp");
        doc.appendChild(root);
        return new ConfigurationOverride(readElement(configuration.writeXml(doc, root), "module"));
    }

    private GBeanOverride copy(GBeanOverride gbean) throws Exception {
        Document doc = createDocument();
        Element root = doc.createElement("temp");
        doc.appendChild(root);
        return new GBeanOverride(readElement(gbean.writeXml(doc, root), "gbean"));
    }

    private Element parseXml(InputStream in, String name) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = XmlUtil.newDocumentBuilderFactory();
        Document doc = documentBuilderFactory.newDocumentBuilder().parse(in);
        Element elem = doc.getDocumentElement();
        if(elem.getNodeName().equals(name)) {
            return elem;
        }
        NodeList list = elem.getElementsByTagName(name);
        return (Element) list.item(0);
    }

    private Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dFactory = XmlUtil.newDocumentBuilderFactory();
        dFactory.setValidating(false);
        return dFactory.newDocumentBuilder().newDocument();
    }

    private Element readElement(Element e, String name) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TransformerFactory xfactory = XmlUtil.newTransformerFactory();
        Transformer xform = xfactory.newTransformer();
        xform.setOutputProperty(OutputKeys.INDENT, "yes");
        xform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        xform.transform(new DOMSource(e), new StreamResult(out));
        log.debug(new String(out.toByteArray()));
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return parseXml(in, name);
    }
}
