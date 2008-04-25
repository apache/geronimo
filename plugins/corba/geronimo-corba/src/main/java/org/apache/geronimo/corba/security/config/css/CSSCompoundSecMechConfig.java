/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.security.config.css;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.UserException;
import org.omg.CSI.EstablishContext;
import org.omg.CSI.SASContextBody;
import org.omg.CSI.SASContextBodyHelper;
import org.omg.IOP.SecurityAttributeService;
import org.omg.IOP.ServiceContext;

import org.apache.geronimo.corba.security.config.ConfigUtil;
import org.apache.geronimo.corba.security.config.tss.TSSCompoundSecMechConfig;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Rev: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public class CSSCompoundSecMechConfig implements Serializable {

    private final static Logger log = LoggerFactory.getLogger(CSSCompoundSecMechConfig.class);

    private short supports;
    private short requires;
    private CSSTransportMechConfig transport_mech;
    private CSSASMechConfig as_mech;
    private CSSSASMechConfig sas_mech;

    public CSSTransportMechConfig getTransport_mech() {
        return transport_mech;
    }

    public void setTransport_mech(CSSTransportMechConfig transport_mech) {
        this.transport_mech = transport_mech;
        this.supports |= transport_mech.getSupports();
        this.requires |= transport_mech.getRequires();
    }

    public CSSASMechConfig getAs_mech() {
        return as_mech;
    }

    public void setAs_mech(CSSASMechConfig as_mech) {
        this.as_mech = as_mech;
        this.supports |= as_mech.getSupports();
        this.requires |= as_mech.getRequires();
    }

    public CSSSASMechConfig getSas_mech() {
        return sas_mech;
    }

    public void setSas_mech(CSSSASMechConfig sas_mech) {
        this.sas_mech = sas_mech;
        this.supports |= sas_mech.getSupports();
        this.requires |= sas_mech.getRequires();
    }

    public boolean canHandle(TSSCompoundSecMechConfig requirement) {

        if (log.isDebugEnabled()) {
            log.debug("canHandle()");
            log.debug("    CSS SUPPORTS: " + ConfigUtil.flags(supports));
            log.debug("    CSS REQUIRES: " + ConfigUtil.flags(requires));
            log.debug("    TSS SUPPORTS: " + ConfigUtil.flags(requirement.getSupports()));
            log.debug("    TSS REQUIRES: " + ConfigUtil.flags(requirement.getRequires()));
        }

        if ((supports & requirement.getRequires()) != requirement.getRequires()) return false;
        if ((requires & requirement.getSupports()) != requires) return false;

        if (!transport_mech.canHandle(requirement.getTransport_mech())) return false;
        if (!as_mech.canHandle(requirement.getAs_mech())) return false;
        if (!sas_mech.canHandle(requirement.getSas_mech())) return false;

        return true;
    }

    public ServiceContext generateServiceContext() throws UserException {

        if (as_mech instanceof CSSNULLASMechConfig && sas_mech.getIdentityToken() instanceof CSSSASITTAbsent) return null;

        EstablishContext msg = new EstablishContext();

        msg.client_context_id = 0;
        msg.client_authentication_token = as_mech.encode();
        msg.authorization_token = sas_mech.encodeAuthorizationElement();
        msg.identity_token = sas_mech.encodeIdentityToken();

        ServiceContext context = new ServiceContext();

        SASContextBody sas = new SASContextBody();
        sas.establish_msg(msg);
        Any sas_any = Util.getORB().create_any();
        SASContextBodyHelper.insert(sas_any, sas);
        context.context_data = Util.getCodec().encode_value(sas_any);

        context.context_id = SecurityAttributeService.value;

        return context;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString("", buf);
        return buf.toString();
    }

    void toString(String spaces, StringBuffer buf) {
        String moreSpaces = spaces + "  ";
        buf.append(spaces).append("CSSCompoundSecMechConfig: [\n");
        buf.append(moreSpaces).append("SUPPORTS: ").append(ConfigUtil.flags(supports)).append("\n");
        buf.append(moreSpaces).append("REQUIRES: ").append(ConfigUtil.flags(requires)).append("\n");
        transport_mech.toString(moreSpaces, buf);
        as_mech.toString(moreSpaces, buf);
        sas_mech.toString(moreSpaces, buf);
        buf.append(spaces).append("]\n");
    }
    
}
