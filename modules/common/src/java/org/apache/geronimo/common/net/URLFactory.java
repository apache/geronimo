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

package org.apache.geronimo.common.net;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.IOException;
import java.io.File;

import org.apache.geronimo.common.NullArgumentException;

/**
 * A helper for creating URL instances.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:03 $
 */
public class URLFactory
{
    /**
     * Make a URL from the given string.
     *
     * <p>If the string is an invalid URL then it will be converted into a
     *    file URL.
     *
     * @param urlspec           The string to construct a URL for.
     * @param relativePrefix    The string to prepend to relative file
     *                          paths, or null to disable prepending.
     * @return                  A URL for the given string.
     *
     * @throws MalformedURLException  Could not make a URL for the given string.
     */
    public static URL create(String urlspec, final String relativePrefix)
        throws MalformedURLException
    {
        if (urlspec == null) {
            throw new NullArgumentException("urlspec");
        }
        
        urlspec = urlspec.trim();
        URL url;
        
        try {
            url = new URL(urlspec);
            if (url.getProtocol().equals("file")) {
                url = createFromFilespec(url.getFile(), relativePrefix);
            }
        }
        catch (Exception e) {
            url = createFromFilespec(urlspec, relativePrefix);
        }
        
        return url;
    }
    
    /**
     * Make a URL from the given string.
     *
     * @see #create(String,String)
     *
     * @param urlspec    The string to construct a URL for.
     * @return           A URL for the given string.
     *
     * @throws MalformedURLException  Could not make a URL for the given string.
     */
    public static URL create(final String urlspec) throws MalformedURLException
    {
        return create(urlspec, null);
    }
    
    public static URL createFromFilespec(final String filespec, final String relativePrefix)
        throws MalformedURLException
    {
        // make sure the file is absolute 
        File file = new File(filespec);
        
        // if we have a prefix and the file is not abs then prepend
        if (relativePrefix != null && !file.isAbsolute()) {
            file = new File(relativePrefix, filespec);
        }
        
        return create(file);
    }
    
    public static URL create(final File file)
        throws MalformedURLException
    {
        return file.toURI().toURL();
    }
}

