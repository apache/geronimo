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

import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class GeronimoUIPlugin extends AbstractUIPlugin {
    protected static final String PLUGIN_ID = "org.apache.geronimo.ui";

    private static final String ICONS_DIRECTORY = "icons/";

    private static String iconLocation;

    private static GeronimoUIPlugin singleton;

    /**
     * The constructor.
     */
    public GeronimoUIPlugin() {
        super();
        singleton = this;
    }

    /**
     * Returns the singleton instance of this plugin.
     * 
     * @return org.apache.geronimo.ui.internal.GeronimoUIPlugin
     */
    public static GeronimoUIPlugin getInstance() {
        return singleton;
    }

    public static String getIconLocation() {
        if (iconLocation == null) {
            try {
                iconLocation = Platform.resolve(
                        GeronimoUIPlugin.getInstance().getBundle()
                                .getEntry("/")).getPath()
                        + ICONS_DIRECTORY;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return iconLocation;
    }
}