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
package org.apache.geronimo.kernel.config.xstream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomReader;
import com.thoughtworks.xstream.io.xml.DomWriter;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.GBeanState;
import org.apache.geronimo.kernel.repository.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class XStreamGBeanState implements GBeanState {
    /**
     * GBeans contained in this configuration.
     */
    private final List<GBeanData> gbeans = new ArrayList<GBeanData>();

    /**
     * The serialized form of the gbeans.  Once this is set on more gbeans can be added.
     */
    private Element gbeanState;

    public XStreamGBeanState(Element gbeanState) {
        this.gbeanState = gbeanState;
    }

    public XStreamGBeanState(Collection<GBeanData> gbeans) {
        if (gbeans != null){
            this.gbeans.addAll(gbeans);
        }
    }

    public Element getGBeanState() throws IOException {
        if (gbeanState == null) {
            gbeanState = XStreamGBeanState.storeGBeans(gbeans);
            gbeans.clear();
        }
        return gbeanState;
    }

    public List<GBeanData> getGBeans(Bundle bundle) throws InvalidConfigException {
        if (gbeanState == null) {
            return Collections.unmodifiableList(gbeans);
        }
        gbeans.addAll(XStreamGBeanState.loadGBeans(gbeanState, bundle));
        return Collections.unmodifiableList(gbeans);
    }

    public void addGBean(GBeanData gbeanData) {
        if (gbeanState != null) {
            throw new IllegalStateException("GBeans have been serialized, so no more GBeans can be added");
        }

        gbeans.add(gbeanData);
    }

    public GBeanData addGBean(String name, GBeanInfo gbeanInfo, Naming naming, Environment environment) {
        if (gbeanState != null) {
            throw new IllegalStateException("GBeans have been serialized, so no more GBeans can be added");
        }

        String j2eeType = gbeanInfo.getJ2eeType();
        if (j2eeType == null) j2eeType = "GBean";
        AbstractName abstractName = naming.createRootName(environment.getConfigId(), name, j2eeType);
        GBeanData gBeanData = new GBeanData(abstractName, gbeanInfo);
        addGBean(gBeanData);
        return gBeanData;
    }

    public GBeanData addGBean(String name, Class gbeanClass, Naming naming, Environment environment) {
        if (gbeanState != null) {
            throw new IllegalStateException("GBeans have been serialized, so no more GBeans can be added");
        }
        GBeanData gBeanData = new GBeanData(gbeanClass);

        String j2eeType = gBeanData.getGBeanInfo().getJ2eeType();
        if (j2eeType == null) j2eeType = "GBean";
        AbstractName abstractName = naming.createRootName(environment.getConfigId(), name, j2eeType);
        gBeanData.setAbstractName(abstractName);
        addGBean(gBeanData);
        return gBeanData;
    }

    private static List<GBeanData> loadGBeans(Element element, Bundle bundle) throws InvalidConfigException {
        if (element != null) {
            // Set the thread context classloader so deserializing classes can grab the cl from the thread
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
//                Thread.currentThread().setContextClassLoader(bundle);

                DomReader reader = new DomReader(element);
                XStream xstream = XStreamUtil.createXStream();
                //TODO Obviously, totally broken
//                xstream.setClassLoader(bundle);
                Object o = xstream.unmarshal(reader);
                GBeanData[] gbeanDatas = (GBeanData[]) o;
                return Arrays.asList(gbeanDatas);
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to load gbeans", e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
        return Collections.emptyList();
    }

    private static Element storeGBeans(List<GBeanData> gbeans) throws IOException {
        GBeanData[] gbeanDatas = gbeans.toArray(new GBeanData[gbeans.size()]);

        DocumentBuilderFactory documentBuilderFactory = XmlUtil.newDocumentBuilderFactory();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw (IOException)new IOException("Cannot instantiate " + Document.class.getName()).initCause(e);
        }
        Document document = documentBuilder.newDocument();
        DomWriter writer = new DomWriter(document);

        XStream xstream = XStreamUtil.createXStream();
        xstream.marshal(gbeanDatas, writer);

//        FileWriter w = new FileWriter("target/foo.xml");
//        xstream.toXML(gbeanDatas, w);
//        w.close();

        return document.getDocumentElement();
    }
}
