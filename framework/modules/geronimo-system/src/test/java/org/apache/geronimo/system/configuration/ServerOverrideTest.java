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

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.apache.geronimo.system.configuration.condition.JexlExpressionParser;
import org.apache.geronimo.system.configuration.condition.ParserUtils;
import org.apache.geronimo.system.plugin.model.GbeanType;
import org.apache.geronimo.system.plugin.model.ModuleType;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class ServerOverrideTest extends TestCase {
    private JexlExpressionParser expressionParser;

    private GAttributeInfo cheeseInfo = new GAttributeInfo("cheese", String.class.getName(), true, true, "getCheese", "setCheese");
    private GAttributeInfo sizeInfo = new GAttributeInfo("size", String.class.getName(), true, true, "getSize", "setSize");
    private GAttributeInfo emptyStringInfo = new GAttributeInfo("emptyString", String.class.getName(), true, true, "getEmptyString", "setEmptyString");
    private GAttributeInfo portInfo = new GAttributeInfo("port", int.class.getName(), true, true, "getPort", "setPort");
    private GAttributeInfo expressionInfo = new GAttributeInfo("expression", boolean.class.getName(), true, true, "getExpression", "setExpression");
    private Bundle bundle = new MockBundle(getClass().getClassLoader(), null, 0L);

    protected void setUp() throws java.lang.Exception {
        Map<String, Object> subs = new HashMap<String, Object>();
        subs.put("host", "localhost");
        subs.put("port", "8080");
        subs.put("portOffset", "1");
        ParserUtils.addDefaultVariables(subs);
        expressionParser = new JexlExpressionParser(subs);
    }

    public void testBasics() throws Exception {
        GBeanOverride pizza = new GBeanOverride("Pizza", true, expressionParser);
        assertTrue(pizza.isLoad());

        pizza.setLoad(false);
        assertFalse(pizza.isLoad());

        pizza.setAttribute(cheeseInfo, "mozzarella", bundle);
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
        Set<AbstractNameQuery> querySet = new LinkedHashSet<AbstractNameQuery>(Arrays.asList(queries));
        return new ReferencePatterns(querySet);
    }

    private AbstractNameQuery getAbstractNameQuery(String pizzaOvenString) throws MalformedObjectNameException {
        ObjectName pizzaOvenPattern = new ObjectName(pizzaOvenString);
        return new AbstractNameQuery(null, pizzaOvenPattern.getKeyPropertyList(), Collections.EMPTY_SET);
    }

    public void testGBeanXml() throws Exception {
        GBeanOverride pizza = new GBeanOverride("Pizza", true, expressionParser);
        assertCopyIdentical(pizza);

        pizza.setLoad(false);
        assertCopyIdentical(pizza);

        pizza.setAttribute(cheeseInfo, "mozzarella", bundle);
        assertCopyIdentical(pizza);

        pizza.setAttribute(sizeInfo, "x-large", bundle);
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

        GBeanOverride pizza = new GBeanOverride("Pizza", false, expressionParser);
        pizza.setAttribute(cheeseInfo, "mozzarella", bundle);
        pizza.setAttribute(sizeInfo, "x-large", bundle);
        pizza.setAttribute(emptyStringInfo, "", bundle);
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

        GBeanOverride garlicCheeseBread = new GBeanOverride("Garlic Cheese Bread", true, expressionParser);
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
        GBeanOverride pizza = new GBeanOverride("Pizza", false, expressionParser);
        pizza.setAttribute(cheeseInfo, "mozzarella", bundle);
        pizza.setAttribute(sizeInfo, "x-large", bundle);
        pizza.setAttribute(emptyStringInfo, "", bundle);
        pizza.setClearAttribute("greenPeppers");
        pizza.setNullAttribute("pineapple");
        AbstractNameQuery pizzaOvenQuery = getAbstractNameQuery(":name=PizzaOven");
        AbstractNameQuery toasterOvenQuery = getAbstractNameQuery(":name=ToasterOven,*");
        AbstractNameQuery[] queries = new AbstractNameQuery[]{pizzaOvenQuery, toasterOvenQuery};
        ReferencePatterns ovenPatterns = getReferencePatterns(queries);
        pizza.setReferencePatterns("oven", ovenPatterns);
        pizza.setClearReference("microwave");
        dinnerMenu.addGBean(pizza);
        GBeanOverride garlicCheeseBread = new GBeanOverride("Garlic Cheese Bread", true, expressionParser);
        ReferencePatterns toasterOvenPatterns = new ReferencePatterns(Collections.singleton(toasterOvenQuery));
        garlicCheeseBread.setReferencePatterns("oven", toasterOvenPatterns);
        dinnerMenu.addGBean(garlicCheeseBread);
        assertCopyIdentical(restaurant);

        ConfigurationOverride drinkMenu = new ConfigurationOverride(new Artifact("test","Drink Menu","1.0","car"), false);
        restaurant.addConfiguration(drinkMenu);
        GBeanOverride beer = new GBeanOverride("Beer", true, expressionParser);
        pizza.setReferencePatterns("glass", getReferencePatterns(new AbstractNameQuery[] {
            getAbstractNameQuery(":name=PintGlass"),
            getAbstractNameQuery(":name=BeerStein"),
            getAbstractNameQuery(":name=BeerBottle"),
            getAbstractNameQuery(":name=BeerCan")
        }));
        drinkMenu.addGBean(beer);
        GBeanOverride wine = new GBeanOverride("Wine", true, expressionParser);
        wine.setReferencePatterns("glass", getReferencePatterns(new AbstractNameQuery[] {
            getAbstractNameQuery(":name=WineGlass"),
            getAbstractNameQuery(":name=WineBottle"),
            getAbstractNameQuery(":name=BoxWine")
        }));
        drinkMenu.addGBean(wine);
        assertCopyIdentical(restaurant);
    }

    private static final String REFERENCE_COMMENT_XML =
            "<module name=\"org.apache.geronimo.config/commentTest/2.0/car\" xmlns='" + GBeanOverride.ATTRIBUTE_NAMESPACE + "'>\n" +
            "    <comment>This comment should get properly parsed</comment>\n" +
            "    <gbean name=\"CommentBean\">\n" +
            "        <attribute name=\"value\">someValue</attribute>\n" +
            "    </gbean>\n" +
            "</module>";

    public void testCommentXml() throws Exception {
        String comment = "This comment should get properly parsed";

        Reader in = new StringReader(REFERENCE_COMMENT_XML);
        ModuleType module = AttributesXmlUtil.loadModule(in);
        ConfigurationOverride commentConfig = new ConfigurationOverride(module, expressionParser);

        assertNotNull(commentConfig);
        assertEquals(commentConfig.getComment(), comment);
    }

    private static final String REFERENCE_XML =
            "        <gbean name=\"EJBBuilder\" xmlns='\" + GBeanOverride.ATTRIBUTE_NAMESPACE + \"'>\n" +
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
        Reader in = new StringReader(REFERENCE_XML);
        GbeanType gbeanElement = AttributesXmlUtil.loadGbean(in);
        GBeanOverride gbean = new GBeanOverride(gbeanElement, expressionParser);
        assertCopyIdentical(gbean);
    }

    private static final String EXPRESSION_XML =
            "        <gbean name=\"mockGBean\" xmlns='\" + GBeanOverride.ATTRIBUTE_NAMESPACE + \"'>\n" +
                    "            <attribute name=\"value\">${host}</attribute>\n" +
                    "            <attribute name=\"port\">${port}</attribute>\n" +
                    "            <attribute name=\"expression\">${host}</attribute>\n" +
                    "        </gbean>";

    public void testExpressionXml() throws Exception {
        Reader in = new StringReader(EXPRESSION_XML);
        GbeanType gbeanElement = AttributesXmlUtil.loadGbean(in);
        GBeanOverride gbean = new GBeanOverride(gbeanElement, expressionParser);
        assertCopyIdentical(gbean);
        GBeanData data = new GBeanData(MockGBean.GBEAN_INFO);
        gbean.setAttribute(portInfo, "${port}", bundle);
        gbean.applyOverrides(data, null, null, bundle);
        assertEquals(8080, data.getAttribute("port"));
        gbean.setAttribute(portInfo, "${port + 1}", bundle);
        gbean.applyOverrides(data, null, null, bundle);
        assertEquals(8081, data.getAttribute("port"));
        gbean.setAttribute(portInfo, "${port + portOffset}", bundle);
        gbean.applyOverrides(data, null, null, bundle);
        assertEquals(8081, data.getAttribute("port"));
        
        gbean.setAttribute(expressionInfo, "${if (java == null) 'null'; else 'non-null';}", bundle);
        gbean.applyOverrides(data, null, null, bundle);
        assertEquals("non-null", data.getAttribute("expression"));
        
        gbean.setAttribute(expressionInfo, "${if (java == null) { 'null'; } else { if (os == null) { 'java,null'; } else { 'java,non-null'; } } }", bundle);
        gbean.applyOverrides(data, null, null, bundle);
        assertEquals("java,non-null", data.getAttribute("expression"));
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

        for (Object o : expectedGBeans.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
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

        for (Object o : expectedGBeans.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
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
        return new ServerOverride(server.writeXml(), expressionParser);
    }

    private ConfigurationOverride copy(ConfigurationOverride configuration) throws Exception {
        return new ConfigurationOverride(configuration.writeXml(), expressionParser);
    }

    private GBeanOverride copy(GBeanOverride gbean) throws Exception {
        return new GBeanOverride(gbean.writeXml(), expressionParser);
    }

}
