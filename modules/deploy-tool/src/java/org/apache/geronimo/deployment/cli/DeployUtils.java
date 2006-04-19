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

package org.apache.geronimo.deployment.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.ConfigIDExtractor;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * Various helpers for deployment.
 *
 * @version $Rev$ $Date$
 */
public class DeployUtils extends ConfigIDExtractor {
    /**
     * Split up an output line so it indents at beginning and end (to fit in a
     * typical terminal) and doesn't break in the middle of a word.
     * @param source The unformatted String
     * @param indent The number of characters to indent on the left
     * @param endCol The maximum width of the entire line in characters,
     *               including indent (indent 10 with endCol 70 results
     *               in 60 "usable" characters).
     */
    public static String reformat(String source, int indent, int endCol) {
        if(endCol-indent < 10) {
            throw new IllegalArgumentException("This is ridiculous!");
        }
        StringBuffer buf = new StringBuffer((int)(source.length()*1.1));
        String prefix = indent == 0 ? "" : buildIndent(indent);
        try {
            BufferedReader in = new BufferedReader(new StringReader(source));
            String line;
            int pos;
            while((line = in.readLine()) != null) {
                if(buf.length() > 0) {
                    buf.append('\n');
                }
                while(line.length() > 0) {
                    line = prefix + line;
                    if(line.length() > endCol) {
                        pos = line.lastIndexOf(' ', endCol);
                        if(pos < indent) {
                            pos = line.indexOf(' ', endCol);
                            if(pos < indent) {
                                pos = line.length();
                            }
                        }
                        buf.append(line.substring(0, pos)).append('\n');
                        if(pos < line.length()-1) {
                            line = line.substring(pos+1);
                        } else {
                            break;
                        }
                    } else {
                        buf.append(line).append("\n");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError("This should be impossible");
        }
        return buf.toString();
    }

    private static String buildIndent(int indent) {
        StringBuffer buf = new StringBuffer(indent);
        for(int i=0; i<indent; i++) {
            buf.append(' ');
        }
        return buf.toString();
    }


    /**
     * Given a list of all available TargetModuleIDs and the name of a module,
     * find the TargetModuleIDs that represent that module.
     * @throws DeploymentException If no TargetModuleIDs have that module.
     */
    public static Collection identifyTargetModuleIDs(TargetModuleID[] allModules, String name) throws DeploymentException {
        List list = new LinkedList();
        int pos;
        if((pos = name.indexOf('|')) > -1) {
            String target = name.substring(0, pos);
            String module = name.substring(pos+1);
            Artifact artifact = Artifact.create(module);
            if(artifact.getGroupId() == null || artifact.getType() == null) {
                artifact = new Artifact(artifact.getGroupId() == null ? Artifact.DEFAULT_GROUP_ID : artifact.getGroupId(),
                        artifact.getArtifactId(), artifact.getVersion(),
                        artifact.getType() == null ? "car" : artifact.getType());
            }
            // First pass: exact match
            for(int i=0; i<allModules.length; i++) {
                if(allModules[i].getTarget().getName().equals(target) && artifact.matches(Artifact.create(allModules[i].getModuleID()))) {
                    list.add(allModules[i]);
                }
            }
        }
        if(!list.isEmpty()) {
            return list;
        }
        // second pass: module matches
        Artifact artifact = Artifact.create(name);
        if(artifact.getGroupId() == null || artifact.getType() == null) {
            artifact = new Artifact(artifact.getGroupId() == null ? Artifact.DEFAULT_GROUP_ID : artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion(),
                    artifact.getType() == null ? "car" : artifact.getType());
        }
        for(int i = 0; i < allModules.length; i++) {
            if(artifact.matches(Artifact.create(allModules[i].getModuleID()))) {
                list.add(allModules[i]);
            }
        }
        if(list.isEmpty()) {
            throw new DeploymentException(name+" does not appear to be a the name of a module " +
                    "available on the selected server. Perhaps it has already been " +
                    "stopped or undeployed?  If you're trying to specify a " +
                    "TargetModuleID, use the syntax TargetName|ModuleName instead. " +
                    "If you're not sure what's running, try the list-modules command.");
        }
        return list;
    }
}
