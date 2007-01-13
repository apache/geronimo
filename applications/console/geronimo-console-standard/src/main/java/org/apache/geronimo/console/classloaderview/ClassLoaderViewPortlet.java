/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.classloaderview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.StringTree;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.kernel.util.ClassLoaderRegistry;

public class ClassLoaderViewPortlet extends BasePortlet {

    private static final String NORMALVIEW_JSP = "/WEB-INF/view/classloaderview/view.jsp";

    private static final String MAXIMIZEDVIEW_JSP = "/WEB-INF/view/classloaderview/view.jsp";

    private static final String HELPVIEW_JSP = "/WEB-INF/view/classloaderview/help.jsp";

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        renderRequest.getPortletSession().setAttribute("classloaderTree", this);

        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                NORMALVIEW_JSP);
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                MAXIMIZEDVIEW_JSP);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                HELPVIEW_JSP);

    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        super.destroy();
    }

    public String getJSONTrees() {
        List list = getTrees();
        if (list == null)
            return "[]";

        StringBuffer stb = new StringBuffer();
        stb.append("[");
        for (int i = 0; i < list.size(); i++) {
            StringTree node = (StringTree) list.get(i);
            if (i != 0)
                stb.append(",");
            stb.append(node.toJSONObject("" + i));
        }
        stb.append("]");
        list = null;
        return stb.toString();
    }

    public ArrayList getTrees() {
        ArrayList parentNodes = new ArrayList();
        List list = ClassLoaderRegistry.getList();
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            updateTree((ClassLoader) iter.next(), parentNodes);
        }
        return parentNodes;
    }

    public StringTree updateTree(ClassLoader classloader, ArrayList parentNodes) {

        Iterator iter = parentNodes.iterator();
        StringTree node = null;
        while (iter.hasNext()) {
            StringTree currNode = (StringTree) iter.next();
            node = currNode.findNode(classloader.toString());
            if (node != null)
                return node;
        }

        if (node == null) {
            node = new StringTree(classloader.toString());
            node = addClasses(node, classloader);

            if (classloader instanceof org.apache.geronimo.kernel.config.MultiParentClassLoader) {
                org.apache.geronimo.kernel.config.MultiParentClassLoader mpclassloader = (org.apache.geronimo.kernel.config.MultiParentClassLoader) classloader;
                ClassLoader[] parents = mpclassloader.getParents();
                if (parents == null)
                    parentNodes.add(node);
                else if (parents.length == 0)
                    parentNodes.add(node);
                else {
                    for (int i = 0; i < parents.length; i++) {
                        StringTree parentNode = updateTree(parents[i],
                                parentNodes);
                        parentNode.addChild(node);
                    }
                }
            } else if (classloader.getParent() != null) {
                StringTree parentNode = updateTree(classloader.getParent(),
                        parentNodes);
                parentNode.addChild(node);
            } else
                parentNodes.add(node);
        }
        return node;
    }

    private StringTree addClasses(StringTree node, ClassLoader loader) {
        try {
            java.lang.reflect.Field CLASSES_VECTOR_FIELD = ClassLoader.class
                    .getDeclaredField("classes");

            if (CLASSES_VECTOR_FIELD.getType() != java.util.Vector.class) {
                return node;
            }
            CLASSES_VECTOR_FIELD.setAccessible(true);

            final java.util.Vector classes = (java.util.Vector) CLASSES_VECTOR_FIELD
                    .get(loader);
            if (classes == null)
                return node;

            final Class[] result;

            synchronized (classes) {
                result = new Class[classes.size()];
                classes.toArray(result);
            }

            CLASSES_VECTOR_FIELD.setAccessible(false);

            StringTree classNames = new StringTree("Classes");
            StringTree interfaceNames = new StringTree("Interfaces");
            node.addChild(classNames);
            node.addChild(interfaceNames);

            for (int i = 0; i < result.length; i++) {
                if (result[i].isInterface())
                    interfaceNames.addChild(result[i].toString());
                else
                    classNames.addChild(result[i].toString());
            }

            return node;
        } catch (Exception e) {
            return node;
        }
    }

}
