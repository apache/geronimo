/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.twiddle.command;

import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * ???
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:17 $
 */
public class CommandPathParser
{
    public static final String SEPARATOR = "/";
    
    protected String path;
    
    public CommandPathParser(final String path)
    {
        this.path = path.trim();
    }
    
    public String getPath()
    {
        return path;
    }
    
    public boolean isAbsolute()
    {
        return path.startsWith(SEPARATOR);
    }
    
    public String[] elements()
    {
        List list = new LinkedList();
        
        if (isAbsolute()) {
            list.add(SEPARATOR);
        }
        
        StringTokenizer stok = new StringTokenizer(path, SEPARATOR);
        while (stok.hasMoreTokens()) {
            list.add(stok.nextToken());
        }
        
        return (String[]) list.toArray(new String[list.size()]);
    }
}
