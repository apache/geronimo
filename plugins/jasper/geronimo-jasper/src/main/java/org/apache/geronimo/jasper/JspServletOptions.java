/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.apache.geronimo.jasper;

import java.io.File;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.TagPluginManager;
import org.apache.jasper.compiler.TldLocationsCache;

public class JspServletOptions implements Options {

    EmbeddedServletOptions delegate;

    TldLocationsCache tldLocationsCache;

    /**
     * Create an EmbeddedServletOptions object using data available from
     * ServletConfig and ServletContext.  Most calls are delegated to it.
     */
    public JspServletOptions(ServletConfig config, ServletContext context) {
        delegate = new EmbeddedServletOptions(config, context);
        tldLocationsCache = new GeronimoTldLocationsCache(context);
    }

    public boolean genStringAsCharArray() {
        return delegate.genStringAsCharArray();
    }

    public Map<String, TagLibraryInfo> getCache() {
        return delegate.getCache();
    }

    public int getCheckInterval() {
        return delegate.getCheckInterval();
    }

    public boolean getClassDebugInfo() {
        return delegate.getClassDebugInfo();
    }

    public String getClassPath() {
        return delegate.getClassPath();
    }

    public String getCompiler() {
        return delegate.getCompiler();
    }

    public String getCompilerClassName() {
        return delegate.getCompilerClassName();
    }

    public String getCompilerSourceVM() {
        return delegate.getCompilerSourceVM();
    }

    public String getCompilerTargetVM() {
        return delegate.getCompilerTargetVM();
    }

    public boolean getDevelopment() {
        return delegate.getDevelopment();
    }

    public boolean getDisplaySourceFragment() {
        return delegate.getDisplaySourceFragment();
    }

    public boolean getErrorOnUseBeanInvalidClassAttribute() {
        return delegate.getErrorOnUseBeanInvalidClassAttribute();
    }

    public boolean getFork() {
        return delegate.getFork();
    }

    public String getIeClassId() {
        return delegate.getIeClassId();
    }

    public String getJavaEncoding() {
        return delegate.getJavaEncoding();
    }

    public JspConfig getJspConfig() {
        return delegate.getJspConfig();
    }

    public boolean getKeepGenerated() {
        return delegate.getKeepGenerated();
    }

    public boolean getMappedFile() {
        return delegate.getMappedFile();
    }

    public int getModificationTestInterval() {
        return delegate.getModificationTestInterval();
    }

    public File getScratchDir() {
        return delegate.getScratchDir();
    }

    public TagPluginManager getTagPluginManager() {
        return delegate.getTagPluginManager();
    }

    public TldLocationsCache getTldLocationsCache() {
        return tldLocationsCache;
    }

    public boolean getTrimSpaces() {
        return delegate.getTrimSpaces();
    }

    public boolean isCaching() {
        return delegate.isCaching();
    }

    public boolean isPoolingEnabled() {
        return delegate.isPoolingEnabled();
    }

    public boolean isSmapDumped() {
        return delegate.isSmapDumped();
    }

    public boolean isSmapSuppressed() {
        return delegate.isSmapSuppressed();
    }

    public boolean isXpoweredBy() {
        return delegate.isXpoweredBy();
    }

}
