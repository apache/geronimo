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

package org.apache.geronimo.activemq;

import java.util.Properties;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Samples: 1. ${a}
 *          2. ${a${a}}
 *          3. ${${a} + 10}
 */
public class GeronimoPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    @Override
    protected String resolveSystemProperty(String placeholder) {
        String sPropertyValue = super.resolveSystemProperty(placeholder);
        if (sPropertyValue == null) {
            sPropertyValue = parseSystemProperty(placeholder);
            //TODO No way to get the SearchSystemEnvironment property
            if (sPropertyValue == null)
                sPropertyValue = parseEnvProperty(placeholder);
        }
        return sPropertyValue;
    }

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {
        String sPropertyValue = super.resolvePlaceholder(placeholder, props);
        if (sPropertyValue == null) {
            try {
                Expression expression = ExpressionFactory.createExpression(placeholder);
                JexlContext jexlContext = JexlHelper.createContext();
                jexlContext.setVars(props);
                sPropertyValue = expression.evaluate(jexlContext).toString();
            } catch (Throwable t) {
            }
        }
        return sPropertyValue;
    }

    private String parseSystemProperty(String placeholder) {
        try {
            Expression expression = ExpressionFactory.createExpression(placeholder);
            JexlContext jexlContext = JexlHelper.createContext();
            jexlContext.setVars(System.getProperties());
            return expression.evaluate(jexlContext).toString();
        } catch (Throwable t) {
            return null;
        }
    }

    private String parseEnvProperty(String placeholder) {
        try {
            Expression expression = ExpressionFactory.createExpression(placeholder);
            JexlContext jexlContext = JexlHelper.createContext();
            jexlContext.setVars(System.getenv());
            return expression.evaluate(jexlContext).toString();
        } catch (Throwable t) {
            return null;
        }
    }
}
