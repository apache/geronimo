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

package org.apache.geronimo.connector.deployment;

import javax.management.ObjectName;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;

import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;

/**
 * ResourceAdapterHelperImpl
 *
 * @version $VERSION$ $DATE$
 */
public class ResourceAdapterHelperImpl implements GeronimoMBeanTarget, ResourceAdapterHelper {

    private GeronimoMBeanContext context;
    private ResourceAdapter resourceAdapter;
    
    private BootstrapContext bootstrapContext;
    static final String TARGET_NAME = "raHelper";

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#setMBeanContext(org.apache.geronimo.kernel.service.GeronimoMBeanContext)
     */
    public void setMBeanContext(GeronimoMBeanContext context) {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#canStart()
     */
    public boolean canStart() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#doStart()
     */
    public void doStart() {
        resourceAdapter = (ResourceAdapter)context.getTarget();  
        try {
            resourceAdapter.start(bootstrapContext);
        } catch (ResourceAdapterInternalException re) {
            throw new RuntimeException(re);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#canStop()
     */
    public boolean canStop() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#doStop()
     */
    public void doStop() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#doFail()
     */
    public void doFail() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.connector.deployment.ResourceAdapterHelper#registerManagedConnectionFactory(javax.resource.spi.ManagedConnectionFactory)
     */
    public void registerManagedConnectionFactory(ManagedConnectionFactory mcf) throws ResourceException {
        mcf.setResourceAdapter(resourceAdapter);
    }

    /**
     * @return Returns the bootstrapContext.
     */
    public BootstrapContext getBootstrapContext() {
        return bootstrapContext;
    }

    /**
     * @param bootstrapContext The bootstrapContext to set.
     */
    public void setBootstrapContext(BootstrapContext bootstrapContext) {
        this.bootstrapContext = bootstrapContext;
    }
    
	public static void addMBeanInfo(GeronimoMBeanInfo mbeanInfo,
									ObjectName bootstrapContextName) {
		mbeanInfo.setTargetClass(TARGET_NAME, ResourceAdapterHelperImpl.class.getName());
        
		mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("BootstrapContext", BootstrapContext.class.getName(), bootstrapContextName, true, TARGET_NAME));

	}

}
