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

package org.apache.geronimo.directory;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.ldap.server.configuration.MutableContextPartitionConfiguration;
import org.apache.ldap.server.configuration.ShutdownConfiguration;
import org.apache.ldap.server.jndi.CoreContextFactory;

/**
 * Directory GBean
 *
 * @version $Rev: 233391 $ $Date: 2005-08-18 16:38:47 -0600 (Thu, 18 Aug 2005) $
 */
public class DirectoryGBean implements GBeanLifecycle {

    private static final Log log = LogFactory.getLog(DirectoryGBean.class);
    
    private final ServerInfo serverInfo;
    private final String workingDir;
    private final boolean anonymousAccess;
    private String providerURL;
    private String securityPrincipal;
    private String securityCredentials;
    private String securityAuthentication;
    private int port = 389;
    private InetAddress host = null;
    private boolean enableNetworking;
    private final String configFile;
    
    /**
     * Geronimo class loader
     **/
    private ClassLoader classLoader;

    public DirectoryGBean(ClassLoader classLoader, String workingDir, 
            boolean anonymousAccess, String configFile,
            ServerInfo serverInfo) {
        
        if (serverInfo == null)
            throw new IllegalArgumentException("Must have a ServerInfo value in initParams.");
        
        if (workingDir == null)
            this.workingDir = "var/ldap";
        else
            this.workingDir = workingDir;
        
        this.classLoader = classLoader;
        this.anonymousAccess = anonymousAccess;
        this.configFile = configFile;
        this.serverInfo = serverInfo;
        setHost("0.0.0.0");
    }

    public String getProviderURL() {
        return providerURL;
    }

    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    public String getSecurityAuthentication() {
        return securityAuthentication;
    }

    public void setSecurityAuthentication(String securityAuthentication) {
        this.securityAuthentication = securityAuthentication;
    }

    public String getSecurityCredentials() {
        return securityCredentials;
    }

    public void setSecurityCredentials(String securityCredentials) {
        this.securityCredentials = securityCredentials;
    }

    public String getSecurityPrincipal() {
        return securityPrincipal;
    }

    public void setSecurityPrincipal(String securityPrincipal) {
        this.securityPrincipal = securityPrincipal;
    }

    public boolean isEnableNetworking() {
        return enableNetworking;
    }

    public void setEnableNetworking(boolean enableNetworking) {
        this.enableNetworking = enableNetworking;
    }

    public String getHost() {
        if (host == null){
            return "0.0.0.0";
        }
        
        return host.getHostAddress();
    }

    public void setHost(String host) {
        try{
            if (host == null )
                this.host = null;
            
            String strHost = host.trim();
            if (strHost.equals("0.0.0.0")){
                this.host = null;
                return;
            }

            this.host = InetAddress.getByName(strHost);

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void doFail() {
        log.info("Service failed");
        //Insert failure code here
    }

    private void setEnvironment(Properties env){

        if (providerURL != null){
            env.put( Context.PROVIDER_URL, providerURL);
        }       

        if (securityAuthentication != null){
            env.put( Context.SECURITY_AUTHENTICATION, securityAuthentication);
        }

        if (securityPrincipal != null){
            env.put( Context.SECURITY_PRINCIPAL, securityPrincipal);
        }

        if (securityCredentials != null){
            env.put( Context.SECURITY_CREDENTIALS, securityCredentials);
        }
    }
    
    public void doStart() throws Exception {
        log.info("Starting LDAP Directory service");
        
        MutableServerStartupConfiguration startup = new MutableServerStartupConfiguration();
        // put some mandatory JNDI properties here
        startup.setWorkingDirectory(serverInfo.resolve(workingDir));
        startup.setAllowAnonymousAccess(anonymousAccess);
        startup.setLdapPort(port);
        startup.setEnableNetworking(enableNetworking);
        startup.setHost(host);
        
        if (configFile != null){
            File f = serverInfo.resolve(configFile);
            DirectoryConfigurator dc = new DirectoryConfigurator();
            dc.configure(classLoader, startup, f);
        }
        
        Properties env = new Properties();
        env.putAll(startup.toJndiEnvironment());
        env.put( Context.INITIAL_CONTEXT_FACTORY, ServerContextFactory.class.getName());
        setEnvironment(env);
        
        //Load the proper class for the Context Loader
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);

        //Fire it up
        new InitialDirContext( env );
        
        //Set it back
        Thread.currentThread().setContextClassLoader(oldCL);
        log.info("LDAP Directory service started.");
    }

    public void doStop() throws Exception {
        log.info("Stopping LDAP Directory service");
        //Insert stopping code here
        Properties env = new Properties();
        env.putAll(new ShutdownConfiguration().toJndiEnvironment());
        env.put( Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName() );
        setEnvironment(env);
        
        //Load the proper class for th Context Loader
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);

        //Shut it down
        new InitialDirContext( env );

        //Set it back
        Thread.currentThread().setContextClassLoader(oldCL);

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("DirectoryGBean",
                DirectoryGBean.class);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        
        infoFactory.addAttribute("providerURL", String.class, true, true);
        infoFactory.addAttribute("securityAuthentication", String.class, true, true);
        infoFactory.addAttribute("securityPrincipal", String.class, true, true);
        infoFactory.addAttribute("securityCredentials", String.class, true, true);
        infoFactory.addAttribute("port", int.class, true, true);
        infoFactory.addAttribute("host", String.class, true, true);
        infoFactory.addAttribute("enableNetworking", boolean.class, true, true);
        
        infoFactory.addAttribute("workingDir", String.class, true);
        infoFactory.addAttribute("anonymousAccess", boolean.class, true);
        infoFactory.addAttribute("configFile", String.class, true);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.setConstructor(new String[] { "classLoader", "workingDir", "anonymousAccess", "configFile", "ServerInfo" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
