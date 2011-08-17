/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.bval;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ValidationProvider;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.bval.jsr303.ConfigurationImpl;
import org.apache.bval.jsr303.xml.PropertyType;
import org.apache.bval.jsr303.xml.ValidationConfigType;
import org.apache.bval.jsr303.xml.ValidationParser;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.naming.ResourceSource;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


/**
 * GBean that provides access to ValidatorFactory instances for a module
 * <p/>
 * This GBean is used to generate ValidatorFactory instances.  This will use
 * a validation.xml config file, if it exists, or create a default bean validation
 * factory.
 *
 * @version $Rev$ $Date$
 */

@GBean(j2eeType = NameFactory.VALIDATOR_FACTORY)
@OsgiService
public class ValidatorFactoryGBean implements GBeanLifecycle, ResourceSource<ValidationException> {
    private static final Logger log = LoggerFactory.getLogger(ValidatorFactoryGBean.class);
    private static final JAXBContext VALIDATION_FACTORY_CONTEXT;
    private static final Schema VALIDATION_FACTORY_SCHEMA ;

    static {
        URL schemaUrl = ValidationParser.class.getClassLoader().getResource("META-INF/validation-configuration-1.0.xsd");
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            VALIDATION_FACTORY_SCHEMA = sf.newSchema(schemaUrl);
            VALIDATION_FACTORY_CONTEXT = JAXBContext.newInstance(ValidationConfigType.class);
        } catch (SAXException e) {
            throw new RuntimeException("Fail to initialize the schema instance", e);
        } catch (JAXBException e) {
            throw new RuntimeException("Fail to initialize the JAXB context instance", e);
        }
    }

    private final String objectName;
    private final Bundle bundle;
    private final ClassLoader classLoader;
    // module file name if the module is not standalone module. Will be null otherwise.
    private final String moduleName;
    // module validation configuration
    private String validationConfig;
    // The created ValidatorFactory
    private ValidatorFactory factory;
    // Temporary file to hold the extracted archive in case of nested archives
    private File tmpArchiveFile;

    /**
     * Construct an instance of ValidatorFactoryGBean
     * <p/>
     * @param moduleName  the name of the module in an EAR file (null implies standalone module)
     * @param validationConfig    the location of validation configuration xml file in the bundle
     */
    public ValidatorFactoryGBean(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
                                 @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                                 @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                                 @ParamAttribute(name = "moduleName") String moduleName,
                                 @ParamAttribute(name = "validationConfig") String validationConfig) {
        this.objectName = objectName;
        this.bundle = bundle;
        this.classLoader = classLoader;
        this.moduleName = moduleName;
        this.validationConfig = validationConfig;
    }

    @Override
    public Object $getResource() throws ValidationException {
        // return the current factory instance
        return getFactory();
    }


    /**
     * Retrieve (and potentially create) the ValidatorFactory
     * instance for this module.
     *
     * @return A ValidatorFactory instance configured using the validation xml for this module.
     */
    public ValidatorFactory getFactory() {
        if (factory == null) {
            if (validationConfig == null) {
                // just create the default
                createDefaultFactory();
            } else {
                // Parse the validation xml
                ValidationConfigType validationConfigType = null;
                try {
                    Unmarshaller unmarshaller = VALIDATION_FACTORY_CONTEXT.createUnmarshaller();
                    unmarshaller.setSchema(VALIDATION_FACTORY_SCHEMA);
                    URL validationConfigURL = null;
                    if(moduleName == null) {
                        // Standalone module
                        validationConfigURL = bundle.getEntry(validationConfig);
                    } else {
                        if(bundle.getEntry(moduleName+"/"+validationConfig) != null) {
                            // The archive is extracted and repacked as directories
                            validationConfigURL = bundle.getEntry(moduleName+"/"+validationConfig);
                        } else {
                            // Extract archive file and get the validation config entry out
                            // Copy the archive to a temp file for runtime processing
                            try {
                                tmpArchiveFile = FileUtils.createTempFile(".jar");
                                InputStream inp = bundle.getEntry(moduleName).openStream();
                                OutputStream out = new FileOutputStream(tmpArchiveFile);
                                IOUtils.copy(inp, out);
                                inp.close();
                                out.close();
                                validationConfigURL = new URL("jar:"+tmpArchiveFile.toURI().toURL()+"!/"+validationConfig);
                            } catch (IOException e) {
                                log.warn("Error processing validation configuration "+validationConfig+" in "+moduleName + " Using default factory.", e);
                                createDefaultFactory();
                                return factory;
                            }
                        }
                    }
                    StreamSource stream = new StreamSource(validationConfigURL.openStream());
                    JAXBElement<ValidationConfigType> root = unmarshaller.unmarshal(stream, ValidationConfigType.class);
                    validationConfigType = root.getValue();
                } catch(Throwable t) {
                    log.warn("Unable to create module ValidatorFactory instance.  Using default factory", t);
                    createDefaultFactory();
                    return factory;
                }
                // Apply the configuration
                // TODO: Ideally this processing should happen in BVal code. But, the ValidationParser loads the validation xml and
                //       mapping files using the classloader and these files are not always available through the classloader in case
                //       of Java EE (for e.g., WEB-INF/validation.xml)
                ConfigurationImpl config = (ConfigurationImpl) Validation.byDefaultProvider().configure();
                applyConfig(validationConfigType, config);
                config.ignoreXmlConfiguration();
                // Create the factory instance
                ClassLoader oldContextLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(classLoader);
                    factory = config.buildValidatorFactory();
                } finally {
                    Thread.currentThread().setContextClassLoader(oldContextLoader);
                }
                if(tmpArchiveFile != null) {
                    tmpArchiveFile.delete();
                }
            }
        }
        return factory;
    }

    /**
     * Create a default ValidatorFactory
     */
    private void createDefaultFactory() {
        ClassLoader oldContextLoader = Thread.currentThread().getContextClassLoader();
        // No validation configuration specified. Create default instance.
        try {
            Thread.currentThread().setContextClassLoader(null);
            factory = Validation.buildDefaultValidatorFactory();
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextLoader);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyConfig(ValidationConfigType xmlConfig, ConfigurationImpl target) {
        String providerClassName = xmlConfig.getDefaultProvider();
        if (providerClassName != null) {
            Class<? extends ValidationProvider<?>> clazz;
            try {
                clazz = (Class<? extends ValidationProvider<?>>) classLoader.loadClass(providerClassName);
                target.setProviderClass(clazz);
                log.info("Using " + providerClassName + " as validation provider.");
            } catch (ClassNotFoundException e) {
                log.warn("Unable to load provider class "+providerClassName, e);
            }
        }
        String messageInterpolatorClass = xmlConfig.getMessageInterpolator();
        if (target.getMessageInterpolator() == null) {
            if (messageInterpolatorClass != null) {
                Class<MessageInterpolator> clazz;
                try {
                    clazz = (Class<MessageInterpolator>) classLoader.loadClass(messageInterpolatorClass);
                    target.messageInterpolator(clazz.newInstance());
                } catch (Exception e) {
                    log.warn("Unable to set "+messageInterpolatorClass+ " as message interpolator.", e);
                }
                log.info("Using " + messageInterpolatorClass + " as message interpolator.");
            }
        }
        String traversableResolverClass = xmlConfig.getTraversableResolver();
        if (target.getTraversableResolver() == null) {
            if (traversableResolverClass != null) {
                Class<TraversableResolver> clazz;
                try {
                    clazz = (Class<TraversableResolver>) classLoader.loadClass(traversableResolverClass);
                    target.traversableResolver(clazz.newInstance());
                } catch (Exception e) {
                    log.warn("Unable to set "+traversableResolverClass+ " as traversable resolver.", e);
                }
                log.info("Using " + traversableResolverClass + " as traversable resolver.");
            }
        }
        String constraintFactoryClass = xmlConfig.getConstraintValidatorFactory();
        if (target.getConstraintValidatorFactory() == null) {
            if (constraintFactoryClass != null) {
                Class<ConstraintValidatorFactory> clazz;
                try {
                    clazz = (Class<ConstraintValidatorFactory>) classLoader.loadClass(constraintFactoryClass);
                    target.constraintValidatorFactory(clazz.newInstance());
                } catch (Exception e) {
                    log.warn("Unable to set "+constraintFactoryClass+ " as constraint factory.", e);
                }
                log.info("Using " + constraintFactoryClass + " as constraint factory.");
            }
        }
        for (PropertyType property : xmlConfig.getProperty()) {
            if (log.isDebugEnabled()) {
                log.debug("Found property '" + property.getName() + "' with value '" + property.getValue() + "' in " + validationConfig);
            }
            target.addProperty(property.getName(), property.getValue());
        }
        for (JAXBElement<String> mappingFileNameElement : xmlConfig.getConstraintMapping()) {
            String mappingFileName = mappingFileNameElement.getValue();
            if (log.isDebugEnabled()) {
                log.debug("Opening input stream for " + mappingFileName);
            }
            InputStream in = null;
            try {
                if(moduleName == null) {
                    // standalone module
                    in = bundle.getEntry(mappingFileName).openStream();
                } else if(tmpArchiveFile != null) {
                    // The archive was extracted to a temporary file earlier
                    in = new URL("jar:"+tmpArchiveFile.toURI().toURL()+"!/"+mappingFileName).openStream();
                } else {
                    in = bundle.getEntry(moduleName+"/"+mappingFileName).openStream();
                }
                if (in == null) {
                    throw new ValidationException("Unable to open input stream for mapping file " + mappingFileName + (moduleName != null ? " in "+moduleName : ""));
                }
            } catch (IOException e) {
                throw new ValidationException("Unable to open input stream for mapping file " + mappingFileName + (moduleName != null ? " in "+moduleName : ""), e);
            }
            target.addMapping(in);
        }
    }

    public void doStart() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Starting " + objectName);
        }
        // if there is an explicit configuration provided, we need to process
        // this now in case there are exceptions
        if (validationConfig != null) {
            getFactory();
        }
    }

    public void doStop() throws Exception {
        factory = null;
        if (log.isDebugEnabled()) {
            log.debug("Stopped " + objectName);
        }
    }

    public void doFail() {
        factory = null;
        log.warn("Failed " + objectName);
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return false;
    }

}
