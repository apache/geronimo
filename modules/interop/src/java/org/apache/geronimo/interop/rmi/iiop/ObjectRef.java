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
package org.apache.geronimo.interop.rmi.iiop;

import org.apache.geronimo.interop.*;
import org.apache.geronimo.interop.rmi.iiop.client.*;
import org.apache.geronimo.interop.IOP.*;
import org.apache.geronimo.interop.IIOP.*;
import org.apache.geronimo.interop.security.*;
import org.apache.geronimo.interop.util.*;
import java.util.*;

public class ObjectRef extends CorbaObject
{
    //public static final Component $component = new Component(ObjectRef.class);

    public static ObjectRef _getInstance()
    {
        return new ObjectRef(); //(ObjectRef)$component.getInstance();
    }

    // -----------------------------------------------------------------------
    // public data
    // -----------------------------------------------------------------------

    public static final int IIOP_VERSION_1_0 = 0;
    public static final int IIOP_VERSION_1_1 = 1;
    public static final int IIOP_VERSION_1_2 = 2;

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private static FutureObject _defaultNamingContext = new FutureObject()
    {
        public Object evaluate()
        {
            Hashtable env = new Hashtable();
            return ClientNamingContext.getInstance(env);
        }
    };

    private static Version VERSION_1_1 = new Version((byte)1, (byte)1);

    private static Version VERSION_1_2 = new Version((byte)1, (byte)2);

    private static TaggedComponent[] NO_PROFILE_COMPONENTS = {};

    /*
    private static TaggedProfile AUTOMATIC_FAILOVER_PROFILE;

    static
    {
        AUTOMATIC_FAILOVER_PROFILE = new TaggedProfile();
        AUTOMATIC_FAILOVER_PROFILE.tag = AutomaticFailover.PROFILE_TAG;
        AUTOMATIC_FAILOVER_PROFILE.profile_data = ArrayUtil.EMPTY_BYTE_ARRAY;
    }
    */

    private int _iiopVersion = IIOP_VERSION_1_2;

    private org.apache.geronimo.interop.IOP.IOR _ior;

    private ClientNamingContext _namingContext;

    private String _repositoryID; // CORBA repository ID e.g. "RMI:xyz:0000000000000000"

    private int _protocol;

    private String _endpoint;

    private String _host;

    private int _port = -1;

    //private boolean _automaticFailover;

    public byte[] _objectKey;

    public byte[] _objectState;

    public long _objectVersion;

    // public methods
    
   public ObjectRef()
   {
   } 

    public Connection $connect()
    {
        try
        {
            Connection conn = $getNamingContext().getConnectionPool().get(_protocol, $getEndpoint(), this);
            conn.beforeInvoke();
            return conn;
        }
        catch (RuntimeException ex)
        {
            //if (_automaticFailover)
            //{
            //    throw new RetryInvokeException(ex);
            //}
            //else
            //{
                throw ex;
            //}
        }
    }

    /*
    public boolean $getAutomaticFailover()
    {
        return _automaticFailover;
    }
    */

    /*
    public void $setAutomaticFailover()
    {
        _automaticFailover = true;
    }
    */

    public int $getIiopVersion()
    {
        return _iiopVersion;
    }

    public void $setIiopVersion(int version)
    {
        _iiopVersion = version;
    }

    public String $getID()
    {
        if (_repositoryID == null)
        {
            return "";
        }
        else
        {
            return _repositoryID;
        }
    }

    public void $setID(String id)
    {
        _repositoryID = id;
    }

    public org.apache.geronimo.interop.IOP.IOR $getIOR()
    {
        if (_ior == null)
        {
            ProfileBody_1_1 profileBody = new ProfileBody_1_1();
            profileBody.iiop_version = _iiopVersion == IIOP_VERSION_1_1
                ? VERSION_1_1 : VERSION_1_2;
            if (_host == null || _host.length() == 0)
            {
                profileBody.host = ThreadContext.getDefaultRmiHost();
            }
            else
            {
                profileBody.host = _host;
            }
            if (_port == -1)
            {
                profileBody.port = (short)ThreadContext.getDefaultRmiPort();
            }
            else
            {
                profileBody.port = (short)_port;
            }
            profileBody.object_key = _objectKey;
            // TODO: if protocol using SSL, set port to 0 and set components
            profileBody.components = NO_PROFILE_COMPONENTS;

            TaggedProfile profile = new TaggedProfile();
            profile.tag = TAG_INTERNET_IOP.value;
            CdrOutputStream output = CdrOutputStream.getInstanceForEncapsulation();
            ProfileBody_1_1Helper.write(output, profileBody);
            profile.profile_data = output.getEncapsulation();

            IOR ior = new IOR();
            ior.type_id = $getID();
            ior.profiles = new TaggedProfile[] { profile };
            return ior;
        }
        return _ior;
    }

