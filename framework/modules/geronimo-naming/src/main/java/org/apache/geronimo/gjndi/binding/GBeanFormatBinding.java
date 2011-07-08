/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.gjndi.binding;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gjndi.KernelContextGBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Name;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = "Context")
public class GBeanFormatBinding extends KernelContextGBean {
    protected static final Logger log = LoggerFactory.getLogger(GBeanFormatBinding.class);
    private static final Pattern PATTERN = Pattern.compile("(\\{)(\\w+)(})");

    protected final String format;
    protected final Pattern namePattern;

    public GBeanFormatBinding(@ParamAttribute(name="format")String format,
                              @ParamAttribute(name="namePattern")String namePattern,
                              @ParamAttribute(name="nameInNamespace")String nameInNamespace,
                              @ParamAttribute(name="abstractNameQuery")AbstractNameQuery abstractNameQuery,
                              @ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel,
                              @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) throws NamingException {
        super(nameInNamespace, abstractNameQuery, kernel, bundleContext);
        this.format = format;
        if (namePattern != null && namePattern.length() > 0) {
            this.namePattern = Pattern.compile(namePattern);
        } else {
            this.namePattern = null;
        }
    }

    @Override
    protected Name createBindingName(AbstractName abstractName, Object value) throws NamingException {
        String name = abstractName.getNameProperty("name");
        if (namePattern != null) {
            Matcher matcher = namePattern.matcher(name);
            if (!matcher.matches()) {
                return null;
            }
        }
        Map<String, String> map = new HashMap<String, String>(abstractName.getName());
        Artifact artifact = abstractName.getArtifact();
        map.put("groupId", artifact.getGroupId());
        map.put("artifactId", artifact.getArtifactId());
        map.put("version", artifact.getVersion().toString());
        map.put("type", artifact.getType());
        String fullName = format(format, map);

        Name parsedName = getContext().getNameParser("").parse(getNameInNamespace()+"/" + fullName);

        // create intermediate contexts
        for (int i = 1; i < parsedName.size(); i++) {
            Name contextName = parsedName.getPrefix(i);
            if (!bindingExists(getContext(), contextName)) {
                getContext().createSubcontext(contextName);
            }
        }
        return parsedName;
    }

    static String format(String input, Map<String, String> map) {
        Matcher matcher = PATTERN.matcher(input);
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(2);
            String value = map.get(key);
            matcher.appendReplacement(buf, value);
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

}
