/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.xml.registry;

import java.util.Collection;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

/**
 * @version $Revision$ $Date$
 */
public interface LifeCycleManager {
    public static final String ASSOCIATION = "Association";
    public static final String AUDITABLE_EVENT = "AuditableEvent";
    public static final String CLASSIFICATION = "Classification";
    public static final String CLASSIFICATION_SCHEME = "ClassificationScheme";
    public static final String CONCEPT = "Concept";
    public static final String EMAIL_ADDRESS = "EmailAddress";
    public static final String EXTERNAL_IDENTIFIER = "ExternalIdentifier";
    public static final String EXTERNAL_LINK = "ExternalLink";
    public static final String EXTRINSIC_OBJECT = "ExtrinsicObject";
    public static final String INTERNATIONAL_STRING = "InternationalString";
    public static final String KEY = "Key";
    public static final String LOCALIZED_STRING = "LocalizedString";
    public static final String ORGANIZATION = "Organization";
    public static final String PERSON_NAME = "PersonName";
    public static final String POSTAL_ADDRESS = "PostalAddress";
    public static final String REGISTRY_ENTRY = "RegistryEntry";
    public static final String REGISTRY_PACKAGE = "RegistryPackage";
    public static final String SERVICE = "Service";
    public static final String SERVICE_BINDING = "ServiceBinding";
    public static final String SLOT = "Slot";
    public static final String SPECIFICATION_LINK = "SpecificationLink";
    public static final String TELEPHONE_NUMBER = "TelephoneNumber";
    public static final String USER = "User";
    public static final String VERSIONABLE = "Versionable";

    Association createAssociation(RegistryObject targetObject, Concept associationType) throws JAXRException;

    Classification createClassification(ClassificationScheme scheme, InternationalString name, String value) throws JAXRException;

    Classification createClassification(ClassificationScheme scheme, String name, String value) throws JAXRException;

    Classification createClassification(Concept concept) throws JAXRException, InvalidRequestException;

    ClassificationScheme createClassificationScheme(Concept concept) throws JAXRException, InvalidRequestException;

    ClassificationScheme createClassificationScheme(InternationalString name, InternationalString description) throws JAXRException, InvalidRequestException;

    ClassificationScheme createClassificationScheme(String name, String description) throws JAXRException, InvalidRequestException;

    Concept createConcept(RegistryObject parent, InternationalString name, String value) throws JAXRException;

    Concept createConcept(RegistryObject parent, String name, String value) throws JAXRException;

    EmailAddress createEmailAddress(String address) throws JAXRException;

    EmailAddress createEmailAddress(String address, String type) throws JAXRException;

    ExternalIdentifier createExternalIdentifier(ClassificationScheme identificationScheme, InternationalString name, String value) throws JAXRException;

    ExternalIdentifier createExternalIdentifier(ClassificationScheme identificationScheme, String name, String value) throws JAXRException;

    ExternalLink createExternalLink(String externalURI, InternationalString description) throws JAXRException;

    ExternalLink createExternalLink(String externalURI, String description) throws JAXRException;

    ExtrinsicObject createExtrinsicObject(DataHandler repositoryItem) throws JAXRException;

    InternationalString createInternationalString() throws JAXRException;

    InternationalString createInternationalString(Locale locale, String value) throws JAXRException;

    InternationalString createInternationalString(String value) throws JAXRException;

    Key createKey(String id) throws JAXRException;

    LocalizedString createLocalizedString(Locale locale, String value) throws JAXRException;

    LocalizedString createLocalizedString(Locale locale, String value, String chatsetName) throws JAXRException;

    Object createObject(String interfaceName) throws JAXRException, InvalidRequestException, UnsupportedCapabilityException;

    Organization createOrganization(InternationalString name) throws JAXRException;

    Organization createOrganization(String name) throws JAXRException;

    PersonName createPersonName(String fullName) throws JAXRException;

    PersonName createPersonName(String firstName, String middleName, String lastName) throws JAXRException;

    PostalAddress createPostalAddress(String streetNumber, String street, String city, String stateOrProvince, String country, String postalCode, String type) throws JAXRException;

    RegistryPackage createRegistryPackage(InternationalString name) throws JAXRException;

    RegistryPackage createRegistryPackage(String name) throws JAXRException;

    Service createService(InternationalString name) throws JAXRException;

    Service createService(String name) throws JAXRException;

    ServiceBinding createServiceBinding() throws JAXRException;

    Slot createSlot(String name, Collection values, String slotType) throws JAXRException;

    Slot createSlot(String name, String value, String slotType) throws JAXRException;

    SpecificationLink createSpecificationLink() throws JAXRException;

    TelephoneNumber createTelephoneNumber() throws JAXRException;

    User createUser() throws JAXRException;

    BulkResponse deleteObjects(Collection keys) throws JAXRException;

    BulkResponse deleteObjects(Collection keys, String objectType) throws JAXRException;

    BulkResponse deprecateObjects(Collection keys) throws JAXRException;

    RegistryService getRegistryService() throws JAXRException;

    BulkResponse saveObjects(Collection objects) throws JAXRException;

    BulkResponse unDeprecateObjects(Collection keys) throws JAXRException;
}
