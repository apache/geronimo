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

package org.apache.geronimo.jasper.deployment;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.junit.Assert;

/**
 * @version $Rev$ $Date$
 */
public class SchemaParseTest extends TestCase {

    private TestJspModuleBuilderExtension jspModuleBuilderExtension;

    private ClassLoader classLoader = SchemaParseTest.class.getClassLoader();

    @Override
    public void setUp() {
        //webAppInfo is used to prevent the NPE in JspModuleBuilderExtension constructor
        WebAppInfo webAppInfo = new WebAppInfo();
        webAppInfo.servlets.add(new ServletInfo());
        jspModuleBuilderExtension = new TestJspModuleBuilderExtension(null, null, webAppInfo, null);
    }

    public void testParse11DTD() throws Exception {
        URL url = classLoader.getResource("1_1_dtd/taglib-tag-listener.tld");
        List<String> expectedTagClassNames = Arrays.asList("org.apache.struts.taglib.nested.NestedPropertyTag",
                "org.apache.struts.taglib.nested.NestedWriteNestingTag",
                "org.apache.struts.taglib.nested.NestedRootTag",
                "org.apache.struts.taglib.nested.bean.NestedDefineTag",
                "org.apache.struts.taglib.nested.bean.NestedMessageTag",
                "org.apache.struts.taglib.nested.bean.NestedSizeTag",
                "org.apache.struts.taglib.nested.bean.NestedWriteTag",
                "org.apache.struts.taglib.nested.html.NestedCheckboxTag",
                "org.apache.struts.taglib.nested.html.NestedErrorsTag",
                "org.apache.struts.taglib.nested.html.NestedFileTag");
        List<String> expectedListenerClassNames = Collections.<String> emptyList();
        jspModuleBuilderExtension.testParse(url, expectedListenerClassNames, expectedTagClassNames);
    }

    public void testParse12DTD() throws Exception {
        URL url = classLoader.getResource("1_2_dtd/taglib-tag-listener.tld");
        List<String> expectedTagClassNames = Arrays.asList("examples.LogTag");
        List<String> expectedListenerClassNames = Arrays.asList("TestListenerClassName");
        jspModuleBuilderExtension.testParse(url, expectedListenerClassNames, expectedTagClassNames);
    }

    public void testParse20XSD() throws Exception {
        URL url = classLoader.getResource("2_0_xsd/taglib-tag-listener.tld");
        List<String> expectedTagClassNames = Arrays.asList("org.apache.taglibs.standard.tag.rt.fmt.RequestEncodingTag",
                "org.apache.taglibs.standard.tag.rt.fmt.SetLocaleTag",
                "org.apache.taglibs.standard.tag.rt.fmt.TimeZoneTag",
                "org.apache.taglibs.standard.tag.rt.fmt.SetTimeZoneTag",
                "org.apache.taglibs.standard.tag.rt.fmt.BundleTag",
                "org.apache.taglibs.standard.tag.rt.fmt.SetBundleTag",
                "org.apache.taglibs.standard.tag.rt.fmt.MessageTag", "org.apache.taglibs.standard.tag.rt.fmt.ParamTag",
                "org.apache.taglibs.standard.tag.rt.fmt.FormatNumberTag",
                "org.apache.taglibs.standard.tag.rt.fmt.ParseNumberTag",
                "org.apache.taglibs.standard.tag.rt.fmt.FormatDateTag",
                "org.apache.taglibs.standard.tag.rt.fmt.ParseDateTag");
        List<String> expectedListenerClassNames = Arrays.asList("TestListenerA", "TestListenerB");
        jspModuleBuilderExtension.testParse(url, expectedListenerClassNames, expectedTagClassNames);
    }

    public void testParse21XSD() throws Exception {
        URL url = classLoader.getResource("2_1_xsd/taglib-tag-listener.tld");
        List<String> expectedTagClassNames = Arrays.asList("org.apache.taglibs.standard.tag.rt.fmt.RequestEncodingTag",
                "org.apache.taglibs.standard.tag.rt.fmt.SetLocaleTag",
                "org.apache.taglibs.standard.tag.rt.fmt.TimeZoneTag",
                "org.apache.taglibs.standard.tag.rt.fmt.SetTimeZoneTag",
                "org.apache.taglibs.standard.tag.rt.fmt.BundleTag",
                "org.apache.taglibs.standard.tag.rt.fmt.SetBundleTag",
                "org.apache.taglibs.standard.tag.rt.fmt.MessageTag", "org.apache.taglibs.standard.tag.rt.fmt.ParamTag",
                "org.apache.taglibs.standard.tag.rt.fmt.FormatNumberTag",
                "org.apache.taglibs.standard.tag.rt.fmt.ParseNumberTag",
                "org.apache.taglibs.standard.tag.rt.fmt.FormatDateTag",
                "org.apache.taglibs.standard.tag.rt.fmt.ParseDateTag");
        List<String> expectedListenerClassNames = Arrays.asList("TestListenerA", "TestListenerB");
        jspModuleBuilderExtension.testParse(url, expectedListenerClassNames, expectedTagClassNames);
    }

    static class TestJspModuleBuilderExtension extends JspModuleBuilderExtension {

        public TestJspModuleBuilderExtension(Environment defaultEnvironment, Collection<String> excludedListenerNames,
                WebAppInfo defaultJspServlet, NamingBuilder namingBuilders) {
            super(defaultEnvironment, excludedListenerNames, defaultJspServlet, namingBuilders);
        }

        public void testParse(URL url, List<String> expectedListenerClassNames, List<String> expectedTagClassNames)
                throws DeploymentException {
            List<String> listenerClassNames = new ArrayList<String>();
            List<String> tagClassNames = new ArrayList<String>();
            parseTldFile(url, listenerClassNames, tagClassNames);
            Assert.assertEquals(expectedListenerClassNames.size(), listenerClassNames.size());
            for (String expectedListenerClassName : expectedListenerClassNames) {
                Assert.assertTrue("expected class name " + expectedListenerClassName + "is not found in the result"
                        + listenerClassNames, listenerClassNames.contains(expectedListenerClassName));
            }
            Assert.assertEquals(expectedTagClassNames.size(), tagClassNames.size());
            for (String expectedTagClassName : expectedTagClassNames) {
                Assert.assertTrue("expected class name " + expectedTagClassName + "is not found in the result"
                        + listenerClassNames, tagClassNames.contains(expectedTagClassName));
            }
        }
    }
}
