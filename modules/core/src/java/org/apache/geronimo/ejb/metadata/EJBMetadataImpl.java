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
package org.apache.geronimo.ejb.metadata;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * 
 * 
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:51:54 $
 */
public final class EJBMetadataImpl implements EJBMetadata {
    private String name;
    private ClassLoader classLoader;
    private Class beanClass;
    private Class homeInterface;
    private Class remoteInterface;
    private Class localHomeInterface;
    private Class localInterface;
    private boolean reentrant;
    private CommitOption commitOption;
    private TransactionDemarcation transactionDemarcation;
    private Map methodMetadataMap = new HashMap();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public Class getHomeInterface() {
        return homeInterface;
    }

    public void setHomeInterface(Class homeInterface) {
        this.homeInterface = homeInterface;
    }

    public Class getRemoteInterface() {
        return remoteInterface;
    }

    public void setRemoteInterface(Class remoteInterface) {
        this.remoteInterface = remoteInterface;
    }

    public Class getLocalHomeInterface() {
        return localHomeInterface;
    }

    public void setLocalHomeInterface(Class localHomeInterface) {
        this.localHomeInterface = localHomeInterface;
    }

    public Class getLocalInterface() {
        return localInterface;
    }

    public void setLocalInterface(Class localInterface) {
        this.localInterface = localInterface;
    }

    public boolean isReentrant() {
        return reentrant;
    }

    public void setReentrant(boolean reentrant) {
        this.reentrant = reentrant;
    }

    public CommitOption getCommitOption() {
        return commitOption;
    }

    public void setCommitOption(CommitOption commitOption) {
        this.commitOption = commitOption;
    }

    public TransactionDemarcation getTransactionDemarcation() {
        return transactionDemarcation;
    }

    public void setTransactionDemarcation(TransactionDemarcation transactionDemarcation) {
        this.transactionDemarcation = transactionDemarcation;
    }

    public MethodMetadata getMethodMetadata(Method method) {
        return (MethodMetadata) methodMetadataMap.get(method);
    }

    public void putMethodMetadata(Method method, MethodMetadata methodMetadata) {
        methodMetadataMap.put(method, methodMetadata);
    }
}
