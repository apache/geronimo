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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.Configuration;

/**
 * This Class should build Configurations out of deployment Module.
 * @version $Rev: $ $Date: $ 
 */
public class WSConfigBuilder implements ConfigurationBuilder {
   // private final AxisGbean axisGBean;
    
//    public WSConfigBuilder(AxisGbean axisGBean){
//        //this.axisGBean = axisGBean;
//    }
    public WSConfigBuilder(){
        //this.axisGBean = axisGBean;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("WSConfigBuilder",WSConfigBuilder.class);
        //referances
        //infoFactory.addReference("AxisGBean", AxisGbean.class);
        //interfaces
        infoFactory.addInterface(ConfigurationBuilder.class);
        //constructers
        //infoFactory.setConstructor(new String[]{"AxisGBean"});
        
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    
    public void doStart() throws WaitingException, Exception {
    }

    public Object getDeploymentPlan(File planFile, JarFile jarFile) throws DeploymentException {
        return null;
    }

    public List buildConfiguration(Object plan, JarFile unused, File unpackedDir) throws IOException, DeploymentException {
        try {
            WSPlan wsplan = null; 
            if(plan instanceof WSPlan){
                wsplan = (WSPlan)plan;
            }
            
            if(wsplan.isEJBbased()){
                GBeanMBean wsGbean = new GBeanMBean(EJBWSGBean.getGBeanInfo());
                ArrayList classList = AxisGeronimoUtils.getClassFileList(new ZipFile(wsplan.getModule()));
                wsGbean.setAttribute("classList", classList);
                wsGbean.setReferencePattern("ejbConfig", wsplan.getEjbConfName());
                Map gbeans = new HashMap();
                gbeans.put(wsplan.getWsName(), wsGbean);
//      
//                //create a configuraton with Web Service GBean
                byte[] state = Configuration.storeGBeans(gbeans);
                AxisGeronimoUtils.createConfiguration(wsplan.getConfigURI(),state,unpackedDir);

            }else{
                File rawmodule = wsplan.getModule();
                File installedModule = new File(unpackedDir, rawmodule.getName());
                copyTheFile(rawmodule, installedModule);
                
                
                GBeanMBean gbean = new GBeanMBean(POJOWSGBean.getGBeanInfo());
                //TODO fill up the POJOWSGBean info
                ArrayList classList = AxisGeronimoUtils.getClassFileList(new ZipFile(installedModule));
                gbean.setAttribute("classList", classList);
                gbean.setAttribute("moduleURL", installedModule.toURL());
            
                Map gbeans = new HashMap();
                gbeans.put(wsplan.getWsName(), gbean);
                byte[] state = Configuration.storeGBeans(gbeans);
                AxisGeronimoUtils.createConfiguration(wsplan.getConfigURI(),state,unpackedDir);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(e);
        }

        
        
        //TODO this is not implemented as the current code need the location of the file 
        //where this gives a Zip file. For the time been the method is override and used.  
        return null;
    }
    
//    /**
//     * the users suppose to use 
//     * <code>
//     * 		File outFile = store.newConfigDir();
//     * 		buildConfiguration(objectName,module,outFile);
//     * 		//returning list have nothing in it.
//     * 		URI uri = store.install(outFile);
//     * 		//the URI will put under a configuration and started later.
//     * </code>
//     * @param plan
//     * @param earFile
//     * @param outfile
//     * @return
//     * @throws Exception
//     */
//
//    public List buildConfiguration(Object plan, 
//            File earFile, 
//            File outfile) throws Exception {
//        ObjectName wsconf = new ObjectName("geronimo.test:name=" + earFile.getName());
//        ObjectName ejbconf = new ObjectName("geronimo.test:name=" + earFile.getName() + "EJB");
//
//        Enumeration entires = new JarFile(earFile).entries();
//        while (entires.hasMoreElements()) {
//            ZipEntry zipe = (ZipEntry) entires.nextElement();
//            String name = zipe.getName();
//            if (name.endsWith("/ejb-jar.xml")) {
//                hasEJB = true;
//                System.out.println("entry found " + name + " the web service is based on a ejb.");
//                //log.info("the web service is based on a ejb.");
//                break;
//            }
//        }
//        GBeanMBean[] confBeans = null;
//        if (hasEJB) {
//            return installEJBWebService(earFile, outfile,wsconf);
//            
//        } else {
//            return installPOJOWebService(earFile, outfile,wsconf);
//
//        }
//    }


//    /**
//     * @param module      Web Service module generated by EWS
//     * @param unpackedDir for WS
//     * @return the file to where Module is copied in to
//     */
//    private List installPOJOWebService(File module, File unpackedDir,ObjectName pojoWSGbeanName) throws Exception {
//        List installedConfig = new ArrayList();
//        File out = new File(unpackedDir, module.getName());
//        copyTheFile(module, out);
//
//        GBeanMBean gbean = new GBeanMBean(POJOWSGBean.getGBeanInfo());
//        //TODO fill up the POJOWSGBean info
//        ArrayList classList = AxisGeronimoUtils.getClassFileList(new ZipFile(module));
//        gbean.setAttribute("classList", classList);
//        gbean.setAttribute("moduleURL", module.toURL());
//        
//        Map gbeans = new HashMap();
//        gbeans.put(pojoWSGbeanName, gbean);
//        byte[] state = Configuration.storeGBeans(gbeans);
//        
//        
//        installedConfig.add(AxisGeronimoUtils.saveAsConfiguration(state,new URI("test1"),store));
//        return installedConfig;
//    }
    
    
    

//    private List installEJBWebService(File module, 
//                File unpackedDir,
//                ObjectName wSGbeanName) throws Exception {
//        List installedConfig = new ArrayList();
//
//        JarFile jarFile = new JarFile(module);
//        //Install the EJB
//        Object plan = earConfigBuilder.getDeploymentPlan(null, jarFile);
//        earConfigBuilder.buildConfiguration(plan, jarFile, unpackedDir);
//        URI uri = store.install(unpackedDir);
//        installedConfig.add(uri);
//
//        //load the EJB Configuration TODO, Do we need this?        
//        GBeanMBean ejbGBean = AxisGeronimoUtils.loadConfig(unpackedDir);
//
////        //Create the Web Service GBean       
//        GBeanMBean wsGbean = new GBeanMBean(EJBWSGBean.getGBeanInfo());
//        ArrayList classList = AxisGeronimoUtils.getClassFileList(new ZipFile(module));
//        wsGbean.setAttribute("classList", classList);
////        wsGbean.setReferencePattern("ejbConfig", ejbGBean.getObjectNameObject());
//        Map gbeans = new HashMap();
//        gbeans.put(wSGbeanName, wsGbean);
////      
////        //create a configuraton with Web Service GBean
//        byte[] state = Configuration.storeGBeans(gbeans);
//        installedConfig.add(AxisGeronimoUtils.saveAsConfiguration(state,new URI("test2"),store));
//        return installedConfig;
//    }
//
//
    private void copyTheFile(File inFile, File outFile) throws IOException {
        if (!outFile.exists())
            outFile.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(outFile);
        FileInputStream in = new FileInputStream(inFile);
        try {
            byte[] buf = new byte[1024];
            int val = in.read(buf);
            while (val > 0) {
                out.write(buf, 0, val);
                val = in.read(buf);
            }
        } finally {
            in.close();
            out.close();
        }
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}

