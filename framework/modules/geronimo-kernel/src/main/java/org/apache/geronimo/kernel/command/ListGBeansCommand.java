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


package org.apache.geronimo.kernel.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Rev:$ $Date:$
 */
@Command(scope = "gbeans", name = "list", description = "Lists all gbeans in the kernel.")
public class ListGBeansCommand extends KernelCommandSupport {

    @Option(name = "-q", aliases = {"--query"}, description = "Abstract name query URI", required = false, multiValued = false)
    String queryString = "";

    @Option(name = "-v", aliases = {"--verbose"}, description = "Show the state reason for non-running gbeans", required = false, multiValued = false)
    boolean verbose;

    @Override
    protected void doExecute(Kernel kernel) throws Exception {
        AbstractNameQuery query = new AbstractNameQuery(URI.create(queryString));
        List<AbstractName> names = new ArrayList<AbstractName>(kernel.listGBeans(query));
        if (names.isEmpty()) {
            System.out.println("No gbeans found in kernel");
            return;
        }

        Collections.sort(names, new Comparator<AbstractName> () {
            @Override
            public int compare(AbstractName first, AbstractName second) {
                return first.toString().compareTo(second.toString());
            }
        });

        LinkedHashMap<AbstractName, Integer> states = new LinkedHashMap<AbstractName, Integer>();

        for (AbstractName name: names) {
            states.put(name, kernel.getGBeanState(name));
        }
        StringBuilder buf = new StringBuilder("State      Name:\n");
        for (Map.Entry<AbstractName, Integer> entry: states.entrySet()) {
            AbstractName name = entry.getKey();
            int state = entry.getValue();
            buf.append("[").append(State.fromInt(state)).append("]   ").append(name).append("\n");
            if (verbose) {
                if (state == State.FAILED_INDEX) {
                    buf.append("  state reason: ").append(kernel.getStateReason(name)).append("\n\n");
                } else if (state != State.RUNNING_INDEX) {
                    GBeanData data = kernel.getGBeanData(name);
                    if (!data.getDependencies().isEmpty()) {
                        buf.append(" non-running dependendencies:\n");
                        for (ReferencePatterns pattern : data.getDependencies()) {
                            if (pattern.isResolved()) {
                                AbstractName dependencyName = pattern.getAbstractName();
                                if (states.get(dependencyName) != State.RUNNING_INDEX) {
                                    buf.append("   state: ").append(states.get(dependencyName)).append(" resolved: ").append(dependencyName).append("\n");
                                }
                            } else {
                                Set<AbstractName> choices = kernel.listGBeans(pattern.getPatterns());
                                if (choices.isEmpty()) {
                                    buf.append("  unresolved: ").append(pattern).append("\n");
                                }
                            }
                        }
                    }
                    if (!data.getReferences().isEmpty()) {
                        GBeanInfo info = data.getGBeanInfo();
                        buf.append("  non-running references:\n");
                        for (Map.Entry<String, ReferencePatterns> refEntry : data.getReferences().entrySet()) {
                            ReferencePatterns pattern = refEntry.getValue();
                            GReferenceInfo refInfo = info.getReference(refEntry.getKey());
                            boolean multivalued = refInfo.getProxyType().equals(Collection.class.getName());
                            if (!multivalued) {
                                if (pattern.isResolved()) {
                                    AbstractName dependencyName = pattern.getAbstractName();
                                    if (states.get(dependencyName) != State.RUNNING_INDEX) {
                                        buf.append("   state: [").append(State.fromInt(states.get(dependencyName))).append("] resolved: ").append(dependencyName).append("\n");
                                    }
                                } else {
                                    Set<AbstractName> choices = kernel.listGBeans(pattern.getPatterns());
                                    if (choices.isEmpty()) {
                                        buf.append("  unresolved: ").append(pattern).append("\n");
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        System.out.println(buf.toString());

    }
}
