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
package org.apache.geronimo.interop.rmi.iiop.client;

/**
 * corbaname and corbaloc urls
 */

public class UrlScheme
{
    class Addr
    {
        Addr(String h, int port, int ver)
        {
            _host = h;
            _port = port;
            _version = ver;
        }

        String _host;
        int    _port;
        int    _version;
    }

    static final int IIOP_1_0 = 0;
    static final int IIOP_1_1 = 1;
    static final int IIOP_1_2 = 2;

    private boolean _corbaloc, _corbaname;
    private java.util.ArrayList _addrList;
    private String _url;
    private String _namePrefix;
    private String _objKey;

    public UrlScheme(String url)
    {
        _url = url;
        _addrList = new java.util.ArrayList();
    }

    public String getHost(int i)
    {
        Addr addr = getEntry(i);
        return addr._host;
    }

    public int getPort(int i)
    {
        Addr addr = getEntry(i);
        return addr._port;
    }

    public int getVersion(int i)
    {
        Addr addr = getEntry(i);
        return addr._version;
    }

    public String getObjectKey()
    {
        return _objKey;
    }

    public String getNamePrefix()
    {
        return _namePrefix;
    }

    public int getAddressCount()
    {
        return _addrList.size();
    }

    private Addr getEntry(int i)
    {
        try
        {
            return (Addr)_addrList.get(i);
        }
        catch(IndexOutOfBoundsException e)
        {
            throw new IllegalArgumentException(_url);
        }
    }

    public static boolean canProcess(String url)
    {
        return url.startsWith("corbaloc:") || url.startsWith("corbaname:");
    }

    public boolean process()
    {
        boolean handled = false;

        String cloc = "corbaloc:";
        String cname = "corbaname:";
        
        _corbaloc = _url.startsWith(cloc);
        _corbaname =_url.startsWith(cname);

        if(_corbaloc || _corbaname)
        {
            try
            {
                int keySep = _url.indexOf("/");
                int nameSep = _url.indexOf("#");

                if(keySep > nameSep) //we may have "/" in a name
                {
                    keySep = -1;
                }
        
                int addrStart = _corbaloc ? "corbaloc:".length() : "corbaname:".length();
                String addrlist;
                
                if(keySep != -1)
                {
                    addrlist = _url.substring(addrStart, keySep);
                }
                else if(nameSep != -1)
                {
                    addrlist = _url.substring(addrStart, nameSep);
                }
                else
                {
                    addrlist = _url.substring(addrStart);
                }

                readAddrList(addrlist);
                readKeyAndName(keySep, nameSep);
            }
            catch(Exception e)
            {
                throw new IllegalArgumentException(_url);
            }
        }

        return handled;
    }

    //addr,addr...
    private void readAddrList(String addrlist)
    {
        java.util.StringTokenizer tk = new java.util.StringTokenizer(addrlist, ",");
        if(tk.countTokens() < 1)
        {
            throw new IllegalArgumentException(_url);
        }
        
        while(tk.hasMoreElements())
        {
            String addr = tk.nextToken();
            readAddr(addr);
        }
    }

    //supported protocol: iiop
    //addr       <-   <":" | "iiop:">[<version><host> [":" <port>]]
    //version    <-   <major>.<minor>"@" | empty_string

    private void readAddr(String addr)
    {
        String host;
        int port, ver = IIOP_1_2;

        int addrlen = addr.length();

        if(!addr.startsWith("iiop:") && !addr.startsWith(":"))
        {
            throw new IllegalArgumentException(_url);
        }

        int curindex = addr.indexOf(":") + 1;

        //VERSION
        if( (curindex + 3) < addrlen && addr.charAt(curindex + 3) == '@' )
        {
            if(addr.startsWith("1.0@", curindex))
            {
                ver = IIOP_1_0;
            }
            else if(addr.startsWith("1.1@", curindex))
            {
                ver = IIOP_1_1;
            }
            else if(addr.startsWith("1.2@", curindex))
            {
                ver = IIOP_1_2;
            }
            else
            {
                throw new IllegalArgumentException(_url);
            }
            curindex += 4;
        }

        //defaults
        host = "localhost";
        port = 2089; 

        //HOST
        if(curindex < addrlen)
        {
            //is it ipv6 address? (Enclosed between '[' and ']')
            if(addr.charAt(curindex) == '[')
            {
                int lastindex = addr.indexOf(']', curindex);
                if(-1 == lastindex)
                {
                    throw new IllegalArgumentException(_url);
                }
                host = addr.substring(curindex, lastindex + 1);
                curindex = lastindex + 1;
            }
            else if(addr.charAt(curindex) != ':')
            {
                int i = addr.indexOf(":", curindex);
                if(-1 == i)
                {
                    i = addrlen;
                }
                host = addr.substring(curindex, i);
                curindex = i + 1;
            }
        }

        //PORT
        if(curindex < addrlen)
        {
            try
            {
                String sport = addr.substring(curindex);
                port = new Integer(sport).intValue();
            }
            catch(NumberFormatException e)
            {
                throw new IllegalArgumentException(_url);
            }
        }

        _addrList.add(new Addr(host, port, ver));
    }

    //TODO: key may be escaped
    private void readKeyAndName(int keySep, int nameSep)
    {
        _objKey = "NameService";
        _namePrefix = "";

        if(keySep != -1)
        {
            int i = nameSep;
            if(-1 == i)
            {
                i = _url.length();
            }
            _objKey = _url.substring(keySep + 1, i);
        }

        if(nameSep != -1)
        {
            _namePrefix = _url.substring(nameSep + 1);
        }
    }
}
