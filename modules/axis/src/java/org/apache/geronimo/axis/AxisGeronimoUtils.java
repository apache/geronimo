/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.axis;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

import javax.management.ObjectName;
import java.io.File;

/**
 * Class AxisGeronimoUtils
 */
public class AxisGeronimoUtils {
    /**
     * Method startGBean
     *
     * @param objectName
     * @param gbean
     * @param kernel
     * @throws DeploymentException
     */
    public static void startGBean(ObjectName objectName, GBeanMBean gbean, Kernel kernel)
            throws DeploymentException {
        try {
            kernel.loadGBean(objectName, gbean);
            kernel.startGBean(objectName);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Method stopGBean
     *
     * @param objectName
     * @param kernel
     * @throws DeploymentException
     */
    public static void stopGBean(ObjectName objectName, Kernel kernel)
            throws DeploymentException {
        try {
            kernel.unloadGBean(objectName);
            kernel.stopGBean(objectName);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Method delete
     *
     * @param file
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    delete(files[i]);
                }
            }
            file.delete();
        }
    }
}
