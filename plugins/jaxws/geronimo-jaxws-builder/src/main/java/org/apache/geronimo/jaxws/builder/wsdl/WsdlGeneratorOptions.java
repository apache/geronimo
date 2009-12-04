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
package org.apache.geronimo.jaxws.builder.wsdl;

import javax.xml.namespace.QName;

public class WsdlGeneratorOptions {

    private final static String ADD_TO_CLASSPATH_WSGEN_PROPERTY =
        "org.apache.geronimo.jaxws.wsgen.addToClassPath";
    
    private final static String FORK_WSGEN_PROPERTY = 
        "org.apache.geronimo.jaxws.wsgen.fork";
    
    private final static String FORK_TIMEOUT_WSGEN_PROPERTY = 
        "org.apache.geronimo.jaxws.wsgen.fork.timeout";
    
    public final static long FORK_POLL_FREQUENCY = 1000 * 2; // 2 seconds
    
    public enum SAAJ { SUN, Axis2 };
    
    private QName wsdlService;
    private QName wsdlPort;
    private SAAJ saaj;
    private boolean fork = getDefaultFork();
    private long forkTimeout = getDefaultForkTimeout();
    private boolean addToClassPath = getDefaultAddToClassPath();
    
    private static boolean getDefaultFork() {
        String value = System.getProperty(FORK_WSGEN_PROPERTY);
        if (value != null) {
            return Boolean.valueOf(value).booleanValue();
        } else {
            String osName = System.getProperty("os.name");
            if (osName == null) {
                return false;
            }
            osName = osName.toLowerCase();
            // Fork on Windows only
            return (osName.indexOf("windows") != -1);            
        }
    }
    
    private static long getDefaultForkTimeout() {
        String value = System.getProperty(FORK_TIMEOUT_WSGEN_PROPERTY);
        if (value != null) {
            return Long.parseLong(value);
        } else {
            return 1000 * 60; // 60 seconds
        }
    }
    
    private static boolean getDefaultAddToClassPath() {
        String value = System.getProperty(ADD_TO_CLASSPATH_WSGEN_PROPERTY);
        if (value == null) {
            return true;
        } else {
            return Boolean.parseBoolean(value);
        }
    }
    
    public QName getWsdlService() {
        return wsdlService;
    }
    
    public void setWsdlService(QName wsdlService) {
        this.wsdlService = wsdlService;
    }
    
    public QName getWsdlPort() {
        return wsdlPort;
    }
    
    public void setWsdlPort(QName wsdlPort) {
        this.wsdlPort = wsdlPort;
    }

    public void setSAAJ(SAAJ saaj) {
        this.saaj = saaj;
    }
    
    public SAAJ getSAAJ() {
        return this.saaj;
    }
    
    public void setAddToClassPath(boolean addToClassPath) {
        this.addToClassPath = addToClassPath;        
    }
    
    public boolean getAddToClassPath() {
        return this.addToClassPath;
    }
    
    public long getForkTimeout() {
        return forkTimeout;
    }

    public void setForkTimeout(long forkTimeout) {
        this.forkTimeout = forkTimeout;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }
    
    public boolean getFork() {
        return this.fork;
    }
    
}
