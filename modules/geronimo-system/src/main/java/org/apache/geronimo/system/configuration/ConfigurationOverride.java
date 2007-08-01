/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.configuration.condition.ConditionParser;
import org.apache.geronimo.system.configuration.condition.JexlConditionParser;
import org.apache.geronimo.system.configuration.condition.JexlExpressionParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * @version $Rev$ $Date$
 */
class ConfigurationOverride {
    private final Artifact name;
    private boolean load;
    private String condition;
    private String comment;
    private final Map<Object, GBeanOverride> gbeans = new LinkedHashMap<Object, GBeanOverride>();

    /**
     * Cached condition parser; lazy init on the first call to {@link #parseCondition()}
     * when {@link #condition} is non-null.
     */
    private static ConditionParser parser;

    public ConfigurationOverride(Artifact name, boolean load) {
        this.name = name;
        this.load = load;
    }

    /**
     * Create a copy of a ConfigurationOverride with a new Artifact name
     * @param base The original
     * @param name The new Artifact name
     */
    public ConfigurationOverride(ConfigurationOverride base, Artifact name) {
        this.name = name;
        this.load = base.load;
        this.condition = base.condition;
        this.comment = base.comment;

        for (GBeanOverride gbean : base.gbeans.values()) {
            GBeanOverride replacement = new GBeanOverride(gbean, base.name.toString(), name.toString());
            gbeans.put(replacement.getName(), replacement);
        }
    }

    public ConfigurationOverride(Element element, JexlExpressionParser expressionParser) throws InvalidGBeanException {
        name = Artifact.create(element.getAttribute("name"));

        condition = element.getAttribute("condition");
        comment = getCommentText(element);
        
        String loadConfigString = element.getAttribute("load");
        load = ! "false".equals(loadConfigString);

        NodeList gbeans = element.getElementsByTagName("gbean");
        for (int g = 0; g < gbeans.getLength(); g++) {
            Element gbeanElement = (Element) gbeans.item(g);
            GBeanOverride gbean = new GBeanOverride(gbeanElement, expressionParser);
            addGBean(gbean);
        }
    }

    public Artifact getName() {
        return name;
    }

    public String getCondition() {
        return condition;
    }
    
    public void setCondition(final String condition) {
        this.condition = condition;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private String getCommentText(Element element) {
        String commentText = "";

        NodeList children = element.getChildNodes();
        Element child = null;

        for (int nodePos = 0; nodePos < children.getLength(); nodePos++) {
            if (children.item(nodePos) instanceof Element) {
                child = (Element) children.item(nodePos);

                if (child.getTagName().equals("comment")) {
                    commentText = child.getTextContent();
                    break;
                }
            }
        }

        return commentText;
    }

    private boolean parseCondition() {
        if (condition == null) {
            // no condition means true
            return true;
        }

        // Create a parser if one does not already exist
        if (parser == null) {
            parser = new JexlConditionParser();
        }
        
        return parser.evaluate(condition);
    }
    
    public boolean isLoad() {
        return load && parseCondition();
    }

    public void setLoad(boolean load) {
        this.load = load;
    }
    
    public GBeanOverride getGBean(String gbeanName) {
        return gbeans.get(gbeanName);
    }

    public void addGBean(GBeanOverride gbean) {
        gbeans.put(gbean.getName(), gbean);
    }

    public void addGBean(String gbeanName, GBeanOverride gbean) {
        gbeans.put(gbeanName, gbean);
    }

    public Map getGBeans() {
        return gbeans;
    }

    public GBeanOverride getGBean(AbstractName gbeanName) {
        return gbeans.get(gbeanName);
    }

    public void addGBean(AbstractName gbeanName, GBeanOverride gbean) {
        gbeans.put(gbeanName, gbean);
    }

    public Element writeXml(Document doc, Element root) {
        Element module = doc.createElement("module");
        root.appendChild(module);
        module.setAttribute("name", name.toString());

        if (! load) {
            module.setAttribute("load", "false");
        }

        if (condition != null && condition.trim().length() != 0) {
            module.setAttribute("condition", condition);
        }

        if (comment != null && comment.trim().length() > 0) {
            Element eleComment = doc.createElement("comment");
            eleComment.setTextContent(comment);

            module.appendChild(eleComment);
        }

        // GBeans
        for (GBeanOverride gbeanOverride : gbeans.values()) {
            gbeanOverride.writeXml(doc, module);
        }
        return module;
    }
}
