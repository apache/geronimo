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
package org.apache.geronimo.console.jndiview;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.gbean.AbstractName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.StringTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

public class JNDIViewPortlet extends BasePortlet {

    private static final Logger log = LoggerFactory.getLogger(JNDIViewPortlet.class);
        
    private static final String NORMALVIEW_JSP = "/WEB-INF/view/jndiview/view.jsp";

    private static final String MAXIMIZEDVIEW_JSP = "/WEB-INF/view/jndiview/view.jsp";

    private static final String HELPVIEW_JSP = "/WEB-INF/view/jndiview/help.jsp";

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

        try {
            renderRequest.getPortletSession().setAttribute("jndiTree",
                    getJSONTrees());
        } catch (Exception ex) {
            throw new PortletException(ex);
        }
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

    public String getJSONTrees() throws Exception {
        List list = getContextTree();
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
        return stb.toString();
    }

    private void buildEJBModule(Kernel kernel, List arryList, Hashtable entApp)
            throws Exception {
        Map query = new HashMap();
        query.put("j2eeType", "EJBModule");
        Set setEnt = kernel.listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(null, query));
        Iterator iterator = setEnt.iterator();

        while (iterator.hasNext()) {
            AbstractName gb = (AbstractName) iterator.next();
            StringTree ejbModule = new StringTree(gb.getNameProperty("name"));

            if (gb.getNameProperty("J2EEApplication") == null
                    || gb.getNameProperty("J2EEApplication").equals("null")) {
                StringTree treeEnt = (StringTree) entApp.get("EJBModule");
                treeEnt.addChild(ejbModule);
            } else {
                StringTree treeEnt = (StringTree) entApp.get(gb
                        .getNameProperty("J2EEApplication"));
                treeEnt = treeEnt.findNode("EJBModule");
                treeEnt.addChild(ejbModule);
            }
            Map queryEnt = new HashMap();
            StringTree entityBean = new StringTree("EntityBeans");
            ejbModule.addChild(entityBean);
            queryEnt.put("j2eeType", "EntityBean");
            queryEnt.put("EJBModule", gb.getNameProperty("name"));
            queryEnt.put("J2EEApplication", gb
                    .getNameProperty("J2EEApplication"));
            Set setEntBean = kernel
                    .listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(
                            null, queryEnt));

            Iterator iterEntBean = setEntBean.iterator();

            while (iterEntBean.hasNext()) {
                AbstractName gbEntBean = (AbstractName) iterEntBean.next();
                StringTree beanNode = new StringTree(gbEntBean
                        .getNameProperty("name"));
                entityBean.addChild(beanNode);
                Context jndi = (Context) kernel.getAttribute(gbEntBean,
                        "componentContext");
                buildContext(beanNode, jndi, "java:comp");
            }

