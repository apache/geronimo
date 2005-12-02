/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.system.configuration;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class ServerOverrideTest extends TestCase {
    public void testBasics() throws Exception {
        GBeanOverride pizza = new GBeanOverride("Pizza", true);
        assertTrue(pizza.isLoad());

        pizza.setLoad(false);
        assertFalse(pizza.isLoad());

        pizza.setAttribute("cheese", "mozzarella");
        assertEquals("mozzarella", pizza.getAttribute("cheese"));

        ObjectName pizzaOvenPattern = new ObjectName(":name=PizzaOven,j2eeType=oven,*");
        pizza.setReferencePattern("oven", pizzaOvenPattern);
        assertEquals(Collections.singleton(pizzaOvenPattern), pizza.getReferencePatterns("oven"));

        ObjectName toasterOvenPattern = new ObjectName(":name=ToasterOven,j2eeType=oven,*");
        Set ovenPatterns = new LinkedHashSet(Arrays.asList(new ObjectName[] {pizzaOvenPattern, toasterOvenPattern}));
        pizza.setReferencePatterns("oven", ovenPatterns);
        assertEquals(ovenPatterns, pizza.getReferencePatterns("oven"));

        ConfigurationOverride dinnerMenu = new ConfigurationOverride("Dinner Menu", true);
        assertTrue(dinnerMenu.isLoad());

        dinnerMenu.setLoad(false);
        assertFalse(dinnerMenu.isLoad());

        dinnerMenu.addGBean(pizza);
        assertSame(pizza, dinnerMenu.getGBean("Pizza"));

        ServerOverride restaurant = new ServerOverride();
        restaurant.addConfiguration(dinnerMenu);
        assertSame(dinnerMenu, restaurant.getConfiguration("Dinner Menu"));
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

        ObjectName pizzaOvenPattern = new ObjectName(":name=PizzaOven,j2eeType=oven,*");
        pizza.setReferencePattern("oven", pizzaOvenPattern);
        assertCopyIdentical(pizza);

        ObjectName toasterOvenPattern = new ObjectName(":name=ToasterOven,j2eeType=oven,*");
        Set ovenPatterns = new LinkedHashSet(Arrays.asList(new ObjectName[] {pizzaOvenPattern, toasterOvenPattern}));
        pizza.setReferencePatterns("oven", ovenPatterns);
        assertCopyIdentical(pizza);
    }

    public void testConfigurationXml() throws Exception {
        ConfigurationOverride dinnerMenu = new ConfigurationOverride("Dinner Menu", true);
        assertCopyIdentical(dinnerMenu);

        dinnerMenu.setLoad(false);
        assertCopyIdentical(dinnerMenu);

        GBeanOverride pizza = new GBeanOverride("Pizza", false);
        pizza.setAttribute("cheese", "mozzarella");
        pizza.setAttribute("size", "x-large");
        ObjectName pizzaOvenPattern = new ObjectName(":name=PizzaOven,j2eeType=oven,*");
        ObjectName toasterOvenPattern = new ObjectName(":name=ToasterOven,j2eeType=oven,*");
        pizza.setReferencePatterns("oven", new LinkedHashSet(Arrays.asList(new ObjectName[] {pizzaOvenPattern, toasterOvenPattern})));
        assertCopyIdentical(dinnerMenu);

        dinnerMenu.addGBean(pizza);
        assertCopyIdentical(dinnerMenu);

        GBeanOverride garlicCheeseBread = new GBeanOverride("Garlic Cheese Bread", true);
        garlicCheeseBread.setReferencePattern("oven", toasterOvenPattern);
        dinnerMenu.addGBean(garlicCheeseBread);
        assertCopyIdentical(dinnerMenu);
    }

    public void testServerXml() throws Exception {
        ServerOverride restaurant = new ServerOverride();
        assertCopyIdentical(restaurant);

        ConfigurationOverride dinnerMenu = new ConfigurationOverride("Dinner Menu", false);
        restaurant.addConfiguration(dinnerMenu);
        GBeanOverride pizza = new GBeanOverride("Pizza", false);
        pizza.setAttribute("cheese", "mozzarella");
        pizza.setAttribute("size", "x-large");
        ObjectName pizzaOvenPattern = new ObjectName(":name=PizzaOven,j2eeType=oven,*");
        ObjectName toasterOvenPattern = new ObjectName(":name=ToasterOven,j2eeType=oven,*");
        pizza.setReferencePatterns("oven", new LinkedHashSet(Arrays.asList(new ObjectName[] {pizzaOvenPattern, toasterOvenPattern})));
        dinnerMenu.addGBean(pizza);
        GBeanOverride garlicCheeseBread = new GBeanOverride("Garlic Cheese Bread", true);
        garlicCheeseBread.setReferencePattern("oven", toasterOvenPattern);
        dinnerMenu.addGBean(garlicCheeseBread);
        assertCopyIdentical(restaurant);

        ConfigurationOverride drinkMenu = new ConfigurationOverride("Drink Menu", false);
        restaurant.addConfiguration(drinkMenu);
        GBeanOverride beer = new GBeanOverride("Beer", true);
        pizza.setReferencePatterns("glass", new LinkedHashSet(Arrays.asList(new ObjectName[] {
            new ObjectName(":name=PintGlass"),
            new ObjectName(":name=BeerStein"),
            new ObjectName(":name=BeerBottle"),
            new ObjectName(":name=BeerCan")
        })));
        drinkMenu.addGBean(beer);
        GBeanOverride wine = new GBeanOverride("Wine", true);
        wine.setReferencePatterns("glass", new LinkedHashSet(Arrays.asList(new ObjectName[] {
            new ObjectName(":name=WineGlass"),
            new ObjectName(":name=WineBottle"),
            new ObjectName(":name=BoxWine")
        })));
        drinkMenu.addGBean(wine);
        assertCopyIdentical(restaurant);
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
    }

    private ServerOverride copy(ServerOverride server) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        server.writeXml(new PrintWriter(out, true));
        System.out.println();
        System.out.println();
        System.out.println(new String(out.toByteArray()));
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Element element = parseXml(in);
        ServerOverride copy = new ServerOverride(element);
        return copy;
    }

    private ConfigurationOverride copy(ConfigurationOverride configuration) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        configuration.writeXml(new PrintWriter(out, true));
        System.out.println();
        System.out.println();
        System.out.println(new String(out.toByteArray()));
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Element element = parseXml(in);
        ConfigurationOverride copy = new ConfigurationOverride(element);
        return copy;
    }

    private GBeanOverride copy(GBeanOverride gbean) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        gbean.writeXml(new PrintWriter(out, true));
        System.out.println();
        System.out.println();
        System.out.println(new String(out.toByteArray()));
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Element element = parseXml(in);
        GBeanOverride copy = new GBeanOverride(element);
        return copy;
    }

    private Element parseXml(InputStream in) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document doc = documentBuilderFactory.newDocumentBuilder().parse(in);
        return doc.getDocumentElement();
    }
}
