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
package org.apache.geronimo.interop.naming;

import java.util.HashMap;
import javax.naming.NamingException;

import org.apache.geronimo.interop.adapter.Adapter;


public class NameService {
    protected static NameService _ns = null;

    public static NameService getInstance() {
        if (_ns == null) {
            synchronized (NameService.class) {
                if (_ns == null) {
                    _ns = new NameService();
                    _ns.init();
                }
            }
        }

        return _ns;
    }

    private org.apache.geronimo.interop.naming.InitialContext _context;

    /*
     * TODO: Do we need this method?
     */
    public void bindAdapter(Adapter adp) {
        NamingContext.getInstance(NameService.class).bindAdapter(adp);
    }

    public static org.apache.geronimo.interop.naming.InitialContext getInitialContext() {
        return getInstance()._context;
    }

    public HashMap getMap() {
        return _context.getMap();
    }

    public Object lookup(String name) throws NamingException {
        return _context.lookup(name);
    }

    protected void init() {
        _context = new org.apache.geronimo.interop.naming.InitialContext(null);
    }
}
