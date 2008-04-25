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
package org.apache.geronimo.corba.security;

import java.util.Set;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Subject;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.ORB;
import org.omg.CSI.CompleteEstablishContext;
import org.omg.CSI.ContextError;
import org.omg.CSI.MTCompleteEstablishContext;
import org.omg.CSI.MTContextError;
import org.omg.CSI.MTEstablishContext;
import org.omg.CSI.MTMessageInContext;
import org.omg.CSI.SASContextBody;
import org.omg.CSI.SASContextBodyHelper;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.SecurityAttributeService;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geronimo.security.ContextManager;

import org.apache.geronimo.corba.security.config.tss.TSSConfig;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Revision: 482212 $ $Date: 2006-12-04 07:16:03 -0800 (Mon, 04 Dec 2006) $
 */
final class ServerSecurityInterceptor extends LocalObject implements ServerRequestInterceptor {

    private final Logger log = LoggerFactory.getLogger(ServerSecurityInterceptor.class);

    public ServerSecurityInterceptor() {

        if (log.isDebugEnabled()) log.debug("<init>");
    }

    public void receive_request(ServerRequestInfo ri) {

        Subject identity = null;
        long contextId = 0;

        if (log.isDebugEnabled()) log.debug("receive_request(" + ri.operation() + " [" + new String(ri.object_id()) + "] ");
        ClassLoader savedCL = Thread.currentThread().getContextClassLoader();
        try {
            ServerPolicy serverPolicy = (ServerPolicy) ri.get_server_policy(ServerPolicyFactory.POLICY_TYPE);
            if (serverPolicy == null) return;

            TSSConfig tssPolicy = serverPolicy.getConfig();
            if (tssPolicy == null) return;

            if (serverPolicy.getClassloader() != null) Thread.currentThread().setContextClassLoader(serverPolicy.getClassloader());

            if (log.isDebugEnabled()) log.debug("Found server policy");

            ServiceContext serviceContext = ri.get_request_service_context(SecurityAttributeService.value);
            if (serviceContext == null) return;

            if (log.isDebugEnabled()) log.debug("Found service context");

            Any any = Util.getCodec().decode_value(serviceContext.context_data, SASContextBodyHelper.type());
            SASContextBody contextBody = SASContextBodyHelper.extract(any);

            short msgType = contextBody.discriminator();
            switch (msgType) {
                case MTEstablishContext.value:
                    if (log.isDebugEnabled()) log.debug("   EstablishContext");

                    contextId = contextBody.establish_msg().client_context_id;

                    identity = tssPolicy.check(SSLSessionManager.getSSLSession(ri.request_id()), contextBody.establish_msg());
                    if (identity != null) {
                        ContextManager.registerSubject(identity);
                    }

                    SASReplyManager.setSASReply(ri.request_id(), generateContextEstablished(identity, contextId, false));

                    break;

                case MTCompleteEstablishContext.value:
                    log.error("The CSIv2 TSS is not supposed to receive a CompleteEstablishContext message.");
                    throw new INTERNAL("The CSIv2 TSS is not supposed to receive a CompleteEstablishContext message.");

                case MTContextError.value:
                    log.error("The CSIv2 TSS is not supposed to receive a ContextError message.");
                    throw new INTERNAL("The CSIv2 TSS is not supposed to receive a ContextError message.");

                case MTMessageInContext.value:
                    log.error("The CSIv2 TSS is not supposed to receive a MessageInContext message.");

                    contextId = contextBody.in_context_msg().client_context_id;
                    throw new SASNoContextException();
            }
        } catch (BAD_PARAM e) {
            if (log.isDebugEnabled()) log.debug("No security service context found");
        } catch (INV_POLICY e) {
            if (log.isDebugEnabled()) log.debug("INV_POLICY");
        } catch (TypeMismatch tm) {
            log.error("TypeMismatch thrown", tm);
            throw (MARSHAL)new MARSHAL("TypeMismatch thrown: " + tm).initCause(tm);
        } catch (FormatMismatch fm) {
            log.error("FormatMismatch thrown", fm);
            throw (MARSHAL)new MARSHAL("FormatMismatch thrown: " + fm).initCause(fm);
        } catch (SASException e) {
            log.error("SASException", e);
            SASReplyManager.setSASReply(ri.request_id(), generateContextError(e, contextId));
            // rethrowing this requires some special handling.  If the root exception is a
            // RuntimeException, then we can just rethrow it.  Otherwise we need to turn this into
            // a RuntimeException.
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            }
            else {
                throw new RuntimeException(cause.getMessage(), cause);
            }
        } catch (Exception e) {
            log.error("Exception", e);
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            }
            else {
                throw new RuntimeException(cause.getMessage(), cause);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(savedCL);
        }

