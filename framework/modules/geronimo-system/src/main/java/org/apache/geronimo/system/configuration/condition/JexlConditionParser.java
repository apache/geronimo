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
package org.apache.geronimo.system.configuration.condition;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a simple facility to evaluate condition expressions using the
 * <a href="http://jakarta.apache.org/commons/jexl">Jexl</a> language.
 *
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * @version $Rev$ $Date$
 */
public class JexlConditionParser
    implements ConditionParser
{
    private static final Logger log = LoggerFactory.getLogger(JexlConditionParser.class);

    private final Map<String, Object> vars;
    
    private final JexlEngine engine;

    public JexlConditionParser(final Map<String, Object> vars) {
        if (vars == null) {
            throw new IllegalArgumentException("vars");
        }
        this.vars = vars;
        engine = new JexlEngine();
    }
    
    public JexlConditionParser() {
        // Setup the default vars
        vars = new HashMap<String, Object>();
        ParserUtils.addDefaultVariables(vars);
        
        engine = new JexlEngine();
    }
    
    /**
     * Evaluate a condition expression.
     *
     * @param expression    The condition expression to evaluate; must not be null
     * @return              True if the condition is satisfied
     *
     * @throws org.apache.geronimo.system.configuration.condition.ConditionParserException     Failed to evaluate condition expression
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
            return (Boolean) result;
        }
        else {
            throw new ConditionParserException("Expression '" + expression + "' did not evaluate to a boolean value; found: " + result);
        }
    }

    private Object doEvaluate(final String expression) throws Exception {
        assert expression != null;

        log.debug("Evaluating expression: {}", expression);
        
        Expression expr = engine.createExpression(expression);

        JexlContext ctx = new MapContext(vars);

        Object result = expr.evaluate(ctx);
        log.debug("Result: {}", result);

        return result;
    }    
}
