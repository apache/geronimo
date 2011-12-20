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
package org.apache.geronimo.corba.deployment.security.config.css;

import java.util.Iterator;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditorException;
import org.apache.geronimo.corba.security.config.css.CSSASMechConfig;
import org.apache.geronimo.corba.security.config.css.CSSCompoundSecMechConfig;
import org.apache.geronimo.corba.security.config.css.CSSCompoundSecMechListConfig;
import org.apache.geronimo.corba.security.config.css.CSSConfig;
import org.apache.geronimo.corba.security.config.css.CSSGSSUPMechConfigDynamic;
import org.apache.geronimo.corba.security.config.css.CSSGSSUPMechConfigStatic;
import org.apache.geronimo.corba.security.config.css.CSSNULLASMechConfig;
import org.apache.geronimo.corba.security.config.css.CSSNULLTransportConfig;
import org.apache.geronimo.corba.security.config.css.CSSSASITTAbsent;
import org.apache.geronimo.corba.security.config.css.CSSSASITTAnonymous;
import org.apache.geronimo.corba.security.config.css.CSSSASITTPrincipalNameDynamic;
import org.apache.geronimo.corba.security.config.css.CSSSASITTPrincipalNameStatic;
import org.apache.geronimo.corba.security.config.css.CSSSASMechConfig;
import org.apache.geronimo.corba.security.config.css.CSSSSLTransportConfig;
import org.apache.geronimo.corba.security.config.css.CSSTransportMechConfig;
import org.apache.geronimo.corba.xbeans.csiv2.css.CSSCompoundSecMechType;
import org.apache.geronimo.corba.xbeans.csiv2.css.CSSCssDocument;
import org.apache.geronimo.corba.xbeans.csiv2.css.CSSCssType;
import org.apache.geronimo.corba.xbeans.csiv2.css.CSSGSSUPDynamicType;
import org.apache.geronimo.corba.xbeans.csiv2.css.CSSGSSUPStaticType;
import org.apache.geronimo.corba.xbeans.csiv2.css.CSSITTPrincipalNameDynamicType;
import org.apache.geronimo.corba.xbeans.csiv2.css.CSSITTPrincipalNameStaticType;
import org.apache.geronimo.corba.xbeans.csiv2.css.CSSSSLType;
import org.apache.geronimo.corba.xbeans.csiv2.css.CSSSasMechType;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSAssociationOption;
import org.apache.geronimo.deployment.service.XmlAttributeBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.omg.CSIIOP.CompositeDelegation;
import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.DetectMisordering;
import org.omg.CSIIOP.DetectReplay;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.Integrity;
import org.omg.CSIIOP.NoDelegation;
import org.omg.CSIIOP.NoProtection;
import org.omg.CSIIOP.SimpleDelegation;
import org.osgi.framework.Bundle;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
@Component(immediate = true)
@Service
public class CSSConfigEditor implements XmlAttributeBuilder {
    private static final String NAMESPACE = CSSCssDocument.type.getDocumentElementName().getNamespaceURI();

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public Object getValue(XmlObject xmlObject, XmlObject enclosing, String type, Bundle bundle) throws DeploymentException {

        CSSCssType css;
        if (xmlObject instanceof CSSCssType) {
            css = (CSSCssType) xmlObject;
        } else {
            css = (CSSCssType) xmlObject.copy().changeType(CSSCssType.type);
        }
        try {
            XmlBeansUtil.validateDD(css);
        } catch (XmlException e) {
            throw new DeploymentException("Error parsing CSS configuration", e);
        }

        CSSConfig cssConfig = new CSSConfig();

        if (css.isSetCompoundSecMechTypeList()) {
            CSSCompoundSecMechListConfig mechListConfig = cssConfig.getMechList();
            mechListConfig.setStateful(css.getCompoundSecMechTypeList().getStateful());

            CSSCompoundSecMechType[] mechList = css.getCompoundSecMechTypeList().getCompoundSecMechArray();
            for (int i = 0; i < mechList.length; i++) {
                mechListConfig.add(extractCompoundSecMech(mechList[i], bundle));
            }
        }

        return cssConfig;
    }

