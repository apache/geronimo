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
package org.apache.geronimo.corba.deployment.security.config.tss;

import java.util.Iterator;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditorException;
import org.apache.geronimo.corba.security.config.tss.TSSASMechConfig;
import org.apache.geronimo.corba.security.config.tss.TSSCompoundSecMechConfig;
import org.apache.geronimo.corba.security.config.tss.TSSCompoundSecMechListConfig;
import org.apache.geronimo.corba.security.config.tss.TSSConfig;
import org.apache.geronimo.corba.security.config.tss.TSSGSSExportedNameConfig;
import org.apache.geronimo.corba.security.config.tss.TSSGSSUPMechConfig;
import org.apache.geronimo.corba.security.config.tss.TSSGeneralNameConfig;
import org.apache.geronimo.corba.security.config.tss.TSSITTAbsent;
import org.apache.geronimo.corba.security.config.tss.TSSITTAnonymous;
import org.apache.geronimo.corba.security.config.tss.TSSITTDistinguishedName;
import org.apache.geronimo.corba.security.config.tss.TSSITTPrincipalNameGSSUP;
import org.apache.geronimo.corba.security.config.tss.TSSITTX509CertChain;
import org.apache.geronimo.corba.security.config.tss.TSSNULLASMechConfig;
import org.apache.geronimo.corba.security.config.tss.TSSNULLTransportConfig;
import org.apache.geronimo.corba.security.config.tss.TSSSASMechConfig;
import org.apache.geronimo.corba.security.config.tss.TSSSSLTransportConfig;
import org.apache.geronimo.corba.security.config.tss.TSSTransportMechConfig;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSAssociationOption;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSCompoundSecMechType;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSGSSUPType;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSGeneralNameType;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSGssExportedNameType;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSIdentityTokenTypeList;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSSSLType;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSSasMechType;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSTssDocument;
import org.apache.geronimo.corba.xbeans.csiv2.tss.TSSTssType;
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
 * A property editor for {@link org.apache.geronimo.corba.security.config.tss.TSSConfig}.
 *
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
@Component(immediate = true)
@Service
public class TSSConfigEditor implements XmlAttributeBuilder {

    private static final String NAMESPACE = TSSTssDocument.type.getDocumentElementName().getNamespaceURI();

    public String getNamespace() {
        return NAMESPACE;
    }

    /**
     * Returns a TSSConfig object initialized with the input object
     * as an XML string.
     *
     * @return a TSSConfig object
     * @throws org.apache.geronimo.common.propertyeditor.PropertyEditorException
     *          An IOException occured.
     */
    @Override
    public Object getValue(XmlObject xmlObject, XmlObject enclosing, String type, Bundle bundle) throws DeploymentException {
        TSSTssType tss;
        if (xmlObject instanceof TSSTssType) {
            tss = (TSSTssType) xmlObject;
        } else {
            tss = (TSSTssType) xmlObject.copy().changeType(TSSTssType.type);
        }

        try {
            XmlBeansUtil.validateDD(tss);
        } catch (XmlException e) {
            throw new DeploymentException("Error parsing TSS configuration", e);
        }

        TSSConfig tssConfig = new TSSConfig();

        tssConfig.setInherit(tss.getInherit());

        if (tss.isSetSSL()) {
            tssConfig.setTransport_mech(extractSSL(tss.getSSL()));
        } else if (tss.isSetSECIOP()) {
            throw new PropertyEditorException("SECIOP processing not implemented");
        } else {
            tssConfig.setTransport_mech(new TSSNULLTransportConfig());
        }

        if (tss.isSetCompoundSecMechTypeList()) {
            TSSCompoundSecMechListConfig mechListConfig = tssConfig.getMechListConfig();
            mechListConfig.setStateful(tss.getCompoundSecMechTypeList().getStateful());

            TSSCompoundSecMechType[] mechList = tss.getCompoundSecMechTypeList().getCompoundSecMechArray();
            for (int i = 0; i < mechList.length; i++) {
                TSSCompoundSecMechConfig cMech = extractCompoundSecMech(mechList[i], bundle);
                cMech.setTransport_mech(tssConfig.getTransport_mech());
                mechListConfig.add(cMech);
            }
        }

        return tssConfig;
    }

    protected static TSSTransportMechConfig extractSSL(TSSSSLType sslMech) {
        TSSSSLTransportConfig sslConfig = new TSSSSLTransportConfig();

        sslConfig.setHostname(sslMech.getHostname());
        sslConfig.setPort(sslMech.getPort());
        sslConfig.setHandshakeTimeout(sslMech.getHandshakeTimeout());
        sslConfig.setSupports(extractAssociationOptions(sslMech.getSupports()));
        sslConfig.setRequires(extractAssociationOptions(sslMech.getRequires()));

        return sslConfig;
    }

