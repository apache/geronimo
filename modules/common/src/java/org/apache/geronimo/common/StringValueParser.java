/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.common;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.resolver.FlatResolver;

/**
 * Handles pasring expressions from a string.
 *
 * @version <code>$Revision: 1.1 $ $Date: 2003/08/24 20:13:22 $</code>
 */
public class StringValueParser
{
    private static final Log log = LogFactory.getLog(StringValueParser.class);
    
    protected JexlContext context;
    
    public StringValueParser(final Map vars)
    {
        if (vars == null) {
            throw new NullArgumentException("vars");
        }
        
        context = JexlHelper.createContext();
        context.setVars(vars);
        
        if (log.isTraceEnabled()) {
            log.trace("Using variables: " + context.getVars());
        }
    }
    
    public StringValueParser()
    {
        this(System.getProperties());
    }
    
    public Map getVariables()
    {
        return context.getVars();
    }
    
    public Object getVariable(final Object name)
    {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        return getVariables().get(name);
    }
    
    public Object setVariable(final Object name, final Object value)
    {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        return getVariables().put(name, value);
    }
    
    public Object unsetVariable(final Object name)
    {
        if (name == null) {
            throw new NullArgumentException("name");
        }
        
        return getVariables().remove(name);
    }
    
    public void addVariables(final Map map)
    {
        if (map == null) {
            throw new NullArgumentException("map");
        }
        
        getVariables().putAll(map);
    }
    
    private FlatResolver resolver = new FlatResolver(true);
    
    protected Expression createExpression(final String expression) throws Exception
    {
        assert expression != null;
        Expression expr = ExpressionFactory.createExpression(expression);
        expr.addPreResolver(resolver);
        return expr;
    }
    
    public Object evaluate(final String expression) throws Exception
    {
        if (expression == null) {
            throw new NullArgumentException("expression");
        }
        
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
    
    public String parse(final String input)
    {
        if (input == null) {
            throw new NullArgumentException("input");
        }
        
        boolean trace = log.isTraceEnabled();
        if (trace) {
            log.trace("Parsing input: " + input);
        }
        
        StringBuffer buff = new StringBuffer();

        int cur = 0;
        int prefixLoc = 0;
        int suffixLoc = 0;

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
}
