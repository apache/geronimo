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
package org.apache.geronimo.deployment.model.geronimo.ejb;

/**
 * JavaBean for the geronimo-ejb-jar.xml tag enterprise-beans
 *
 * @version $Revision: 1.2 $ $Date: 2003/09/17 01:47:14 $
 */
public class EnterpriseBeans extends org.apache.geronimo.deployment.model.ejb.EnterpriseBeans {
    public void setEntity(int i, org.apache.geronimo.deployment.model.ejb.Entity bean) {
        assert (bean instanceof Entity);
        super.setEntity(i, bean);
    }

    public void setEntity(org.apache.geronimo.deployment.model.ejb.Entity[] bean) {
        assert (bean instanceof Entity[]);
        super.setEntity(bean);
    }

    public Entity getGeronimoEntity(int i) {
        return (Entity)super.getEntity(i);
    }

    public Entity[] getGeronimoEntity() {
        return (Entity[])super.getEntity();
    }

    public void setGeronimoEntity(int i, Entity bean) {
        super.setEntity(i, bean);
    }

    public void setGeronimoEntity(Entity[] bean) {
        super.setEntity(bean);
    }

    public void setSession(int i, org.apache.geronimo.deployment.model.ejb.Session bean) {
        assert (bean instanceof Session);
        super.setSession(i, bean);
    }

    public void setSession(org.apache.geronimo.deployment.model.ejb.Session[] bean) {
        assert (bean instanceof Session[]);
        super.setSession(bean);
    }

    public Session[] getGeronimoSession() {
        return (Session[])super.getSession();
    }

    public Session getGeronimoSession(int i) {
        return (Session)super.getSession(i);
    }

    public void setGeronimoSession(int i, Session bean) {
        super.setSession(i, bean);
    }

    public void setGeronimoSession(Session[] bean) {
        super.setSession(bean);
    }

    public void setMessageDriven(int i, org.apache.geronimo.deployment.model.ejb.MessageDriven bean) {
        assert (bean instanceof MessageDriven);
        super.setMessageDriven(i, bean);
    }

    public void setMessageDriven(org.apache.geronimo.deployment.model.ejb.MessageDriven[] bean) {
        assert (bean instanceof MessageDriven[]);
        super.setMessageDriven(bean);
    }

    public MessageDriven[] getGeronimoMessageDriven() {
        return (MessageDriven[])super.getMessageDriven();
    }

    public MessageDriven getGeronimoMessageDriven(int i) {
        return (MessageDriven)super.getMessageDriven(i);
    }

    public void setGeronimoMessageDriven(int i, MessageDriven bean) {
        super.setMessageDriven(i, bean);
    }

    public void setGeronimoMessageDriven(MessageDriven[] bean) {
        super.setMessageDriven(bean);
    }
}
