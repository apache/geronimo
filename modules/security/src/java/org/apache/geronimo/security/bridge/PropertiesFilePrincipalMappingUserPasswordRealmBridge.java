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

package org.apache.geronimo.security.bridge;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;


/**
 * works off a property file with lines of the format:
 * sourceprincipalname=targetprincipal:targetuser:targetpassword
 * <p/>
 * all three can be mapped separately; the source for each key is
 * from the appropriate principal class and possibly
 * callback name.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:08 $
 */
public class PropertiesFilePrincipalMappingUserPasswordRealmBridge extends AbstractPrincipalMappingUserPasswordRealmBridge {

    private static final GBeanInfo GBEAN_INFO;

    private URL propertyFileURL;

    public PropertiesFilePrincipalMappingUserPasswordRealmBridge() {
    }

    public PropertiesFilePrincipalMappingUserPasswordRealmBridge(String targetRealm,
                                                                 Class principalSourceType,
                                                                 String principalTargetCallbackName,
                                                                 Class userNameSourceType,
                                                                 String userNameTargetCallbackName,
                                                                 Class passwordSourceType,
                                                                 URL propertyFileURL) {
        super(targetRealm,
              principalSourceType,
              principalTargetCallbackName,
              userNameSourceType,
              userNameTargetCallbackName,
              passwordSourceType);
        this.propertyFileURL = propertyFileURL;
    }

    public URL getPropertyFileURL() {
        return propertyFileURL;
    }

    public void setPropertyFileURL(URL propertyFileURL) throws IOException {
        this.propertyFileURL = propertyFileURL;
        principalMap.clear();
        userNameMap.clear();
        passwordMap.clear();
        Properties properties = new Properties();
        properties.load(propertyFileURL.openStream());
        setMaps(properties, principalMap, userNameMap, passwordMap);
    }

    void setMaps(Properties properties, Map principalMap, Map userNameMap, Map passwordMap) {
        for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            String values = (String) entry.getValue();
            StringTokenizer tokenizer = new StringTokenizer(values, ":");
            String targetPrincipal = tokenizer.nextToken();
            String targetUserName = tokenizer.nextToken();
            char[] targetPassword = tokenizer.nextToken().toCharArray();
            principalMap.put(key, targetPrincipal);
            userNameMap.put(key, targetUserName);
            passwordMap.put(key, targetPassword);
        }
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(PropertiesFilePrincipalMappingUserPasswordRealmBridge.class.getName(), AbstractPrincipalMappingUserPasswordRealmBridge.getGBeanInfo());
        infoFactory.addAttribute(new GAttributeInfo("PropertyFileURL", true));
        infoFactory.setConstructor(new GConstructorInfo(new String[]{"TargetRealm", "PrincipalSourceType", "PrincipalTargetCallbackName", "UserNameSourceType", "UserNameTargetCallbackName", "PasswordSourceType", "PropertyFileURL"},
                                                        new Class[]{String.class, Class.class, String.class, Class.class, String.class, Class.class, URL.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
