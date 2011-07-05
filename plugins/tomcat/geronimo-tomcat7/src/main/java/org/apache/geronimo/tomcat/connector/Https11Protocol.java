/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.tomcat.connector;

public interface Https11Protocol {

    //Https
    public String getAlgorithm();
    public void setAlgorithm(String algorithm);
    public String getClientAuth();
    public void setClientAuth(String clientAuth);
    public String getKeystoreFile();
    public void setKeystoreFile(String keystoreFile);
    public String getKeystorePass();
    public void setKeystorePass(String keystorePass);
    public String getKeystoreType();
    public void setKeystoreType(String keystoreType);
    public String getSslProtocol();
    public void setSslProtocol(String sslProtocol);
    public String getCiphers();
    public void setCiphers(String ciphers);
    public String getKeyAlias();
    public void setKeyAlias(String keyAlias);
    public String getTruststoreFile();
    public void setTruststoreFile(String truststoreFile);
    public String getTruststorePass();
    public void setTruststorePass(String truststorePass);
    public String getTruststoreType();
    public void setTruststoreType(String truststoreType);

}