        if (log.isDebugEnabled()) log.debug("   " + identity);

        if (identity != null) {
            ContextManager.setCallers(identity, identity);

            SubjectManager.setSubject(ri.request_id(), identity);
        }
        else 
        {
            // if there's no identity given, make sure we clear this 
            // to ensure that the default subject ends up getting used. 
            ContextManager.clearCallers(); 
            // and just to be on the safe side, make sure there's no 
            // subject registered for this request. 
            SubjectManager.clearSubject(ri.request_id());
        }
    }

    public void receive_request_service_contexts(ServerRequestInfo ri) {
        if (log.isDebugEnabled()) log.debug("receive_request_service_contexts()");
    }

    public void send_exception(ServerRequestInfo ri) {
        Subject identity = SubjectManager.clearSubject(ri.request_id());
        if (identity != null) {
            ContextManager.unregisterSubject(identity);
            ContextManager.clearCallers(); 
        }

        insertServiceContext(ri);

        if (log.isDebugEnabled()) log.debug("send_exception()");
    }

    public void send_other(ServerRequestInfo ri) {
        if (log.isDebugEnabled()) log.debug("send_other()");
    }

    public void send_reply(ServerRequestInfo ri) {
        Subject identity = SubjectManager.clearSubject(ri.request_id());
        if (identity != null) {
            ContextManager.unregisterSubject(identity);
            ContextManager.clearCallers(); 
        }

        insertServiceContext(ri);

        if (log.isDebugEnabled()) log.debug("send_reply()");
    }

    public void destroy() {
        if (log.isDebugEnabled()) log.debug("destroy()");
    }

    public String name() {
        return "org.apache.geronimo.corba.security.ServerSecurityInterceptor";
    }

    protected SASContextBody generateContextError(SASException e, long contextId) {
        SASContextBody reply = new SASContextBody();

        reply.error_msg(new ContextError(contextId, e.getMajor(), e.getMinor(), e.getErrorToken()));

        return reply;
    }

    protected SASContextBody generateContextEstablished(Subject identity, long contextId, boolean stateful) {
        byte[] finalContextToken = null;
        if (identity != null) {
            Set credentials = identity.getPrivateCredentials(FinalContextToken.class);
            if (!credentials.isEmpty()) {
                try {
                    FinalContextToken token = (FinalContextToken) credentials.iterator().next();
                    finalContextToken = token.getToken();
                    token.destroy();
                } catch (DestroyFailedException e) {
                    // do nothing
                }
            }
        }
        if (finalContextToken == null) {
            finalContextToken = new byte[0];
        }

        SASContextBody reply = new SASContextBody();
        reply.complete_msg(new CompleteEstablishContext(contextId, stateful, finalContextToken));
        return reply;
    }

    protected void insertServiceContext(ServerRequestInfo ri) {
        try {
            SASContextBody sasContextBody = SASReplyManager.clearSASReply(ri.request_id());
            if (sasContextBody != null) {
                Any any = ORB.init().create_any();
                SASContextBodyHelper.insert(any, sasContextBody);
                ri.add_reply_service_context(new ServiceContext(SecurityAttributeService.value, Util.getCodec().encode_value(any)), true);
            }
        } catch (InvalidTypeForEncoding itfe) {
            log.error("InvalidTypeForEncoding thrown", itfe);
            throw (INTERNAL)new INTERNAL("InvalidTypeForEncoding thrown: " + itfe).initCause(itfe);
        }
    }
}