            queryEnt = new HashMap();
            StringTree sessionBean = new StringTree("SessionBeans");
            ejbModule.addChild(sessionBean);
            queryEnt.put("j2eeType", "StatelessSessionBean");
            queryEnt.put("EJBModule", gb.getNameProperty("name"));
            queryEnt.put("J2EEApplication", gb
                    .getNameProperty("J2EEApplication"));
            Set setSessionBean = kernel
                    .listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(
                            null, queryEnt));

            Iterator iterSessionBean = setSessionBean.iterator();

            while (iterSessionBean.hasNext()) {
                AbstractName gbSessionBean = (AbstractName) iterSessionBean
                        .next();
                StringTree beanNode = new StringTree(gbSessionBean
                        .getNameProperty("name"));
                sessionBean.addChild(beanNode);
                Context jndi = (Context) kernel.getAttribute(gbSessionBean,
                        "componentContext");
                buildContext(beanNode, jndi, "java:comp");
            }

            queryEnt = new HashMap();
            queryEnt.put("j2eeType", "StatefullSessionBean");
            queryEnt.put("EJBModule", gb.getNameProperty("name"));
            queryEnt.put("J2EEApplication", gb
                    .getNameProperty("J2EEApplication"));
            setSessionBean = kernel
                    .listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(
                            null, queryEnt));

            iterSessionBean = setSessionBean.iterator();

            while (iterSessionBean.hasNext()) {
                AbstractName gbSessionBean = (AbstractName) iterSessionBean
                        .next();
                StringTree beanNode = new StringTree(gbSessionBean
                        .getNameProperty("name"));
                sessionBean.addChild(beanNode);
                Context jndi = (Context) kernel.getAttribute(gbSessionBean,
                        "componentContext");
                buildContext(beanNode, jndi, "java:comp");
            }
        }
    }

    private void buildWebModule(Kernel kernel, List arryList, Hashtable entApp)
            throws Exception {
        Map query = new HashMap();
        query.put("j2eeType", "WebModule");
        Set setEnt = kernel
                .listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(
                        null, query));
        Iterator iterator = setEnt.iterator();

        while (iterator.hasNext()) {

            AbstractName gb = (AbstractName) iterator.next();
            StringTree webModule = new StringTree(gb.getNameProperty("name"));

            if (gb.getNameProperty("J2EEApplication") == null
                    || gb.getNameProperty("J2EEApplication").equals("null")) {
                StringTree treeEnt = (StringTree) entApp.get("WebModule");
                treeEnt.addChild(webModule);
            } else {
                StringTree treeEnt = (StringTree) entApp.get(gb
                        .getNameProperty("J2EEApplication"));
                treeEnt = treeEnt.findNode("WebModule");
                treeEnt.addChild(webModule);
            }

            Map map = (Map) kernel.getAttribute(gb, "componentContext");
            String[] servlets = (String[]) kernel.getAttribute(gb, "servlets");

            StringTree servletsNode = null;
            StringTree jspNode = null;

            for (int i = 0; i < servlets.length; i++) {
                String servlet = servlets[i];
                servlet = servlet.substring(servlet.indexOf("name=") + 5);
                if (servlet.indexOf(",") != -1)
                    servlet = servlet.substring(0, servlet.indexOf(","));
                if (!servlet.equals("jsp") && servlet.startsWith("jsp.")) {
                    if (servletsNode == null) {
                        servletsNode = new StringTree("Servlets");
                        webModule.addChild(servletsNode);
                    }
                    if (jspNode == null) {
                        jspNode = new StringTree("JSP");
                        servletsNode.addChild(jspNode);
                    }
                    jspNode.addChild(new StringTree(servlet.substring(4)));
                } else if (!servlet.equals("jsp")) {
                    if (servletsNode == null) {
                        servletsNode = new StringTree("Servlets");
                        webModule.addChild(servletsNode);
                    }
                    servletsNode.addChild(new StringTree(servlet));
                }
            }
            Iterator contexts = map.keySet().iterator();
            while (contexts.hasNext())
                webModule.addChild(new StringTree("java:comp/" + contexts.next()));
        }
    }

    private void buildResourceModule(Kernel kernel, List arryList,
            Hashtable entApp) throws Exception {
        Map query = new HashMap();
        query.put("j2eeType", "ResourceAdapterModule");
        Set setEnt = kernel
                .listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(
                        null, query));
        Iterator iterator = setEnt.iterator();

        while (iterator.hasNext()) {
            AbstractName gb = (AbstractName) iterator.next();
            String resourceModule = gb.getNameProperty("name");
            if (gb.getNameProperty("J2EEApplication") == null
                    || gb.getNameProperty("J2EEApplication").equals("null")) {
                StringTree treeEnt = (StringTree) entApp
                        .get("ResourceAdapterModule");
                treeEnt.addChild(new StringTree(resourceModule));
            } else {
                StringTree treeEnt = (StringTree) entApp.get(gb
                        .getNameProperty("J2EEApplication"));
                treeEnt = treeEnt.findNode("ResourceAdapterModule");
                treeEnt.addChild(new StringTree(resourceModule));
            }
        }
    }

    private void buildAppClientModule(Kernel kernel, List arryList,
            Hashtable entApp) throws Exception {
        Map query = new HashMap();
        query.put("j2eeType", "AppClientModule");
        Set setEnt = kernel
                .listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(
                        null, query));
        Iterator iterator = setEnt.iterator();

        while (iterator.hasNext()) {
            AbstractName gb = (AbstractName) iterator.next();
            String appClienteModule = gb.getNameProperty("name");
            if (gb.getNameProperty("J2EEApplication") == null
                    || gb.getNameProperty("J2EEApplication").equals("null")) {
                StringTree treeEnt = (StringTree) entApp.get("AppClientModule");
                treeEnt.addChild(new StringTree(appClienteModule));
            } else {
                StringTree treeEnt = (StringTree) entApp.get(gb
                        .getNameProperty("J2EEApplication"));
                treeEnt = treeEnt.findNode("AppClientModule");
                treeEnt.addChild(new StringTree(appClienteModule));
            }
        }
    }

    public void buildContext(StringTree node, Context compCtx, String nodeCurr) {
        Context oldCtx = RootContext.getComponentContext();
        RootContext.setComponentContext(compCtx);
        try {
            InitialContext ctx = new InitialContext();
            buildContextSub(node, (Context)ctx.lookup("java:comp"), nodeCurr);
        } catch (Exception e) {
            log.warn("Error looking up java:comp context", e);
        } finally {        
            RootContext.setComponentContext(oldCtx);
        }
    }
    
    private void buildContextSub(StringTree node, Context ctx, String nodeCurr) {
        try {
            NamingEnumeration enumName = ctx.list("");
            while (enumName.hasMoreElements()) {
                NameClassPair pair = (NameClassPair) enumName.next();
                Object obj = null;
                try {
                    obj = ctx.lookup(pair.getName());
                } catch (NamingException e) {
                    //ignore.... not a context
                }
                if (obj instanceof Context) {
                    buildContextSub(node, (Context) obj, nodeCurr + "/"
                            + pair.getName());
                } else {
                    node.addChild(new StringTree(nodeCurr + "/" + pair.getName()));
                }
            }
        } catch (Exception e) {
            log.warn("Error listing context", e);
        }
    }

    private void buildGlobal(StringTree tree, Context context, String parent)
            throws Exception {
        if (parent == null)
            parent = "";
        if (!parent.equals(""))
            parent = parent + "/";
        javax.naming.NamingEnumeration enum1 = context.list("");
        while (enum1.hasMoreElements()) {
            javax.naming.NameClassPair pair = (javax.naming.NameClassPair) enum1
                    .next();
            Object obj = null;
            try {
                obj = context.lookup(pair.getName());
            } catch (NamingException e) {
                //ignore.... it wasn't a context
            }
            if (obj instanceof Context) {
                buildGlobal(tree, (Context) obj, parent + pair.getName());
            } else {
                tree.addChild(new StringTree(parent + pair.getName()));
            }
        }
    }

    public List getContextTree() throws Exception {
        List arryList = new ArrayList();
        Hashtable entApp = new Hashtable();

        StringTree treeGlobal = new StringTree("Global Context");
        arryList.add(treeGlobal);
        buildGlobal(treeGlobal,
                org.apache.xbean.naming.global.GlobalContextManager
                        .getGlobalContext(), "");

        StringTree tree = new StringTree("Enterprise Applications");
        arryList.add(tree);

        StringTree treeMod = new StringTree("EJBModule");
        entApp.put("EJBModule", treeMod);
        arryList.add(treeMod);

        treeMod = new StringTree("WebModule");
        entApp.put("WebModule", treeMod);
        arryList.add(treeMod);

        treeMod = new StringTree("ResourceAdapterModule");
        entApp.put("ResourceAdapterModule", treeMod);
        arryList.add(treeMod);

        treeMod = new StringTree("AppClientModule");
        entApp.put("AppClientModule", treeMod);
        arryList.add(treeMod);

        org.apache.geronimo.kernel.Kernel kernel = org.apache.geronimo.kernel.KernelRegistry
                .getSingleKernel();

        Set setEnt = kernel
                .listGBeans(new org.apache.geronimo.gbean.AbstractNameQuery(
                        null,
                        Collections.EMPTY_MAP,
                        org.apache.geronimo.j2ee.management.impl.J2EEApplicationImpl.class
                                .getName()));
        Iterator iterator = setEnt.iterator();
        while (iterator.hasNext()) {
            org.apache.geronimo.gbean.AbstractName gb = (org.apache.geronimo.gbean.AbstractName) iterator
                    .next();
            StringTree curr = new StringTree(gb.getNameProperty("name"));
            tree.addChild(curr);
            entApp.put(gb.getNameProperty("name"), curr);

            StringTree temp = new StringTree("EJBModule");
            curr.addChild(temp);

            temp = new StringTree("WebModule");
            curr.addChild(temp);

            temp = new StringTree("ResourceAdapterModule");
            curr.addChild(temp);

            temp = new StringTree("AppClientModule");
            curr.addChild(temp);
        }

        buildEJBModule(kernel, arryList, entApp);
        buildWebModule(kernel, arryList, entApp);
        buildResourceModule(kernel, arryList, entApp);
        buildAppClientModule(kernel, arryList, entApp);
        return arryList;
    }
}