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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.bval.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.jar.JarFile;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.bval.jsr303.xml.ConstraintMappingsType;
import org.apache.bval.jsr303.xml.ValidationConfigType;
import org.apache.bval.jsr303.xml.ValidationMappingParser;
import org.apache.bval.jsr303.xml.ValidationParser;
import org.apache.geronimo.bval.ValidatorFactoryGBean;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeployableBundle;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.IOUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Validation Module Builder extension to support customization of ValidatorFactory using validation.xml descriptors.
 *
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class BValModuleBuilderExtension implements ModuleBuilderExtension {
    private static final Logger log = LoggerFactory.getLogger(BValModuleBuilderExtension.class);

    private static final Schema VALIDATION_CONFIGURATION_SCHMEA;

    private static final Schema VALIDATION_MAPPING_SCHMEA;

    private static final JAXBContext VALIDATION_CONFIGURATION_JAXBCONTEXT;

    private static final JAXBContext CONSTRAINTMAPPINGSTYPE_JAXBCONTEXT;

    static {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL schemaUrl = ValidationParser.class.getClassLoader().getResource("META-INF/validation-configuration-1.0.xsd");
            VALIDATION_CONFIGURATION_SCHMEA = sf.newSchema(schemaUrl);
            URL mappingSchemaUrl = ValidationMappingParser.class.getClassLoader().getResource("META-INF/validation-mapping-1.0.xsd");
            VALIDATION_MAPPING_SCHMEA = sf.newSchema(mappingSchemaUrl);
        } catch (SAXException e) {
            log.error("Unable to initialize validation schema", e);
            throw new RuntimeException("Unable to initialize validation schema", e);
        }
        try {
            VALIDATION_CONFIGURATION_JAXBCONTEXT = JAXBContext.newInstance(ValidationConfigType.class);
        } catch (JAXBException e) {
            log.error("Unable to initialize validation configuration JAXBContext", e);
            throw new RuntimeException("Unable to initialize validation configuration JAXBContext", e);
        }
        try {
            CONSTRAINTMAPPINGSTYPE_JAXBCONTEXT = JAXBContext.newInstance(ConstraintMappingsType.class);
        } catch (JAXBException e) {
            log.error("Unable to initialize constraint mapping JAXBContext", e);
            throw new RuntimeException("Unable to initialize constraint mapping JAXBContext", e);
        }
    }

    // our default environment
    protected Environment defaultEnvironment;

    public BValModuleBuilderExtension(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
    }

    @Override
    public void createModule(Module module, Bundle bundle, Naming naming, ModuleIDBuilder moduleIDBuilder) throws DeploymentException {
    }

    @Override
    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    @Override
    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
    }

    @Override
    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        // don't do anything for Bundle-based deployments
        if (module.getDeployable() instanceof DeployableBundle) {
            return;
        }

        String moduleName = null;
        String validationConfig = null;
        // the location of the validation config varies depending
        // on the module type
        if (module.getType() == ConfigurationModuleType.WAR) {
            validationConfig = "WEB-INF/validation.xml";
        } else if (module.getType() == ConfigurationModuleType.EAR|| module.getType() == ConfigurationModuleType.EJB
                   || module.getType() == ConfigurationModuleType.CAR || module.getType() == ConfigurationModuleType.RAR) {
            validationConfig = "META-INF/validation.xml";
        }

        if(validationConfig != null) {
            URL validationConfigEntry = null;
            if(module.isStandAlone()) {
                validationConfigEntry = bundle.getEntry(validationConfig);
            } else {
                moduleName = module.getTargetPath();
                validationConfigEntry = module.getDeployable().getResource(validationConfig);
            }
            if(validationConfigEntry == null) {
                // No validation.xml file
                validationConfig = null;
            } else {
                InputStream inp = null;
                try {
                    Unmarshaller unmarshaller = VALIDATION_CONFIGURATION_JAXBCONTEXT.createUnmarshaller();
                    unmarshaller.setSchema(VALIDATION_CONFIGURATION_SCHMEA);
                    inp = validationConfigEntry.openStream();
                    StreamSource stream = new StreamSource(inp);
                    JAXBElement<ValidationConfigType> root = unmarshaller.unmarshal(stream, ValidationConfigType.class);
                    ValidationConfigType xmlConfig = root.getValue();
                    if(xmlConfig.getConstraintMapping().size() > 0) {
                        for (JAXBElement<String> mappingFileNameElement : xmlConfig.getConstraintMapping()) {
                            String mappingFileName = mappingFileNameElement.getValue();
                            if(bundle.getEntry(mappingFileName) == null) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Non-existent constraint mapping file " + mappingFileName + " specified in " + validationConfig + " in module " + module.getName());
                                }
                            } else {
                                // Parse the constraint mappings file and log debug messages if there are any errors
                                InputStream inp1 = null;
                                try {
                                    if(module.isStandAlone()) {
                                        inp1 = bundle.getEntry(mappingFileName).openStream();
                                    } else {
                                        inp1 = module.getDeployable().getResource(mappingFileName).openStream();
                                    }
                                    stream = new StreamSource(inp1);
                                    unmarshaller = CONSTRAINTMAPPINGSTYPE_JAXBCONTEXT.createUnmarshaller();
                                    unmarshaller.setSchema(VALIDATION_MAPPING_SCHMEA);
                                    JAXBElement<ConstraintMappingsType> mappingRoot = unmarshaller.unmarshal(stream, ConstraintMappingsType.class);
                                    ConstraintMappingsType constraintMappings = mappingRoot.getValue();
                                } catch (JAXBException e) {
                                    log.debug("Error processing constraint mapping file "+mappingFileName+" specified in "+validationConfig+" in module "+module.getName(), e);
                                } catch (IOException e) {
                                    log.debug("Error processing constraint mapping file "+mappingFileName+" specified in "+validationConfig+" in module "+module.getName(), e);
                                } finally {
                                    IOUtils.close(inp1);
                                }
                            }
                        }
                    }
                } catch (JAXBException e) {
                    log.debug("Error processing validation configuration " + validationConfig + " in module " + module.getName(), e);
                } catch (IOException e) {
                    log.debug("Error processing validation configuration " + validationConfig + " in module " + module.getName(), e);
                } finally {
                    IOUtils.close(inp);
                }
            }
        }
        EARContext moduleContext = module.getEarContext();
        AbstractName abstractName = moduleContext.getNaming().createChildName(module.getModuleName(), "ValidatorFactory", NameFactory.VALIDATOR_FACTORY);
        GBeanData gbeanData = new GBeanData(abstractName, ValidatorFactoryGBean.class);
        gbeanData.setPriority(GBeanInfo.PRIORITY_CLASSLOADER);
        gbeanData.setAttribute("moduleName", moduleName);
        gbeanData.setAttribute("validationConfig", validationConfig);
        try {
            moduleContext.addGBean(gbeanData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Duplicate ValidatorFactory GBean", e);
        }
    }

    @Override
    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
    }
}

