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

package org.apache.geronimo.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import jline.Terminal;
import org.apache.geronimo.gshell.ansi.RenderWriter;
import org.apache.geronimo.gshell.branding.Branding;
import org.apache.geronimo.gshell.branding.BrandingSupport;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

/**
 * Provides the branding for Geronimo usage of GShell.
 *
 * @version $Rev$ $Date$
 */
@Component(role=Branding.class, hint="geronimo")
public class GeronimoBranding
    extends BrandingSupport
{
    //
    // FIXME: This needs work before it can really be used...
    //
    
    // @Requirement
    // private VersionLoader versionLoader;

    @Requirement
    private Terminal terminal;

    //
    // FIXME: Don't override this... leave things as 'gshell' until we have more use-cases for branding fluff
    //
    
    public String getName() {
        return "gshell";
    }
    
    public String getDisplayName() {
        return "Apache Geronimo";
    }

    public String getProgramName() {
        //
        // FIXME: For now we leave this as 'gsh' ...
        //
        return System.getProperty("program.name", "gsh");
    }

    public String getAbout() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new RenderWriter(writer);

        out.println("For information about @|cyan " + getDisplayName() + "|, visit:");
        out.println("    @|bold http://geronimo.apache.org| ");
        out.flush();

        return writer.toString();
    }

    //
    // HACK: Just duplicating the PropertiesVersionLoader here for now
    //
    
    private Properties props;

    public String getVersion() {
        if (props == null) {
            String resourceName = "version.properties";
            InputStream input = getClass().getResourceAsStream(resourceName);
            assert input != null;

            try {
                props = new Properties();
                props.load(input);
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to load: " + resourceName, e);
            }
            finally {
                try {
                    input.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
        
        return props.getProperty("version");
    }

    public String getWelcomeBanner() {
        StringWriter writer = new StringWriter();
        PrintWriter out = new RenderWriter(writer);

        out.println("@|bold " + getDisplayName() + "| (" + getVersion() + ")");
        out.println();
        out.println("Type '@|bold help|' for more information.");

        // If we can't tell, or have something bogus then use a reasonable default
        int width = terminal.getTerminalWidth();
        if (width < 1) {
            width = 80;
        }
        
        out.print(StringUtils.repeat("-", width - 1));

        out.flush();

        return writer.toString();
    }
}