    public void $setIOR(org.apache.geronimo.interop.IOP.IOR ior)
    {
        _ior = ior;
        _endpoint = null;
        _objectKey = null;
        $getObjectKey(); // set _protocol, _host, _port, _objectKey
    }

    public ClientNamingContext $getNamingContext()
    {
        if (_namingContext == null)
        {
            _namingContext = (ClientNamingContext)_defaultNamingContext.getValue();
        }
        return _namingContext;
    }

    public void $setNamingContext(ClientNamingContext namingContext)
    {
        _namingContext = namingContext;
    }

    public int $getProtocol()
    {
        if (_objectKey == null)
        {
            $getObjectKey(); // to set _protocol
        }
        return _protocol;
    }

    public void $setProtocol(int protocol)
    {
        _protocol = protocol;
        _ior = null;
    }

    public String $getHost()
    {
        if (_objectKey == null)
        {
            $getObjectKey(); // to set _host
        }
        return _host;
    }

    public void $setHost(String host)
    {
        _host = host;
        _endpoint = null;
        _ior = null;
    }

    public int $getPort()
    {
        if (_objectKey == null)
        {
            $getObjectKey(); // to set _port
        }
        return _port;
    }

    public void $setPort(int port)
    {
        _port = port;
        _endpoint = null;
        _ior = null;
    }

    public String $getEndpoint()
    {
        if (_endpoint == null)
        {
            _endpoint = _host + ":" + _port;
        }
        return _endpoint;
    }

    public byte[] $getObjectKey()
    {
        if (_objectKey == null)
        {
            if (_ior == null)
            {
                throw new IllegalStateException("$getObjectKey: _ior == null && _objectKey = null");
            }
            int n = _ior.profiles.length;
            for (int i = 0; i < n; i++)
            {
                TaggedProfile profile = _ior.profiles[i];
                if (profile.tag == TAG_INTERNET_IOP.value)
                {
                    ProfileBody_1_1 profileBody;
                    CdrInputStream input = CdrInputStream.getInstanceForEncapsulation();
                    input.setEncapsulation(profile.profile_data);
                    profileBody = ProfileBody_1_1Helper.read(input);
                    input.recycle();

                    _protocol = Protocol.IIOP; // TODO: IIOP/SSL etc.
                    _iiopVersion = profileBody.iiop_version.minor;
                    _host = profileBody.host;
                    _port = UnsignedShort.intValue(profileBody.port);
                    _objectKey = profileBody.object_key;
                }
            }
        }
        return _objectKey;
    }

    public String $getObjectKeyString()
    {
        return UTF8.toString($getObjectKey());
    }

    public void $setObjectKey(byte[] objectKey)
    {
        _objectKey = objectKey;
        _ior = null;
    }

    public void $setObjectKey(String objectKey)
    {
        $setObjectKey(UTF8.fromString(objectKey));
    }

    public void $setObjectKey(String prefix, byte[] suffixBytes)
    {
        byte[] prefixBytes = UTF8.fromString(prefix);
        int p = prefixBytes.length;
        int s = suffixBytes.length;
        byte[] objectKey = new byte[p + 1 + s];
        System.arraycopy(prefixBytes, 0, objectKey, 0, p);
        objectKey[p] = (byte)':';
        System.arraycopy(suffixBytes, 0, objectKey, p + 1, s);
        $setObjectKey(objectKey);
    }

    public void $setObjectKey(Class compClass)
    {
        SimpleSubject subject = SimpleSubject.getCurrent();
        if (subject != null
            && (subject.getFlags() & SimpleSubject.FLAG_SESSION_MANAGER) != 0)
        {
            // Initialize for simple IDL interoperability.
            ObjectKey objectKey = new ObjectKey();
            objectKey.component = compClass.getName();
            objectKey.username = subject.getUsername();
            objectKey.password = subject.getPassword();
            byte[] key = objectKey.encode();
            key[0] = 'I';
            _iiopVersion = IIOP_VERSION_1_1;
            $setObjectKey(key);
        }
        else
        {
            // Initialize for RMI-IIOP.
            $setObjectKey(compClass.getName());
        }
    }

    public Object $getRequestKey()
    {
        return null;
    }
}
