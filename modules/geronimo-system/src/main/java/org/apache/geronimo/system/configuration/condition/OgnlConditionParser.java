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

package org.apache.geronimo.system.configuration.condition;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ognl.Ognl;
import ognl.OgnlContext;

/**
 * Provides a simple facility to evaluate condition expressions using the
 * <a href="http://ognl.org">OGNL</a> language.
 *
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * @version $Rev$ $Date$
 */
public class OgnlConditionParser
    implements ConditionParser
{
    private static final Log log = LogFactory.getLog(OgnlConditionParser.class);
    
    private final Map vars;
    
    public OgnlConditionParser() {
        // Setup the default vars
        vars = new HashMap();
        
        vars.put("java", new JavaVariable());
        vars.put("os", new OsVariable());
        
        // Install properties (to allow getProperty(x,y) to be used for defaults
        // Using nested defaults to avoid modifications to system props in expresssion
        vars.put("props", new Properties(System.getProperties()));
    }
    
    /**
     * Evaluate a condition expression.
     *
     * @param expression    The condition expression to evaluate; must not be null
     * @return              True if the condition is satisfied
     *
     * @throws ConditionParserException     Failed to evaluate condition expression
     */
    public boolean evaluate(final String expression) throws ConditionParserException {
        if (expression == null) {
            throw new IllegalArgumentException("Expression must not be null");
        }
        
        // Empty expressions are true
        if (expression.trim().length() == 0) {
            log.debug("Expression is empty; skipping evaluation");
            
            return true;
        }

        Object result;
        try {
            result = doEvaluate(expression);
        }
        catch (Exception e) {
            throw new ConditionParserException("Failed to evaluate expression: " + expression, e);
        }

        if (result instanceof Boolean) {
            return ((Boolean)result).booleanValue();
        }
        else {
            throw new ConditionParserException("Expression '" + expression + "' did not evaluate to a boolean value; found: " + result);
        }
    }
    
    private Object doEvaluate(final String expression) throws Exception {
        assert expression != null;

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("Evaluating expression: " + expression);
        }

        // Object root;
        OgnlContext context = new OgnlContext(vars);

        Object expr = Ognl.parseExpression(expression);
        Object result = Ognl.getValue(expr, context);
        
        if (debug) {
            log.debug("Result: " + result);
        }

        return result;
    }
}
