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

package org.apache.geronimo.web25.deployment.utils;

import java.text.MessageFormat;

/**
 * @version $Rev$ $Date$
 */
public class WebDeploymentMessageUtils {

    private static MessageFormat DUPLICATE_JNDIREF_MESSAGE_FORMAT = new MessageFormat(
            "It is not allowed that the same {0} with value {1} are configured in more than two web-fragment file or annotations while it is not present in web.xml file : ( 1 ) {2}  ( 2 ) {3}");

    private static MessageFormat DUPLICATE_KEY_VALUE_MESSAGE_FORMAT = new MessageFormat(
            "Conflict of configuration {1} named {2} of different values in {0} are found below : \n( 1 ) {3} {4} in jar {5} \n( 2 ) {3} {6} in jar {7} \n You might add an entry in the web.xml to take precedence all of them.");

    private static MessageFormat DUPLICATE_VALUE_MESSAGE_FORMAT = new MessageFormat(
            "Conflict of configuration {1}  of different values in {0} are found below : \n( 1 ) {2}  in jar {3} \n( 2 ) {4} in jar {5} \n You might add an entry in the web.xml to take precedence all of them.");

    private static MessageFormat MULTIPLE_CONFIGURATION_WEB_FRAGMENT_WARNING_MESSAGE_FORMAT = new MessageFormat(
            "Only one element of {0} could be configured in web-fragment.xml  {1}  only the first one will be considered");

    private static MessageFormat MULTIPLE_CONFIGURATION_WEB_APP_ERROR_MESSAGE_FORMAT = new MessageFormat("Only one element of {0} could be configured in web.xml");

    private static MessageFormat MULTIPLE_CONFIGURATION_WEB_FRAGMENT_ERROR_MESSAGE_FORMAT = new MessageFormat("Only one element of {0} could be configured in web-fragment.xml of the jar file {1}");

    private static MessageFormat INVALID_URL_PATTERN_ERROR_MESSAGE = new MessageFormat("Invalid character CR(#xD) or LF(#xA) is found in <url-pattern> {2} of {0} {1} from {3}");

    public static String createDuplicateJNDIRefMessage(String elementName, String refName, String jarUrlA, String jarUrlB) {
        return DUPLICATE_JNDIREF_MESSAGE_FORMAT.format(new Object[] { elementName, refName, jarUrlA, jarUrlB });
    }

    public static String createDuplicateKeyValueMessage(String parentElement, String keyElementName, String keyName, String valueElementName, String valueA, String jarUrlA, String valueB,
            String jarUrlB) {
        return DUPLICATE_KEY_VALUE_MESSAGE_FORMAT.format(new Object[] { parentElement, keyElementName, keyName, valueElementName, valueA, jarUrlA, valueB, jarUrlB });
    }

    public static String createDuplicateValueMessage(String parentElement, String elementName, String valueA, String jarUrlA, String valueB, String jarUrlB) {
        return DUPLICATE_VALUE_MESSAGE_FORMAT.format(new Object[] { parentElement, elementName, valueA, jarUrlA, valueB, jarUrlB });
    }

    public static String createMultipleConfigurationWarningMessage(String elementName, String jarUrl) {
        return MULTIPLE_CONFIGURATION_WEB_FRAGMENT_WARNING_MESSAGE_FORMAT.format(new Object[] { elementName, jarUrl });
    }

    public static String createMultipleConfigurationWebAppErrorMessage(String elementName) {
        return MULTIPLE_CONFIGURATION_WEB_APP_ERROR_MESSAGE_FORMAT.format(new Object[] { elementName });
    }

    public static String createMultipleConfigurationWebFragmentErrorMessage(String elementName, String jarUrl) {
        return MULTIPLE_CONFIGURATION_WEB_FRAGMENT_ERROR_MESSAGE_FORMAT.format(new Object[] { elementName, jarUrl });
    }

    public static String createInvalidUrlPatternErrorMessage(String parentElementName, String parentElement, String urlPattern, String location) {
        return INVALID_URL_PATTERN_ERROR_MESSAGE.format(new Object[] { parentElementName, parentElement, urlPattern, location });
    }
}
