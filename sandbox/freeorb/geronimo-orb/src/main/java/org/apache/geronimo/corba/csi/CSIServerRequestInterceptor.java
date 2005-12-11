/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.corba.csi;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.x500.X500Principal;

import org.omg.CORBA.Any;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.OctetSeqHelper;
import org.omg.CORBA.UserException;
import org.omg.CSI.*;
import org.omg.GSSUP.InitialContextToken;
import org.omg.IOP.Codec;
import org.omg.IOP.SecurityAttributeService;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.Security.DelegationDirective;
import org.omg.Security.RequiresSupports;
import org.omg.Security.SecDelegationDirectivePolicy;
import org.omg.SecurityLevel2.DelegationDirectivePolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.corba.csi.gssup.GSSUPPolicy;
import org.apache.geronimo.corba.csi.gssup.SecGSSUPPolicy;


public class CSIServerRequestInterceptor extends CSIInterceptorBase
        implements org.omg.PortableInterceptor.ServerRequestInterceptor
{

    CSIServerRequestInterceptor(Codec codec) {
        super(codec);
    }

    private static final Log log = LogFactory
            .getLog(CSIServerRequestInterceptor.class);

    //
    // SERVER REQUEST API
    //

    public void receive_request_service_contexts(ServerRequestInfo ri)
            throws ForwardRequest
    {

        if (log.isDebugEnabled()) {
            log.debug("receive_request_service_contexts " + ri.operation());
        }

        if (CallStatus.peekIsLocal()) {
            if (log.isDebugEnabled()) {
                log.debug("local call");
            }

            return;
        }

        // set null subject so that we won't run in context of some
        // previous subject
        // CSISubjectInfo.clear ();

        boolean support_gssup_authorization = false;
        boolean require_gssup_authorization = false;

        String gssup_domain = null;

        // if there is no GSSUP policy on this POA, then we won't try
        // to validate the user.
        try {
            GSSUPPolicy gp = (GSSUPPolicy) ri
                    .get_server_policy(SecGSSUPPolicy.value);

            if (gp == null) {

                if (log.isDebugEnabled()) {
                    log.debug("null GSSUPPolicy");
                }

            } else {
                support_gssup_authorization = true;

                if (gp.mode() == RequiresSupports.SecRequires) {
                    require_gssup_authorization = true;
                }

                gssup_domain = gp.domain();
            }

        }
        catch (org.omg.CORBA.INV_POLICY ex) {

            if (log.isDebugEnabled()) {
                log.debug("no GSSUPPolicy");
            }
        }

        boolean support_gssup_principal_identity = false;

        try {
            DelegationDirectivePolicy delegate = (DelegationDirectivePolicy) ri
                    .get_server_policy(SecDelegationDirectivePolicy.value);
            if (delegate != null) {
                DelegationDirective dir = delegate.delegation_directive();
                if (dir == DelegationDirective.Delegate) {
                    support_gssup_principal_identity = true;
                }
            }
        }
        catch (org.omg.CORBA.INV_POLICY ex) {
            // ignore //
        }

        if (log.isDebugEnabled()) {
            log.debug("support gssup authorization: "
                      + support_gssup_authorization);
            log.debug("require gssup authorization: "
                      + require_gssup_authorization);
            log.debug("support gssup identity: "
                      + support_gssup_principal_identity);
        }

        ServiceContext serviceContext;
        try {
            serviceContext = ri
                    .get_request_service_context(SecurityAttributeService.value);
        }
        catch (org.omg.CORBA.BAD_PARAM ex) {
            serviceContext = null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Received request service context: " + serviceContext);
        }

        if (require_gssup_authorization && serviceContext == null) {
            throw new org.omg.CORBA.NO_PERMISSION(
                    "GSSUP authorization required"
                    + " (missing SAS EstablishContext message)");
        }

        SASContextBody sasBody = null;

        if (serviceContext != null) {
            sasBody = decodeSASContextBody(serviceContext);

            if (log.isDebugEnabled()) {
                log
                        .debug("received request of type "
                               + sasBody.discriminator());
            }

            switch (sasBody.discriminator()) {
                case MTCompleteEstablishContext.value:
                case MTContextError.value:
                    // Unexpected
                    log.error("Unexpected message of type "
                              + sasBody.discriminator());
                    throw new org.omg.CORBA.NO_PERMISSION("unexpected SAS message");

                case MTMessageInContext.value:
                    if (log.isDebugEnabled()) {
                        log.debug("MTMessageInContext");
                    }

                    throw new org.omg.CORBA.NO_PERMISSION(
                            "Stateful SAS not supported");

                case MTEstablishContext.value:
                    if (log.isDebugEnabled()) {
                        log.debug("MTEstablishContext");
                    }
                    acceptContext(ri, sasBody.establish_msg(),
                                  support_gssup_authorization,
                                  require_gssup_authorization,
                                  support_gssup_principal_identity, gssup_domain);
                    break;
            }
        }
    }

    public void receive_request(ServerRequestInfo ri) throws ForwardRequest {
    }

    public void send_reply(ServerRequestInfo ri) {
        if (CallStatus.peekIsLocal()) {
            return;
        }
    }

    public void send_exception(ServerRequestInfo ri) throws ForwardRequest {
        send_reply(ri);
    }

    public void send_other(ServerRequestInfo ri) throws ForwardRequest {
        send_reply(ri);
    }

    public String name() {
        return "CSI Server Interceptor";
    }


    void acceptContext(ServerRequestInfo ri, EstablishContext establishMsg,
                       boolean support_gssup_authorization,
                       boolean require_gssup_authorization,
                       boolean support_gssup_principal_identity, String gssup_domain)
    {
        if (establishMsg.client_context_id != 0) {
            // Error, we do not support stateful mode
            log.error("Stateful security contexts not supported");

            throw new org.omg.CORBA.NO_PERMISSION(
                    "Stateful security contexts not supported");
        }

        if (log.isDebugEnabled()) {
            log.debug("accepting context...");
        }

        // Ignore authorization token list (not supported)
        // establishMsg.authorization_token;

        // Ignore identity token for now
        // establishMsg.identity_token;

        // Extract client authentication token
        if (support_gssup_authorization
            && establishMsg.identity_token.discriminator() == ITTAbsent.value
            && establishMsg.client_authentication_token.length > 0)
        {
            InitialContextToken gssupToken = decodeGSSUPToken(establishMsg.client_authentication_token);

            String useratrealm = utf8decode(gssupToken.username);

            String name;
            String realm;

            int idx = useratrealm.lastIndexOf('@');
            if (idx == -1) {
                name = useratrealm;
                realm = "default";
            } else {
                name = useratrealm.substring(0, idx);
                realm = useratrealm.substring(idx + 1);
            }

            if (!realm.equals(gssup_domain)) {
                returnContextError(ri, 1, 1);
                throw new org.omg.CORBA.NO_PERMISSION("bad domain: \"" + realm
                                                      + "\"");
            }

            String password = utf8decode(gssupToken.password);

            if (log.isDebugEnabled()) {
                log.debug("GSSUP initial context token name=" + name
                          + "; realm=" + realm + "; password=" + password);
            }

            try {

                Subject subject = SecurityContext.login(name, realm, password);

                // Login succeeded
                SecurityContext.setAuthenticatedSubject(subject);

                if (log.isDebugEnabled()) {
                    log.debug("Login succeeded");
                }

                returnCompleteEstablishContext(ri);

            }
            catch (LoginException ex) {
                // Login failed
                log.error("Login failed", ex);

                returnContextError(ri, 1, 1);
                throw new org.omg.CORBA.NO_PERMISSION("login failed");

            }
            catch (Exception ex) {
                log.error("Exception occured: ", ex);
            }

        } else if (require_gssup_authorization) {

            returnContextError(ri, 1, 1);
            throw new org.omg.CORBA.NO_PERMISSION(
                    "GSSUP authorization required");

        } else if (support_gssup_principal_identity
                   && establishMsg.identity_token.discriminator() == ITTPrincipalName.value)
        {

            if (log.isDebugEnabled()) {
                log.debug("accepting ITTPrincipalName");
            }

            byte[] name = establishMsg.identity_token.principal_name();
            Any aa;
            try {
                aa = codec.decode_value(name, OctetSeqHelper.type());
            }
            catch (UserException e) {
                MARSHAL me = new MARSHAL("cannot decode security descriptor",
                                         0, CompletionStatus.COMPLETED_NO);
                me.initCause(e);
                throw me;
            }

            byte[] exported_name = OctetSeqHelper.extract(aa);
            // byte[] exported_name = uncapsulateByteArray(name);
            String userAtDomain = decodeGSSExportedName(exported_name);

            if (log.isDebugEnabled()) {
                log.debug("establish ITTPrincipalName " + userAtDomain);
            }

            int idx = userAtDomain.indexOf('@');
            String user = "";
            String domain;

            if (idx == -1) {
                user = userAtDomain;
                domain = "default";
            } else {
                user = userAtDomain.substring(0, idx);
                domain = userAtDomain.substring(idx + 1);
            }

            if (gssup_domain != null && !domain.equals(gssup_domain)) {
                returnContextError(ri, 1, 1);

                log.warn("request designates wrong domain: " + userAtDomain);
                throw new org.omg.CORBA.NO_PERMISSION("bad domain");
            }

            // CSISubjectInfo.setPropagatedCaller (user, domain);
            Subject subject = SecurityContext.delegate(user, domain);
            SecurityContext.setAuthenticatedSubject(subject);

            returnCompleteEstablishContext(ri);

        } else if (establishMsg.identity_token.discriminator() == ITTAnonymous.value) {
            // establish anoynous identity

            if (log.isDebugEnabled()) {
                log.debug("accepting ITTAnonymous");
            }

            // CSISubjectInfo.setAnonymousSubject ();
            try {
                Subject subject = SecurityContext.anonymousLogin();
                SecurityContext.setAuthenticatedSubject(subject);
            }
            catch (LoginException ex) {
                // Won't happen
            }

            returnCompleteEstablishContext(ri);

        } else if (establishMsg.identity_token.discriminator() == ITTDistinguishedName.value) {

            if (log.isDebugEnabled()) {
                log.debug("accepting ITTDistinguishedName");
            }

            byte[] name_data = establishMsg.identity_token.dn();

            Any aa;
            try {
                aa = codec.decode_value(name_data, OctetSeqHelper.type());
            }
            catch (UserException e) {
                MARSHAL me = new MARSHAL("cannot encode security descriptor",
                                         0, CompletionStatus.COMPLETED_NO);
                me.initCause(e);
                throw me;
            }
            byte[] x500name_data = OctetSeqHelper.extract(aa);

            // byte[] x500name_data = uncapsulateByteArray(name_data);

            try {

                Subject subject = new Subject();
                subject.getPrincipals().add(new X500Principal(x500name_data));
                SecurityContext.setAuthenticatedSubject(subject);

            }
            catch (IllegalArgumentException ex) {

                if (log.isDebugEnabled()) {
                    log.debug("cannot decode X500 name", ex);
                }

                returnContextError(ri, 1, 1);
                throw new org.omg.CORBA.NO_PERMISSION("cannot decode X500 name");
            }

            returnCompleteEstablishContext(ri);

        } else {

            returnContextError(ri, 2, 1);
            throw new org.omg.CORBA.NO_PERMISSION("Unsupported IdentityToken");

        }
    }


    void returnCompleteEstablishContext(ServerRequestInfo ri) {
        // Create CompleteEstablishContext
        SASContextBody sasBody = new SASContextBody();

        CompleteEstablishContext completeMsg = new CompleteEstablishContext();

        completeMsg.client_context_id = 0;
        completeMsg.context_stateful = false;
        completeMsg.final_context_token = EMPTY_BARR;

        sasBody.complete_msg(completeMsg);

        if (log.isDebugEnabled()) {
            log.debug("Adding SASContextBody, discriminator = "
                      + sasBody.discriminator());
        }

        ri.add_reply_service_context(encodeSASContextBody(sasBody), true);
    }

    void returnContextError(ServerRequestInfo ri, int major, int minor) {
        // Create CompleteEstablishContext
        SASContextBody sasBody = new SASContextBody();

        ContextError errorMsg = new ContextError();

        errorMsg.client_context_id = 0;
        errorMsg.major_status = major;
        errorMsg.minor_status = minor;
        errorMsg.error_token = EMPTY_BARR;

        sasBody.error_msg(errorMsg);

        if (log.isDebugEnabled()) {
            log.debug("Adding SASContextBody, discriminator = "
                      + sasBody.discriminator());
        }

        ri.add_reply_service_context(encodeSASContextBody(sasBody), true);
    }

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

    // void login(Subject subject, String realm, String name,
    // String password) throws LoginException {

    // LoginContext lc = new LoginContext
    // ("EASSERVER", subject, new LoginCallbackHandler(name, password));

    // lc.login();
    // }


}
