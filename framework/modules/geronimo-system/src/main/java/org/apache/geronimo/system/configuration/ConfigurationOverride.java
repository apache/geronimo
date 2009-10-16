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
import org.apache.geronimo.system.plugin.model.GbeanType;
import org.apache.geronimo.system.plugin.model.ModuleType;

/**
 * @version $Rev$ $Date$
 */
class ConfigurationOverride {
    
    /**
     * Default condition parser.
     */
    private static final ConditionParser DEFAULT_COND_PARSER = new JexlConditionParser();
    
    private final Artifact name;
    private boolean load;
    private String condition;
    private String comment;
    private final Map<Object, GBeanOverride> gbeans = new LinkedHashMap<Object, GBeanOverride>();
    private ConditionParser parser = DEFAULT_COND_PARSER;

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

    public ConfigurationOverride(ModuleType module, JexlExpressionParser expressionParser) throws InvalidGBeanException {
        name = Artifact.create(module.getName());

        condition = module.getCondition();
        comment = module.getComment();
        load = module.isLoad();

        for (GbeanType gbeanType: module.getGbean()) {
            GBeanOverride gbean = new GBeanOverride(gbeanType, expressionParser);
            addGBean(gbean);
        }

        parser = new JexlConditionParser(expressionParser.getVariables());
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

    private boolean parseCondition() {
        if (condition == null) {
            // no condition means true
            return true;
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

    public ModuleType writeXml() {
        ModuleType module = new ModuleType();
        module.setName(name.toString());

        if (condition != null && condition.trim().length() != 0) {
            module.setCondition(condition);
        } else if (!load) {
            module.setLoad(false);
        }

        if (comment != null && comment.trim().length() > 0) {
            module.setComment(comment.trim());
        }

        // GBeans
        for (GBeanOverride gbeanOverride : gbeans.values()) {
            GbeanType gbean = gbeanOverride.writeXml();
            module.getGbean().add(gbean);
        }
        return module;
    }

}
