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

package org.apache.geronimo.deployment.cli;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.NoSuchOperationException;

/**
 * Utility to unlock a keystore and its private keys
 * @version $Rev$ $Date$
 */
public class CommandUnlockKeystore extends AbstractCommand {

    private static final String KEYSTORE_TRUSTSTORE_PASSWORD_FILE = "org.apache.geronimo.keyStoreTrustStorePasswordFile";

    private static final String GERONIMO_HOME = "org.apache.geronimo.home.dir";

    private static final String DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE = System.getProperty(GERONIMO_HOME) + "/var/config/config-substitutions.properties";

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        String args[] = commandArgs.getArgs();
        if (args.length == 0) {
            throw new DeploymentException("Specify the key store name to be unlocked");
        }
        DeploymentManager dm = connection.getDeploymentManager();
        Kernel kernel = null;
        if (dm instanceof RemoteDeploymentManager) {
            kernel = ((RemoteDeploymentManager) dm).getKernel();
        }
        //This implies key store as well as private key or keys has to be unlocked
        if (args.length >= 1) {
            try {
                Properties properties = loadTrustStorePasswordFile();
                AbstractName keyStoreAbName = getKeyStoreAbstractName(kernel, args[0]);
                unLockKeyStore(kernel, keyStoreAbName, properties, args[0]);
                emit(consoleReader, "Successfuly unlocked the keystore:: " + args[0]);
                for (int i = 1; i < args.length; i++) {
                    unlockKeyAlias(kernel, keyStoreAbName, properties, args[0], args[i]);
                    emit(consoleReader, "Successfuly unlocked the private key:: " + args[i]);
                }
            } catch (FileNotFoundException e) {
                throw new DeploymentException("Unable to read the keystore password from the specified file:: "
                        + System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE, DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE), e);
            } catch (IOException e) {
                throw new DeploymentException("Unable to read the keystore password from the specified file:: "
                        + System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE, DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE), e);
            }
        }
    }

    private Properties loadTrustStorePasswordFile() throws DeploymentException {
        Properties props = new Properties();
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream(System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE, DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE));
            props.load(fstream);
            return props;
        } catch (FileNotFoundException e) {
            throw new DeploymentException("Unable to read specified file:: " + System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE, DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE), e);
        } catch (IOException e) {
            throw new DeploymentException("Unable to read specified file:: " + System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE, DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE), e);
        } finally {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /*
     * Returns the password for private key alias
     */
    private String getKeyAliasPassword(Properties properties, String keyStoreName, String aliasName) throws DeploymentException {
        String aliasPassword = properties.getProperty(aliasName);
        if (aliasPassword == null) {
            throw new DeploymentException("No alias with the name " + aliasName + " exists in the kyeStoreTruststore password properties file::"
                    + System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE, DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE));
        }
        return (String) EncryptionManager.decrypt(aliasPassword);
    }

    /*
     * Get the abstract name for the gbean corresponding to the keystore name
     */
    public AbstractName getKeyStoreAbstractName(Kernel kernel, String keyStoreName) throws DeploymentException {
        AbstractNameQuery abstractNameQuery = new AbstractNameQuery("org.apache.geronimo.management.geronimo.KeystoreInstance");
        for (Iterator it = kernel.listGBeans(abstractNameQuery).iterator(); it.hasNext();) {
            AbstractName abstractName = (AbstractName) it.next();
            String curKeyStoreName;
            try {
                curKeyStoreName = (String) kernel.getAttribute(abstractName, "keystoreName");
            } catch (Exception e) {
                throw new DeploymentException("No keystore exists with the name::" + keyStoreName, e);
            }
            if (keyStoreName.equals(curKeyStoreName)) {
                return abstractName;
            }
        }
        throw new DeploymentException("No keystore exists with the name::" + keyStoreName);
    }

    /*
     * Returns the key store password
     */
    private String getKeyStorePassword(Properties properties, String keyStoreName) throws DeploymentException {
        String keyStorePassword = properties.getProperty(keyStoreName);
        if (keyStorePassword == null) {
            throw new DeploymentException("No keyStorePassword attribute named " + keyStoreName + " exists in the kyeStoreTruststore password properties file::"
                    + System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE, DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE));
        }
        return (String) EncryptionManager.decrypt(keyStorePassword);
    }

    /*
     * method to unlock a private key
     */
    public boolean unlockKeyAlias(Kernel kernel, AbstractName keyStoreAbName, Properties properties, String keyStoreName, String aliasName) throws DeploymentException, FileNotFoundException {
        char[] aliasPassword = getKeyAliasPassword(properties, keyStoreName, aliasName).toCharArray();
        char[] keyStorePassword = getKeyStorePassword(properties, keyStoreName).toCharArray();
        boolean success = false;
        Object[] argsVariable = new Object[] { aliasName, keyStorePassword, aliasPassword };
        String[] argsType = new String[] { aliasName.getClass().getName(), keyStorePassword.getClass().getName(), aliasPassword.getClass().getName() };
        try {
            kernel.invoke(keyStoreAbName, "unlockPrivateKey", argsVariable, argsType);
            success = true;
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Unable to find the gbean with the abstractname:: " + keyStoreAbName, e);
        } catch (NoSuchOperationException e) {
            throw new DeploymentException("No such method unlockPrivateKey available with:: " + keyStoreAbName, e);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        return success;
    }

    /*
     * Method to unlock a keystore
     */
    public void unLockKeyStore(Kernel kernel, AbstractName keyStoreAbName, Properties properties, String keyStoreName) throws DeploymentException {
        char[] keyStorepassword = getKeyStorePassword(properties, keyStoreName).toCharArray();
        try {
            kernel.invoke(keyStoreAbName, "unlockKeystore", new Object[] { keyStorepassword }, new String[] { keyStorepassword.getClass().getName() });
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Unable to find the gbean with the abstractname:: " + keyStoreAbName, e);
        } catch (NoSuchOperationException e) {
            throw new DeploymentException("No such method unlockPrivateKey available with:: " + keyStoreAbName, e);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }
}
