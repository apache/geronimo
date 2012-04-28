/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.tomcat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.naming.NamingEntry;
import org.apache.naming.resources.BaseDirContext;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.ResourceAttributes;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory Context implementation helper class.
 *
 * @version $Revision$ $Date$
 */
public class BundleDirContext extends BaseDirContext {

    private static final Logger logger = LoggerFactory.getLogger(BundleDirContext.class);

    private final Bundle bundle;

    private String nameInNamespace;

    private final String path;

    private File rootDocPath;

    public BundleDirContext(Bundle bundle, String path, File rootDocPath) {
        this(bundle, path, Collections.<DirContext> emptyList(), rootDocPath);
    }

    public BundleDirContext(Bundle bundle, String path, List<DirContext> altDirContext, File rootDocPath) {
        this.bundle = bundle;
        this.path = path;
        this.altDirContexts.addAll(altDirContext);
        this.rootDocPath = rootDocPath;
    }

    @Override
    public void bind(String name, Object obj, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    /**
     * Get the current alias configuration in String form. If no aliases are
     * configured, an empty string will be returned.
     */
    public String getAliases() {
        StringBuilder result = new StringBuilder();
        Iterator<Entry<String, BaseDirContext>> iter = aliases.entrySet().iterator();
        boolean first = true;
        try {
            while (iter.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    result.append(',');
                }
                Entry<String, BaseDirContext> entry = iter.next();
                result.append(entry.getKey());
                result.append('=');
                result.append(((BundleDirContext) entry.getValue()).getNameInNamespace());
            }
            return result.toString();
        } catch (NamingException e) {
            //FIXME ?
            return "";
        }
    }

    @Override
    public void setDocBase(String docBase) {
        //TODO Override setDocBase with an empty block to avoid NullPointerException
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        return nameInNamespace;
    }

