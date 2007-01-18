/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.openejb.deployment.ejbref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.naming.Reference;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedEJBRefException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.openejb.EjbReference;
import org.apache.geronimo.openejb.EjbDeployment;
import org.apache.geronimo.openejb.deployment.EjbInterface;

/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public abstract class AbstractEjbRefBuilder extends AbstractNamingBuilder {
    private final static Map<String, String> STATELESS = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATELESS_SESSION_BEAN);
    private final static Map<String, String> STATEFUL = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATEFUL_SESSION_BEAN);
    private final static Map<String, String> ENTITY = Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.ENTITY_BEAN);

    protected AbstractEjbRefBuilder(Environment defaultEnvironment) {
        super(defaultEnvironment);
    }

    protected Reference createEjbRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, AbstractNameQuery query, boolean isSession, String homeInterface, String businessInterface, boolean remote) throws DeploymentException {
        AbstractNameQuery match = getEjbRefQuery(refName, configuration, name, requiredModule, optionalModule, query, isSession, homeInterface, businessInterface, remote);
        return new EjbReference(configuration.getId(), match, remote);
    }

    protected AbstractNameQuery getEjbRefQuery(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, AbstractNameQuery query, boolean isSession, String homeInterface, String businessInterface, boolean remote) throws DeploymentException {
        AbstractNameQuery match;
        if (query != null) {
            if (remote) {
                checkRemoteProxyInfo(query, homeInterface, businessInterface, configuration);
            } else {
                checkLocalProxyInfo(query, homeInterface, businessInterface, configuration);
            }
            match = new AbstractNameQuery(query.getArtifact(), query.getName(), EjbDeployment.class.getName());
        } else if (name != null) {
            match = getMatch(refName, configuration, name, requiredModule, remote, isSession, homeInterface, businessInterface);
        } else {
            match = getImplicitMatch(refName, configuration, optionalModule, remote, isSession, homeInterface, businessInterface);
        }
        return match;
    }

    private AbstractNameQuery getMatch(String refName, Configuration context, String name, String module, boolean isRemote, boolean isSession, String home, String remote) throws DeploymentException {
        Map<String, String> nameQuery = new HashMap<String, String>();
        nameQuery.put(NameFactory.J2EE_NAME, name);
        if (module != null) {
            nameQuery.put(NameFactory.EJB_MODULE, module);
        }
        Artifact id = context.getId();
        Collection<AbstractName> matches = getMatchesFromName(isSession, nameQuery, context, id, isRemote, home, remote);
        if (matches.isEmpty()) {
            matches = getMatchesFromName(isSession, nameQuery, context, null, isRemote, home, remote);
        }
        if (matches.isEmpty()) {
            throw new UnresolvedEJBRefException(refName, !isRemote, isSession, home, remote, false);
        }
        AbstractName match;
        if (matches.size() == 1) {
            match = matches.iterator().next();
        } else {
            throw new UnresolvedEJBRefException(refName, !isRemote, isSession, home, remote, matches.size() > 0);
        }
        return new AbstractNameQuery(stripVersion(match.getArtifact()), match.getName(), EjbDeployment.class.getName());
    }

    private Collection<AbstractName> getMatchesFromName(boolean isSession, Map<String, String> nameQuery, Configuration context, Artifact id, boolean isRemote, String home, String remote) {
        Set<GBeanData> gbeanDatas = new HashSet<GBeanData>();
        if (isSession) {
            Map<String, String> q = new HashMap<String, String>(nameQuery);
            q.putAll(STATELESS);
            gbeanDatas.addAll(findGBeanDatas(context, id, q));

            q = new HashMap<String, String>(nameQuery);
            q.putAll(STATEFUL);
            gbeanDatas.addAll(findGBeanDatas(context, id, q));
        } else {
            Map<String, String> q = new HashMap<String, String>(nameQuery);
            q.putAll(ENTITY);
            gbeanDatas.addAll(findGBeanDatas(context, id, q));
        }

        Collection<AbstractName> matches = new ArrayList<AbstractName>();
        for (GBeanData data : gbeanDatas) {
            if (matchesProxyInfo(data, isRemote, home, remote)) {
                matches.add(data.getAbstractName());
            }
        }
        return matches;
    }

    @SuppressWarnings({"unchecked"})
    private Collection<GBeanData> findGBeanDatas(Configuration context, Artifact id, Map<String, String> q) {
        return context.findGBeanDatas(Collections.singleton(new AbstractNameQuery(id, q, EjbDeployment.class.getName())));
    }

    private AbstractNameQuery getImplicitMatch(String refName, Configuration context, String module, boolean isRemote, boolean isSession, String home, String remote) throws DeploymentException {
        Collection<AbstractName> matches = getMatchesFromName(isSession, Collections.<String, String>emptyMap(), context, context.getId(), isRemote, home, remote);
        if (matches.isEmpty()) {
            matches = getMatchesFromName(isSession, Collections.<String, String>emptyMap(), context, null, isRemote, home, remote);
        }
        if (matches.isEmpty()) {
            throw new UnresolvedEJBRefException(refName, false, isSession, home, remote, false);
        }
        AbstractName match;
        if (matches.size() == 1) {
            match = matches.iterator().next();
        } else {
            for (Iterator<AbstractName> iterator = matches.iterator(); iterator.hasNext();) {
                AbstractName objectName = iterator.next();
                if (module != null && !(objectName.getName().get(NameFactory.EJB_MODULE).equals(module))) {
                    iterator.remove();
                }
            }
            if (matches.size() == 1) {
                match = matches.iterator().next();
            } else {
                throw new UnresolvedEJBRefException(refName, false, isSession, home, remote, matches.size() > 0);
            }
        }
        return new AbstractNameQuery(match);
    }

    private boolean matchesProxyInfo(GBeanData data, boolean isRemote, String home, String remote) {
        if (isRemote) {
            return home.equals(getHomeInterface(data)) && remote.equals(getRemoteInterface(data));
        } else {
            return home.equals(getLocalHomeInterface(data)) && remote.equals(getLocalInterface(data));
        }
    }

    private void checkLocalProxyInfo(AbstractNameQuery query, String expectedLocalHome, String expectedLocal, Configuration configuration) throws DeploymentException {
        try {
            GBeanData data = configuration.findGBeanData(query);

            String actualLocalHome = getLocalHomeInterface(data);
            String actualLocal = getLocalInterface(data);
            if (actualLocalHome == null || actualLocal == null) {
                // EJBs like the MEJB don't have interfaces declared in the gbean data
                return;
            }

            if (!expectedLocalHome.equals(actualLocalHome) || !expectedLocal.equals(actualLocal)) {
                throw new DeploymentException("Reference interfaces do not match bean interfaces:\n" +
                        "reference localHome: " + expectedLocalHome + "\n" +
                        "ejb localHome: " + actualLocalHome + "\n" +
                        "reference local: " + expectedLocal + "\n" +
                        "ejb local: " + getLocalInterface(data));
            }
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not locate ejb matching " + query + " in configuration " + configuration.getId());
        }
    }

    private void checkRemoteProxyInfo(AbstractNameQuery query, String expectedHome, String expectedRemote, Configuration configuration) throws DeploymentException {
        try {
            GBeanData data = configuration.findGBeanData(query);

            String actualHome = getHomeInterface(data);
            String actualRemote = getRemoteInterface(data);
            if (actualHome == null || actualRemote == null) {
                // EJBs like the MEJB don't have interfaces declared in the gbean data
                return;
            }

            if (!expectedHome.equals(actualHome) || !expectedRemote.equals(actualRemote)) {
                throw new DeploymentException("Reference interfaces do not match bean interfaces:\n" +
                        "reference home: " + expectedHome + "\n" +
                        "ejb home: " + actualHome + "\n" +
                        "reference remote: " + expectedRemote + "\n" +
                        "ejb remote: " + actualRemote);
            }
        } catch (GBeanNotFoundException e) {
            // we can't verify remote ejb refs if the GBean can't be found
            // could be a reference to an ejb in another application that hasn't been loaded yet
        }
    }

    private static Artifact stripVersion(Artifact artifact) {
        return new Artifact(artifact.getGroupId(), artifact.getArtifactId(), (String) null, artifact.getType());
    }

    private static String getHomeInterface(GBeanData data) {
        return (String) data.getAttribute(EjbInterface.HOME.getAttributeName());
    }

    private static String getRemoteInterface(GBeanData data) {
        return (String) data.getAttribute(EjbInterface.REMOTE.getAttributeName());
    }

    private static String getLocalHomeInterface(GBeanData data) {
        return (String) data.getAttribute(EjbInterface.LOCAL_HOME.getAttributeName());
    }

    private static String getLocalInterface(GBeanData data) {
        return (String) data.getAttribute(EjbInterface.LOCAL.getAttributeName());
    }
}
