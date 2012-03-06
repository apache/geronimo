/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.apache.geronimo.jasper;

import java.net.URL;
import java.util.Collection;

import org.osgi.framework.Bundle;

public interface TldProvider {
    
    Collection<TldEntry> getTlds();

    public class TldEntry {
        
        private URL url;
        private Bundle bundle;
        private URL jarUrl;
        private String name;
        
        public TldEntry(Bundle bundle, URL url) {
            this(bundle, url, null);
        }
        
        public TldEntry(Bundle bundle, URL url, URL jarUrl) {
            this.bundle = bundle;
            this.url = url;            
            this.jarUrl = jarUrl;
            
            String path = url.getPath();
            if (path.startsWith("/")) {
                name = path.substring(1);
            } else {
                name = path;
            }
        }
                
        public URL getURL() {
            return url;
        }
        
        public Bundle getBundle() {
            return bundle;
        }
        
        public URL getJarUrl() {
            return jarUrl;
        }
        
        public String getName() {
            return name;
        }

    }
}
