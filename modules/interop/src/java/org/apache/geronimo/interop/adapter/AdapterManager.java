/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.adapter;

import org.openejb.EJBContainer;
import org.openejb.EJBComponentType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.*;
import org.apache.geronimo.interop.naming.NameService;

import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

public class AdapterManager implements ReferenceCollectionListener {

    private final Log       log = LogFactory.getLog(AdapterManager.class);
    private HashMap         homeAdapters;
    private HashMap         remoteAdapters;
    private Collection      containers = Collections.EMPTY_SET;
    private ClassLoader     classLoader;

    private NameService     nameService = NameService.getInstance();

    /*
     * This is a singleton GBean.  Do not reference it directly, but use a
     * GBean reference to get it.
     */
    public AdapterManager()
    {
        this.homeAdapters = null;
        this.remoteAdapters = null;
        this.containers = null;
        this.classLoader = null;
    }

    public AdapterManager( ClassLoader classLoader, Collection containers ) {
        log.debug( "AdapterManager(): containers = " + containers );

        this.classLoader = classLoader;

        ReferenceCollection ref = (ReferenceCollection) containers;
        ref.addReferenceCollectionListener(this);

        this.containers = containers;

        int len = ( containers != null ? containers.size() : 20 );
        homeAdapters = new HashMap( len );
        remoteAdapters = new HashMap( len );

        // Todo: Add the containers .. invoke memberAdded for each element in containers.

        registerNameServer();
    }

    protected void registerNameServer()
    {
        //
        // NameService
        //

        String bindNames[] = new String[] { "NameService" };
        String remoteClassName = "org.apache.geronimo.interop.rmi.iiop.server.ServerNamingContext";
        //String remoteInterfaceName = "org.apache.geronimo.interop.rmi.iiop.NameService";
        String remoteInterfaceName = "org.apache.geronimo.interop.CosNaming.NamingContext";
        String ids[] = new String[] { "IDL:omg.org/CosNaming/NamingContext:1.0",
                                      "IDL:omg.org/CosNaming/NamingContextExt:1.0" };                                      
        ClassLoader cl = this.getClass().getClassLoader();

        CorbaAdapter a = new CorbaAdapter( bindNames, ids, remoteClassName, remoteInterfaceName, cl );
        a.start();
        registerRemoteAdapter(a);
    }

    protected Adapter getAdapter( Object adapterID ) {
        // more likely to be using the remoteadapter...
        Adapter rc = getRemoteAdapter( adapterID );
        if (rc == null)
        {
            rc = getHomeAdapter( adapterID );
        }
        return rc;
    }

    protected Adapter getHomeAdapter( Object adapterID )
    {
        return (Adapter)homeAdapters.get( adapterID );
    }

    protected Adapter getRemoteAdapter( Object adapterID )
    {
        return (Adapter)remoteAdapters.get( adapterID );
    }

    protected void registerHomeAdapter( Adapter adapter )
    {
        homeAdapters.put( adapter.getAdapterID(), adapter );
    }

    protected void registerRemoteAdapter( Adapter adapter )
    {
        remoteAdapters.put( adapter.getAdapterID(), adapter );
    }

    protected void unregisterHomeAdapter( Adapter adapter )
    {
        homeAdapters.remove( adapter.getAdapterID() );
    }

    protected void unregisterRemoteAdapter( Adapter adapter )
    {
        remoteAdapters.remove( adapter.getAdapterID() );
    }

    public void memberAdded(ReferenceCollectionEvent event) {
        EJBContainer container = (EJBContainer) event.getMember();

        log.debug( "AdapterManager.memberAdded(): container = " + container );
        log.debug( "AdapterManager.memberAdded(): containerID = " + container.getContainerID() );

        switch (container.getProxyInfo().getComponentType()) {
            case EJBComponentType.STATELESS:
                //generator = new AdapterStateless(container, orb, poa, tieLoader);
                break;
            case EJBComponentType.STATEFUL:
                //generator = new AdapterStateful(container, orb, poa, tieLoader);
                break;
            case EJBComponentType.BMP_ENTITY:
            case EJBComponentType.CMP_ENTITY:
                //generator = new AdapterEntity(container, orb, poa, tieLoader);
                break;
            default:
                // throw new CORBAException("CORBA HomeAdapter does not handle MDB containers");
        }

        Adapter adapter = new HomeAdapter( container );
        adapter.start();
        registerHomeAdapter( adapter );

        adapter = new RemoteAdapter( container );
        adapter.start();
        registerRemoteAdapter( adapter );
    }

    public void memberRemoved(ReferenceCollectionEvent event) {
        EJBContainer container = (EJBContainer) event.getMember();

        log.debug( "AdapterManager.memberRemoved(): container = " + container );
        log.debug( "AdapterManager.memberRemoved(): containerID = " + container.getContainerID() );

        Adapter adapter = getHomeAdapter( container.getContainerID() );
        adapter.stop();
        unregisterHomeAdapter( adapter );

        adapter = getRemoteAdapter( container.getContainerID() );
        adapter.stop();
        unregisterRemoteAdapter( adapter );
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(AdapterManager.class);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addReference("containers", EJBContainer.class);

        infoFactory.setConstructor(new String[]{"classLoader", "containers"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
