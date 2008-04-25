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
package org.apache.geronimo.corba.security.config.tss;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import javax.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.TAG_NULL_TAG;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANSHelper;
import org.omg.CSIIOP.TransportAddress;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;

import org.apache.geronimo.corba.security.SASException;
import org.apache.geronimo.corba.security.config.ConfigUtil;


/**
 * At the moment, this config class can only handle a single address.
 *
 * @version $Rev: 504461 $ $Date: 2007-02-07 00:42:26 -0800 (Wed, 07 Feb 2007) $
 */
public class TSSSSLTransportConfig extends TSSTransportMechConfig {

    private final static Logger log = LoggerFactory.getLogger(TSSSSLTransportConfig.class);

    private short port;
    private String hostname;
    private short handshakeTimeout = -1;
    private short supports;
    private short requires;

    public TSSSSLTransportConfig() {
    }

    public TSSSSLTransportConfig(TaggedComponent component, Codec codec) throws UserException {
        Any any = codec.decode_value(component.component_data, TLS_SEC_TRANSHelper.type());
        TLS_SEC_TRANS tst = TLS_SEC_TRANSHelper.extract(any);

        supports = tst.target_supports;
        requires = tst.target_requires;
        port = tst.addresses[0].port;
        hostname = tst.addresses[0].host_name;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public short getHandshakeTimeout() {
        return handshakeTimeout;
    }

    public void setHandshakeTimeout(short handshakeTimeout) {
        this.handshakeTimeout = handshakeTimeout;
    }

    public short getSupports() {
        return supports;
    }

    public void setSupports(short supports) {
        this.supports = supports;
    }

    public short getRequires() {
        return requires;
    }

    public void setRequires(short requires) {
        this.requires = requires;
    }

    public TaggedComponent encodeIOR(ORB orb, Codec codec) {
        TaggedComponent result = new TaggedComponent();

        TLS_SEC_TRANS tst = new TLS_SEC_TRANS();

        tst.target_supports = supports;
        tst.target_requires = requires;
        tst.addresses = new TransportAddress[1];
        tst.addresses[0] = new TransportAddress(hostname, port);

        try {
            Any any = orb.create_any();
            TLS_SEC_TRANSHelper.insert(any, tst);

            result.tag = TAG_TLS_SEC_TRANS.value;
            result.component_data = codec.encode_value(any);
        } catch (Exception ex) {
            log.error("Error enncoding transport tagged component, defaulting encoding to NULL");

            result.tag = TAG_NULL_TAG.value;
            result.component_data = new byte[0];
        }

        return result;
    }

    public Subject check(SSLSession session) throws SASException {
        if (session == null && requires != 0) throw new NO_PERMISSION("Missing required SSL session");

        try {
            if (log.isDebugEnabled()) log.debug("Scraping principal from SSL session");

            X509Certificate link = session.getPeerCertificateChain()[0];
            Subject subject = new Subject();
            String name = link.getSubjectDN().toString();

            if (log.isDebugEnabled()) log.debug("Obtained principal " + name);

            subject.getPrincipals().add(new X500Principal(name));

            return subject;
        } catch (SSLPeerUnverifiedException e) {
            if ((requires & EstablishTrustInClient.value) != 0) {
                if (log.isDebugEnabled()) log.debug("Unverified peer, throwing exception");
                throw new SASException(1, e);
            }
            if (log.isDebugEnabled()) log.debug("Unverified peer, returning null");
            return null;
        }
    }

    void toString(String spaces, StringBuffer buf) {
        String moreSpaces = spaces + "  ";
        buf.append(spaces).append("TSSSSLTransportConfig: [\n");
        buf.append(moreSpaces).append("SUPPORTS: ").append(ConfigUtil.flags(supports)).append("\n");
        buf.append(moreSpaces).append("REQUIRES: ").append(ConfigUtil.flags(requires)).append("\n");
        buf.append(moreSpaces).append("port    : ").append(port).append("\n");
        buf.append(moreSpaces).append("hostName: ").append(hostname).append("\n");
        buf.append(moreSpaces).append("handshakeTimeout: ").append(handshakeTimeout).append("\n");
       buf.append(spaces).append("]\n");
    }

}