    protected static CSSCompoundSecMechConfig extractCompoundSecMech(CSSCompoundSecMechType mechType, Bundle bundle) throws DeploymentException {

        CSSCompoundSecMechConfig result = new CSSCompoundSecMechConfig();

        if (mechType.isSetSSL()) {
            result.setTransport_mech(extractSSLTransport(mechType.getSSL()));
        } else if (mechType.isSetSECIOP()) {
            throw new PropertyEditorException("SECIOP processing not implemented");
        } else {
            result.setTransport_mech(new CSSNULLTransportConfig());
        }

        if (mechType.isSetGSSUPStatic()) {
            result.setAs_mech(extractGSSUPStatic(mechType.getGSSUPStatic()));
        } else if (mechType.isSetGSSUPDynamic()) {
            result.setAs_mech(extractGSSUPDynamic(mechType.getGSSUPDynamic()));
        } else {
            result.setAs_mech(new CSSNULLASMechConfig());
        }

        result.setSas_mech(extractSASMech(mechType.getSasMech(), bundle));

        return result;
    }

    protected static CSSTransportMechConfig extractSSLTransport(CSSSSLType sslType) {
        CSSSSLTransportConfig result = new CSSSSLTransportConfig();

        result.setSupports(extractAssociationOptions(sslType.getSupports()));
        result.setRequires(extractAssociationOptions(sslType.getRequires()));

        return result;
    }

    protected static CSSASMechConfig extractGSSUPStatic(CSSGSSUPStaticType gssupType) {
        return new CSSGSSUPMechConfigStatic(gssupType.getUsername(), gssupType.getPassword(), gssupType.getDomain());
    }

    protected static CSSASMechConfig extractGSSUPDynamic(CSSGSSUPDynamicType gssupType) {
        return new CSSGSSUPMechConfigDynamic(gssupType.getDomain());
    }

    protected static CSSSASMechConfig extractSASMech(CSSSasMechType sasMechType, Bundle bundle) throws DeploymentException {
        CSSSASMechConfig result = new CSSSASMechConfig();

        if (sasMechType == null) {
            result.setIdentityToken(new CSSSASITTAbsent());
        } else if (sasMechType.isSetITTAbsent()) {
            result.setIdentityToken(new CSSSASITTAbsent());
        } else if (sasMechType.isSetITTAnonymous()) {
            result.setIdentityToken(new CSSSASITTAnonymous());
        } else if (sasMechType.isSetITTPrincipalNameStatic()) {
            CSSITTPrincipalNameStaticType principal = sasMechType.getITTPrincipalNameStatic();
            result.setIdentityToken(new CSSSASITTPrincipalNameStatic(principal.getOid(), principal.getName()));
        } else if (sasMechType.isSetITTPrincipalNameDynamic()) {
            CSSITTPrincipalNameDynamicType principal = sasMechType.getITTPrincipalNameDynamic();
            String principalClassName = principal.getPrincipalClass();
            Class principalClass = null;
            try {
                principalClass = ClassLoading.loadClass(principalClassName, bundle);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load principal class", e);
            }
            String domainName = principal.getDomain();
            String realmName = null;
            if (domainName != null) {
                realmName = principal.getRealm();
            }
            result.setIdentityToken(new CSSSASITTPrincipalNameDynamic(principal.getOid(), principalClass, domainName, realmName));
        }

        return result;
    }

    protected static short extractAssociationOptions(List list) {
        short result = 0;

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            TSSAssociationOption.Enum obj = TSSAssociationOption.Enum.forString((String) iter.next());

            if (TSSAssociationOption.NO_PROTECTION.equals(obj)) {
                result |= NoProtection.value;
            } else if (TSSAssociationOption.INTEGRITY.equals(obj)) {
                result |= Integrity.value;
            } else if (TSSAssociationOption.CONFIDENTIALITY.equals(obj)) {
                result |= Confidentiality.value;
            } else if (TSSAssociationOption.DETECT_REPLAY.equals(obj)) {
                result |= DetectReplay.value;
            } else if (TSSAssociationOption.DETECT_MISORDERING.equals(obj)) {
                result |= DetectMisordering.value;
            } else if (TSSAssociationOption.ESTABLISH_TRUST_IN_TARGET.equals(obj)) {
                result |= EstablishTrustInTarget.value;
            } else if (TSSAssociationOption.ESTABLISH_TRUST_IN_CLIENT.equals(obj)) {
                result |= EstablishTrustInClient.value;
            } else if (TSSAssociationOption.NO_DELEGATION.equals(obj)) {
                result |= NoDelegation.value;
            } else if (TSSAssociationOption.SIMPLE_DELEGATION.equals(obj)) {
                result |= SimpleDelegation.value;
            } else if (TSSAssociationOption.COMPOSITE_DELEGATION.equals(obj)) {
                result |= CompositeDelegation.value;
            }
        }
        return result;
    }

}
