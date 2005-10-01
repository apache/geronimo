/**
*
* Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.tomcat;

import java.net.URL;
import java.net.URLClassLoader;

/**
* @version $Rev$ $Date$
*/
public class TomcatClassLoader extends URLClassLoader {
   private final boolean contextPriorityClassLoader;
   private final ClassLoader parent;
   private final ClassLoader resourceClassLoader;

   public TomcatClassLoader(URL[] urls, URL resourceURL, ClassLoader parent, boolean contextPriorityClassLoader) {
       super(urls, parent);

       if (parent == null) {
           throw new IllegalArgumentException("Parent class loader is null");
       }
       URL[] resourceURLS;
       if (resourceURL != null) {
           resourceURLS = new URL[urls.length + 1];
           System.arraycopy(urls, 0, resourceURLS, 0, urls.length);
           resourceURLS[resourceURLS.length - 1] = resourceURL;
       } else {
           resourceURLS = urls;
       }
       resourceClassLoader = new ResourceClassLoader(resourceURLS, parent);

       // hold on to the parent so we don't have to go throught the security check each time
       this.parent = parent;
       this.contextPriorityClassLoader = contextPriorityClassLoader;
   }

   public Class loadClass(String name) throws ClassNotFoundException {
       if (!contextPriorityClassLoader ||
               name.startsWith("java.") ||
               name.startsWith("javax.") ||
               name.startsWith("org.apache.geronimo.") ||
               name.startsWith("org.apache.jasper.") ||
               name.startsWith("org.apache.tomcat.") ||
               name.startsWith("org.apache.naming.") ||
               name.startsWith("org.apache.catalina.") ||
               name.startsWith("org.apache.commons.logging.") ||
               name.startsWith("org.xml.") ||
               name.startsWith("org.w3c.")) {
           return super.loadClass(name);
       }

       // first check if this class has already been loaded
       Class clazz = findLoadedClass(name);
       if (clazz != null) {
           return clazz;
       }

       // try to load the class from this class loader
       try {
           clazz = findClass(name);
       } catch (ClassNotFoundException ignored) {
       }
       if (clazz != null) {
           return clazz;
       }

       // that didn't work... try the parent
       return parent.loadClass(name);
   }

   public URL getResource(String name) {
       return resourceClassLoader.getResource(name);
   }

   private class ResourceClassLoader extends URLClassLoader {

       public ResourceClassLoader(URL[] urls, ClassLoader classLoader) {
           super(urls, classLoader);
       }

       public URL getResource(String name) {
           if (!contextPriorityClassLoader ||
                   name.startsWith("java/") ||
                   name.startsWith("javax/") ||
                   name.startsWith("org/apache/geronimo/") ||
                   name.startsWith("org/apache/jasper/") ||
                   name.startsWith("org/apache/tomcat/") ||
                   name.startsWith("org/apache/naming/") ||
                   name.startsWith("org/apache/catalina/") ||
                   name.startsWith("org/apache/commons/logging/") ||
                   name.startsWith("org/xml/") ||
                   name.startsWith("org/w3c/")) {
               return super.getResource(name);
           }

           // try to load the resource from this class loader
           URL url = findResource(name);
           if (url != null) {
               return url;
           }

           // that didn't work... try the parent
           return parent.getResource(name);
       }
   }
}
