/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.2 $ $Date: 2004/02/17 00:05:39 $
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
