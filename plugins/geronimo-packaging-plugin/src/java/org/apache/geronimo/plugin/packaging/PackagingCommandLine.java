/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.plugin.packaging;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @version $Rev:  $ $Date:  $
 */
public class PackagingCommandLine {


    public static void Main(String[] args) throws Exception {
        File configFile = new File("packaging.properties");
        Properties config = new Properties();
        InputStream in = new FileInputStream(configFile);
        try {
            config.load(in);
        } finally {
            in.close();
        }
        mergeArgs(config, args);

        new PackagingCommandLine(config).execute();

    }


    private static void mergeArgs(Properties config, String[] args) throws Exception {
        if (args.length % 2 != 0) {
            throw new Exception("There must be an even number of args, --<name> followed by value");
        }
        for (int i = 0; i < args.length; i++) {
            String key = args[i++];
            String value = args[i];
            if (!key.startsWith("--")) {
                throw new Exception("Keys must be preceded by '--'");
            }
            key = key.substring(2);
            config.put(key, value);
        }
    }

    private final Properties config;

    public PackagingCommandLine(Properties config) {
        this.config = config;
    }

    public void execute() throws Exception {
        PackageBuilder builder = new PackageBuilder();
        builder.setClassPath(config.getProperty("classPath"));
        builder.setConfigurationStoreClass(config.getProperty("configurationStoreClass"));
        builder.setDeployerName(config.getProperty("deployerName"));
        builder.setDeploymentConfig(config.getProperty("deploymentConfig"));
        builder.setEndorsedDirs(config.getProperty("endorsedDirs"));
        builder.setExtensionDirs(config.getProperty("extensionDirs"));
        builder.setMainClass(config.getProperty("mainClass"));
        builder.setModuleFile(getFile(config.getProperty("moduleFile")));
        builder.setPackageFile(getFile(config.getProperty("packageFile")));
        builder.setPlanFile(getFile(config.getProperty("planFile")));
        builder.setRepository(getFile(config.getProperty("repository")));
        builder.setRepositoryClass(config.getProperty("repositoryClass"));
        builder.execute();
    }

    private File getFile(String fileName) {
        if (fileName == null) {
            return null;
        }
        return new File(fileName);
    }


}