    @Override
    public DirContext getSchema(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext getSchemaClassDefinition(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        name = getName(name);
        Enumeration<String> entries = BundleUtils.getEntryPaths(bundle, name);
        if (entries == null) {
            throw new NamingException("Resource not found: " + name);
        } else {
            return new NameClassPairEnumeration(name, entries);
        }
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    @Override
    public void modifyAttributes(String name, int mod_op, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rename(String arg0, String arg1) throws NamingException {
        // TODO Auto-generated method stub
    }

    @Override
    public NamingEnumeration<SearchResult> search(String arg0, Attributes matchingAttributes) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filter, Object[] filterArgs, SearchControls cons) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void setAliases(String theAliases) {
        // Overwrite whatever is currently set
        aliases.clear();
        if (theAliases == null || theAliases.length() == 0)
            return;
        String[] kvps = theAliases.split(",");
        for (String kvp : kvps) {
            String[] kv = kvp.split("=");
            if (kv.length != 2 || kv[0].length() == 0 || kv[1].length() == 0)
                throw new IllegalArgumentException(sm.getString("resources.invalidAliasMapping", kvp));
            File aliasLoc = new File(kv[1]);
            if (!aliasLoc.exists()) {
                throw new IllegalArgumentException(sm.getString("resources.invalidAliasNotExist", kv[1]));
            }
            //            if (kv[1].endsWith(".war") && !(aliasLoc.isDirectory())) {
            //                context = new WARDirContext();
            //            } else if (aliasLoc.isDirectory()) {
            //                context = new FileDirContext();
            //            } else {
            //                throw new IllegalArgumentException(
            //                        sm.getString("resources.invalidAliasFile", kv[1]));
            //            }
            //            context.setDocBase(kv[1]);
            //            addAlias(kv[0], context);
        }
    }

    public void setNameInNamespace(String nameInNamespace) {
        this.nameInNamespace = nameInNamespace;
    }

    @Override
    public void unbind(String arg0) throws NamingException {
        // TODO Auto-generated method stub
    }

    @Override
    protected Attributes doGetAttributes(String name, String[] attrIds) throws NamingException {
        name = getName(name);
        URL url;
        try {
            url = BundleUtils.getEntry(bundle, name);
        } catch (MalformedURLException e) {
            logger.warn("MalformedURLException when getting entry:" + name + " from bundle " + bundle.getSymbolicName(), e);
            url = null;
        }
        if (url == null) {
            return null;
        }
        ResourceAttributes resourceAttributes = new ResourceAttributes();
        resourceAttributes.setLastModified(bundle.getLastModified());
        resourceAttributes.setCreation(bundle.getLastModified());
        resourceAttributes.setName(url.toString());
        //TODO How to set content length ?
        return resourceAttributes;
    }

    @Override
    protected String doGetRealPath(String path) {
        return rootDocPath == null ? null : new File(rootDocPath, path).getAbsolutePath();
    }

    @Override
    protected List<NamingEntry> doListBindings(String name) throws NamingException {
        name = getName(name);
        Enumeration<String> entries = BundleUtils.getEntryPaths(bundle, name);
        if (entries == null) {
            throw new NamingException("Resource not found: " + name);
        } else {
            return Collections.list(new BindingEnumeration(bundle, name, entries, rootDocPath));
        }
    }

    @Override
    protected Object doLookup(String name) {
        name = getName(name);
        URL url;
        try {
            url = BundleUtils.getEntry(bundle, name);
        } catch (MalformedURLException e) {
            logger.warn("MalformedURLException when getting entry:" + name + " from bundle " + bundle.getSymbolicName(), e);
            url = null;
        }
        if (url == null) {
            return null;
        }
        if (url.toString().endsWith("/")) {
            return new BundleDirContext(bundle, name, rootDocPath == null ? null : new File(rootDocPath, name));
        } else {
            return new URLResource(url);
        }
    }


    private String getName(String name) {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (path != null) {
            name = path + "/" + name;
        }
        return name;
    }

    private static String getBasePath(String name) {
        if (name != null && !name.endsWith("/")) {
            return name + "/";
        } else {
            return name;
        }
    }

    private static String removeSlash(String name) {
        if (name.endsWith("/")) {
            return name.substring(0, name.length() - 1);
        } else {
            return name;
        }
    }

    private static class NameClassPairEnumeration implements NamingEnumeration<NameClassPair> {

        private String basePath;

        private Enumeration<String> entries;

        public NameClassPairEnumeration(String basePath, Enumeration<String> entries) {
            this.basePath = getBasePath(basePath);
            this.entries = entries;
        }

        public NameClassPair next() throws NamingException {
            return nextElement();
        }

        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        public void close() throws NamingException {
        }

        public boolean hasMoreElements() {
            return (entries != null && entries.hasMoreElements());
        }

        public NameClassPair nextElement() {
            String name = entries.nextElement();
            String relativeName = getRelativeName(name);
            if (name.endsWith("/")) {
                return new Binding(removeSlash(relativeName), DirContext.class.getName());
            } else {
                return new Binding(relativeName, String.class.getName());
            }
        }

        private String getRelativeName(String name) {
            if (basePath != null && name.startsWith(basePath)) {
                return name.substring(basePath.length());
            } else {
                return name;
            }
        }
    }

    private static class BindingEnumeration implements NamingEnumeration<NamingEntry> {

        private Bundle bundle;

        private String basePath;

        private Enumeration<String> entries;

        private File rootDocPath;

        public BindingEnumeration(Bundle bundle, String basePath, Enumeration<String> entries, File rootDocPath) {
            this.bundle = bundle;
            this.basePath = getBasePath(basePath);
            this.entries = entries;
            this.rootDocPath = rootDocPath;
        }

        public NamingEntry next() throws NamingException {
            return nextElement();
        }

        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        public void close() throws NamingException {
        }

        public boolean hasMoreElements() {
            return (entries != null && entries.hasMoreElements());
        }

        public NamingEntry nextElement() {
            String name = entries.nextElement();
            String relativeName = getRelativeName(name);
            if (name.endsWith("/")) {
                return new NamingEntry(removeSlash(relativeName), new BundleDirContext(bundle, name, rootDocPath == null ? null : new File(rootDocPath, relativeName)), NamingEntry.CONTEXT);
            } else {
                return new NamingEntry(relativeName, relativeName, NamingEntry.ENTRY);
            }
        }

        private String getRelativeName(String name) {
            if (basePath != null && name.startsWith(basePath)) {
                return name.substring(basePath.length());
            } else {
                return name;
            }
        }
    }

    private static class URLResource extends Resource {

        private final URL url;

        private URLResource(URL url) {
            this.url = url;
        }

        @Override
        public InputStream streamContent() throws IOException {
            return url.openStream();
        }

        @Override
        public byte[] getContent() {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                InputStream in = url.openStream();
                int size;
                while ((size = in.read(buf)) > 0) {
                    baos.write(buf, 0, size);
                }
                in.close();
                return baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setContent(InputStream inputStream) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setContent(byte[] binaryContent) {
            throw new RuntimeException("Not supported");
        }
    }
}