    protected static TSSCompoundSecMechConfig extractCompoundSecMech(TSSCompoundSecMechType mech, Bundle bundle) throws DeploymentException {

        TSSCompoundSecMechConfig result = new TSSCompoundSecMechConfig();

        if (mech.isSetGSSUP()) {
            result.setAs_mech(extractASMech(mech.getGSSUP()));
        } else {
            result.setAs_mech(new TSSNULLASMechConfig());
        }

        if (mech.isSetSasMech()) {
            result.setSas_mech(extractSASMech(mech.getSasMech(), bundle));
        }

        return result;
    }

    protected static TSSASMechConfig extractASMech(TSSGSSUPType gssupMech) {

        TSSGSSUPMechConfig gssupConfig = new TSSGSSUPMechConfig();

        gssupConfig.setTargetName(gssupMech.getTargetName());
        gssupConfig.setRequired(gssupMech.getRequired());

        return gssupConfig;
    }

    protected static TSSSASMechConfig extractSASMech(TSSSasMechType sasMech, Bundle bundle) throws DeploymentException {

        TSSSASMechConfig sasMechConfig = new TSSSASMechConfig();

        if (sasMech.isSetServiceConfigurationList()) {
            sasMechConfig.setRequired(sasMech.getServiceConfigurationList().getRequired());

            TSSGeneralNameType[] generalNames = sasMech.getServiceConfigurationList().getGeneralNameArray();
            for (int i = 0; i < generalNames.length; i++) {
                sasMechConfig.addServiceConfigurationConfig(new TSSGeneralNameConfig(generalNames[i].getPrivilegeAuthority()));
            }

            TSSGssExportedNameType[] exportedNames = sasMech.getServiceConfigurationList().getGssExportedNameArray();
            for (int i = 0; i < exportedNames.length; i++) {
                sasMechConfig.addServiceConfigurationConfig(new TSSGSSExportedNameConfig(exportedNames[i].getPrivilegeAuthority(), exportedNames[i].getOID()));
            }
        }

        TSSIdentityTokenTypeList identityTokenTypes = sasMech.getIdentityTokenTypes();

        if (identityTokenTypes.isSetITTAbsent()) {
            sasMechConfig.addIdentityToken(new TSSITTAbsent());
        } else {
            if (identityTokenTypes.isSetITTAnonymous()) {
                sasMechConfig.addIdentityToken(new TSSITTAnonymous());
            }
            if (identityTokenTypes.isSetITTPrincipalNameGSSUP()) {
                org.apache.geronimo.corba.xbeans.csiv2.tss.TSSITTPrincipalNameGSSUPType ittPrincipalNameGSSUP = identityTokenTypes.getITTPrincipalNameGSSUP();
                String principalClassName = ittPrincipalNameGSSUP.getPrincipalClass();
                Class principalClass;
                try {
                    principalClass = ClassLoading.loadClass(principalClassName, bundle);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("Could not load principal class", e);
                }
                String domainName = ittPrincipalNameGSSUP.isSetDomain() ? ittPrincipalNameGSSUP.getDomain().trim() : null;
                String realmName = null;
                if (domainName != null && ittPrincipalNameGSSUP.isSetRealm()) {
                    realmName = ittPrincipalNameGSSUP.getRealm().trim();
                }


                try {
                    sasMechConfig.addIdentityToken(new TSSITTPrincipalNameGSSUP(principalClass, realmName, domainName));
                } catch (NoSuchMethodException e) {
                    throw new DeploymentException("Could not find principal class constructor", e);
                }
            }
            if (identityTokenTypes.isSetITTDistinguishedName()) {
                String realmName = identityTokenTypes.getITTDistinguishedName().getRealm();
                String domainName = identityTokenTypes.getITTDistinguishedName().getDomain();

                realmName = (realmName == null ? null : realmName.trim());
                domainName = (domainName == null ? null : domainName.trim());
                sasMechConfig.addIdentityToken(new TSSITTDistinguishedName(realmName, domainName));
            }
            if (identityTokenTypes.isSetITTX509CertChain()) {
                String realmName = identityTokenTypes.getITTX509CertChain().getRealm();
                String domainName = identityTokenTypes.getITTX509CertChain().getDomain();

                realmName = (realmName == null ? null : realmName.trim());
                domainName = (domainName == null ? null : domainName.trim());
                sasMechConfig.addIdentityToken(new TSSITTX509CertChain(realmName, domainName));
            }
        }

        return sasMechConfig;
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
