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
package org.apache.geronimo.ejb.cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;

import org.apache.geronimo.cache.InstanceFactory;
import org.apache.geronimo.core.service.AbstractManagedComponent;
import org.apache.geronimo.core.service.RPCContainer;
import org.apache.geronimo.ejb.EnterpriseContext;
import org.apache.geronimo.ejb.GeronimoSessionContext;
import org.apache.geronimo.ejb.SimpleEnterpriseContext;
import org.apache.geronimo.ejb.container.EJBPlugins;

/**
 *
 *
 *
 * @version $Revision: 1.8 $ $Date: 2003/11/01 22:49:33 $
 */
public class StatelessInstanceFactory extends AbstractManagedComponent implements InstanceFactory {
    private Class beanClass;
    private Method ejbCreate;

    protected void doStart() throws Exception {
        super.doStart();
        beanClass = EJBPlugins.getEJBMetadata((RPCContainer)getContainer()).getBeanClass();
        ejbCreate = beanClass.getMethod("ejbCreate", null);
    }

    protected void doStop() throws Exception {
        beanClass = null;
        ejbCreate = null;
        super.doStop();
    }

    public Object createInstance() throws Exception {
        // create the instance
        SessionBean instance = (SessionBean) beanClass.newInstance();

        // initialize the instance
        instance.setSessionContext(new GeronimoSessionContext((RPCContainer)getContainer()));
        try {
            ejbCreate.invoke(instance, null);
        } catch (IllegalAccessException e) {
            // This method is using the Java language access control and the
            // underlying method is inaccessible.
            throw new EJBException(e);
        } catch (InvocationTargetException e) {
            // unwrap the exception
            Throwable t = e.getTargetException();
            if (t instanceof Exception) {
                throw (Exception) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new Error("Unexpected Throwable", t);
            }
        }

        // wrape the instance in an enterprise context
        EnterpriseContext enterpriseContext = new SimpleEnterpriseContext();
        enterpriseContext.setInstance(instance);
        return enterpriseContext;
    }

    public void destroyInstance(Object instance) {
        throw new UnsupportedOperationException();
    }
}
