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

package javax.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public interface Cache extends Map
{
    public void addListener( CacheListener listender );

    public void clear();

    public boolean containsKey( Object key );

    public boolean containsValue( Object value );

    public Set entrySet();

    public boolean equals( Object obj);

    public Object get( Object obj );

    public Map getAll( Collection keys );

    public CacheEntry getCacheEntry( Object key );

    public CacheStatistics getCacheStatistics();

    public int hashCode();

    public boolean isEmpty();

    public Set keySet();

    public void load( Object obj );

    public void loadAll( Collection keys );

    public Object peek( Object key );

    public Object put( Object key, Object value );

    public void putAll( Map m );

    public Object remove( Object key );

    public void removeListener( CacheListener listener );

    public int size();

    public Collection values();
}
