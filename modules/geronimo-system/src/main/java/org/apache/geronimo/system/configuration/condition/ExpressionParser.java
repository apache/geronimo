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

import java.util.Map;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.resolver.FlatResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parses expressions using <a href="http://jakarta.apache.org/commons/jexl/">Commons Jexl</a>.
 *
 * @version $Rev$ $Date$
 */
public class ExpressionParser
{
    private static final Log log = LogFactory.getLog(ExpressionParser.class);

    protected JexlContext context;

    public ExpressionParser(final Map vars) {
        assert vars != null;

        context = JexlHelper.createContext();
        context.setVars(vars);

        if (log.isTraceEnabled()) {
            log.trace("Using variables: " + context.getVars());
        }
    }

    public ExpressionParser() {
        this(System.getProperties());
    }

    public Map getVariables() {
        return context.getVars();
    }

    public Object getVariable(final Object name) {
        assert name != null;

        return getVariables().get(name);
    }

    public Object setVariable(final Object name, final Object value) {
        assert name != null;

        return getVariables().put(name, value);
    }

    public Object unsetVariable(final Object name) {
        assert name != null;

        return getVariables().remove(name);
    }

    public void addVariables(final Map map) {
        assert map != null;

        getVariables().putAll(map);
    }

    private FlatResolver resolver = new FlatResolver(true);

    protected Expression createExpression(final String expression) throws Exception {
        // assert expression != null;

        Expression expr = ExpressionFactory.createExpression(expression);
        expr.addPreResolver(resolver);

        return expr;
    }

    public Object evaluate(final String expression) throws Exception {
        assert expression != null;

        boolean trace = log.isTraceEnabled();
        if (trace) {
            log.trace("Evaluating expression: " + expression);
        }

        Expression expr = createExpression(expression);
        Object obj = expr.evaluate(context);
        if (trace) {
            log.trace("Result: " + obj);
        }

        return obj;
    }

    public String parse(final String input) {
        assert input != null;

        boolean trace = log.isTraceEnabled();
        if (trace) {
            log.trace("Parsing input: " + input);
        }

        StringBuffer buff = new StringBuffer();

        int cur = 0;
        int prefixLoc;
        int suffixLoc;

        while (cur < input.length()) {
            prefixLoc = input.indexOf("${", cur);

            if (prefixLoc < 0) {
                break;
            }

            suffixLoc = input.indexOf("}", prefixLoc);
            if (suffixLoc < 0) {
                throw new RuntimeException("Missing '}': " + input);
            }

            String expr = input.substring(prefixLoc + 2, suffixLoc);
            buff.append(input.substring(cur, prefixLoc));

            try {
                buff.append(evaluate(expr));
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to evaluate: " + expr, e);
            }

            cur = suffixLoc + 1;
        }

        buff.append(input.substring(cur));

        if (trace) {
            log.trace("Parsed result: " + buff);
        }

        return buff.toString();
    }

    public String parse(final String input, final boolean trim) {
        String output = parse(input);
        if (trim && output != null) {
            output = output.trim();
        }

        return output;
    }
}
