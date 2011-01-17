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

    @Override
    public boolean genStringAsCharArray() {
        return delegate.genStringAsCharArray();
    }

    @Override
    public Map<String, TagLibraryInfo> getCache() {
        return delegate.getCache();
    }

    @Override
    public int getCheckInterval() {
        return delegate.getCheckInterval();
    }

    @Override
    public boolean getClassDebugInfo() {
        return delegate.getClassDebugInfo();
    }

    @Override
    public String getClassPath() {
        return delegate.getClassPath();
    }

    @Override
    public String getCompiler() {
        return delegate.getCompiler();
    }

    @Override
    public String getCompilerClassName() {
        return delegate.getCompilerClassName();
    }

    @Override
    public String getCompilerSourceVM() {
        return delegate.getCompilerSourceVM();
    }

    @Override
    public String getCompilerTargetVM() {
        return delegate.getCompilerTargetVM();
    }

    @Override
    public boolean getDevelopment() {
        return delegate.getDevelopment();
    }

    @Override
    public boolean getDisplaySourceFragment() {
        return delegate.getDisplaySourceFragment();
    }

    @Override
    public boolean getErrorOnUseBeanInvalidClassAttribute() {
        return delegate.getErrorOnUseBeanInvalidClassAttribute();
    }

    @Override
    public boolean getFork() {
        return delegate.getFork();
    }

    @Override
    public String getIeClassId() {
        return delegate.getIeClassId();
    }

    @Override
    public String getJavaEncoding() {
        return delegate.getJavaEncoding();
    }

    @Override
    public JspConfig getJspConfig() {
        return delegate.getJspConfig();
    }

    @Override
    public boolean getKeepGenerated() {
        return delegate.getKeepGenerated();
    }

    @Override
    public boolean getMappedFile() {
        return delegate.getMappedFile();
    }

    @Override
    public int getModificationTestInterval() {
        return delegate.getModificationTestInterval();
    }

    @Override
    public File getScratchDir() {
        return delegate.getScratchDir();
    }

    @Override
    public TagPluginManager getTagPluginManager() {
        return delegate.getTagPluginManager();
    }

    @Override
    public TldLocationsCache getTldLocationsCache() {
        return tldLocationsCache;
    }

    @Override
    public boolean getTrimSpaces() {
        return delegate.getTrimSpaces();
    }

    @Override
    public boolean isCaching() {
        return delegate.isCaching();
    }

    @Override
    public boolean isPoolingEnabled() {
        return delegate.isPoolingEnabled();
    }

    @Override
    public boolean isSmapDumped() {
        return delegate.isSmapDumped();
    }

    @Override
    public boolean isSmapSuppressed() {
        return delegate.isSmapSuppressed();
    }

    @Override
    public boolean isXpoweredBy() {
        return delegate.isXpoweredBy();
    }

    @Override
    public boolean getRecompileOnFail() {
        return delegate.getRecompileOnFail();
    }

    @Override
    public int getMaxLoadedJsps() {
        return delegate.getMaxLoadedJsps();
    }

    @Override
    public int getJspIdleTimeout() {
        return delegate.getJspIdleTimeout();
    }
}
