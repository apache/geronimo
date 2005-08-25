/**
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
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
package org.apache.geronimo.ui.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Translated messages.
 */
public class Messages extends NLS {

    public static String editorTabGeneral;

    public static String editorTabNaming;

    public static String editorTabSecurity;

    public static String editorTabDependencies;

    public static String errorCouldNotOpenFile;

    public static String editorTitle;

    public static String editorSectionGeneralTitle;

    public static String editorSectionGeneralDescription;

    public static String editorConfigId;

    public static String editorParentId;

    public static String editorContextRoot;

    public static String editorClassloader;

    public static String editorClassloaderServer;

    public static String editorClassloaderWebApp;

    public static String securityRealmName;

    //

    public static String editorSectionSecurityRolesTitle;

    public static String editorSectionSecurityRolesDescription;
    
    public static String name;
    
    public static String description;

    public static String name;

    public static String description;

    //

    public static String editorResourceRefDescription;

    public static String editorResourceRefTitle;

    public static String editorResRefTargetNameTitle;

    public static String editorResRefLinkTitle;

    public static String editorResRefNameTitle;

    //

    public static String editorResourceEnvRefDescription;

    public static String editorResourceEnvRefTitle;

    public static String editorResEnvRefMsgDestTitle;

    public static String editorResEnvRefNameTitle;

    //

    public static String editorEjbLocalRefDescription;

    public static String editorEjbLocalRefTitle;

    public static String editorEjbRefTargetName;

    public static String editorEjbRefEjbLink;

    //

    public static String editorEjbRefDescription;

    public static String editorEjbRefTitle;

    // Buttons

    public static String add;

    public static String remove;

    public static String edit;

    // Wizard/Wizard Pages

    public static String wizardNewTitle_ResRef;

    public static String wizardEditTitle_ResRef;

    public static String wizardPageTitle_ResRef;

    public static String wizardPageDescription_ResRef;

    //

    public static String wizardNewTitle_ResEnvRef;

    public static String wizardEditTitle_ResEnvRef;

    public static String wizardPageTitle_ResEnvRef;

    public static String wizardPageDescription_ResEnvRef;

    //

    public static String wizardNewTitle_EjbRef;

    public static String wizardEditTitle_EjbRef;

    public static String wizardPageTitle_EjbRef;

    public static String wizardPageDescription_EjbRef;

    //

    public static String wizardNewTitle_EjbLocalRef;

    public static String wizardEditTitle_EjbLocalRef;

    public static String wizardPageTitle_EjbLocalRef;

    public static String wizardPageDescription_EjbLocalRef;

    //

    public static String wizardNewTitle_SecurityRole;

    public static String wizardEditTitle_SecurityRole;

    public static String wizardPageTitle_SecurityRole;

    public static String wizardPageDescription_SecurityRole;

    //

    public static String wizardNewTitle_SecurityRole;

    public static String wizardEditTitle_SecurityRole;

    public static String wizardPageTitle_SecurityRole;

    public static String wizardPageDescription_SecurityRole;

    public static String editorSectionSecurityTitle;

    public static String editorSectionSecurityDescription;

    public static String username;

    public static String password;

    static {
        NLS.initializeMessages(GeronimoUIPlugin.PLUGIN_ID
                + ".internal.Messages", Messages.class);
    }
}