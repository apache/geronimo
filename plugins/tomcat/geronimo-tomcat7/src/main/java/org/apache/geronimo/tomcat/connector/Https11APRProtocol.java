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

public interface Https11APRProtocol extends Http11APRProtocol{
    
    
    //SSL
    public String getSslProtocol();
    public void setSslProtocol(String sslProtocol);
    public String getSslCipherSuite();
    public void setSslCipherSuite(String sslCipherSuite);
    public String getSslCertificateFile();
    public void setSslCertificateFile(String sslCertificateFile);
    public String getSslCertificateKeyFile();
    public void setSslCertificateKeyFile(String sslCertificateKeyFile);
    public String getSslPassword();
    public void setSslPassword(String sslPassword);
    public String getSslVerifyClient();
    public void setSslVerifyClient(String sslVerifyClient);
    public int getSslVerifyDepth();
    public void setSslVerifyDepth(int sslVerifyDepth);
    public String getSslCACertificateFile();
    public void setSslCACertificateFile(String sslCACertificateFile);
    public String getSslCACertificatePath();
    public void setSslCACertificatePath(String sslCACertificatePath);
    public String getSslCertificateChainFile();
    public void setSslCertificateChainFile(String sslCertificateChainFile);
    public String getSslCARevocationFile();
    public void setSslCARevocationFile(String sslCARevocationFile);
    public String getSslCARevocationPath();
    public void setSslCARevocationPath(String sslCARevocationPath);
    


}
