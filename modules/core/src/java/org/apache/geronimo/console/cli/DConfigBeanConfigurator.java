/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.console.cli;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;

/**
 * Knows how to configure a DConfigBean at the command line.  The editing process
 * is a series of reads and writes to the provided input and output streams,
 * which basically presents information and a prompt to the user, gathers their
 * input, and repeats.  They can navigate through a tree of DConfigBeans and
 * Java Beans, adding, removing, and editing properies on beans where
 * appropriate.
 * <p>
 * Note: it might make sense to break this class up eventually.  Particularly if
 * we want to allow the user to navigate between arbitrary DDBeans (standard DD)
 * and their matching DConfigBeans (server-specific DD).  Right now they can only
 * edit one tree at a time, either the whole DDBean tree, or the whole
 * DConfigBean tree.
 * </p>
 * @version $Revision: 1.1 $ $Date: 2003/09/04 05:26:19 $
 */
public class DConfigBeanConfigurator {
    private final static Log log = LogFactory.getLog(DConfigBeanConfigurator.class);
    private PrintWriter out;
    private BufferedReader in;
    private Stack beans = new Stack();

    /**
     * Creates a new instance, based on the supplied config bean root and
     * input and output streams.
     */
    public DConfigBeanConfigurator(DConfigBeanRoot bean, PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
        beans.push(bean);
    }

    /**
     * Begins the process of configuring the DConfigBean tree.  When this method
     * returns, the user has finished editing the DConfigBeans (or a fatal error
     * caused the editing to abort).
     *
     * @return <code>true</code> if the editing completed normally
     *        (<code>false</code> if there was a fatal error).
     */
    public boolean configure() {
        try {
            initialize();
            return true;
        } catch(IntrospectionException e) {
            log.error("Unable to introspect a JavaBean", e);
        } catch(IOException e) {
            log.error("Unable to gather input from user", e);
        } catch(InvocationTargetException e) {
            log.error("Unable to read or write a JavaBean property", e.getTargetException());
        } catch(IllegalAccessException e) {
            log.error("Unable to read or write a JavaBean property", e);
        } catch(ConfigurationException e) {
            log.error("Unable to generate a child DConfigBean", e);
        } catch(InstantiationException e) {
            log.error("Unable to generate a child bean", e);
        }
        return false;
    }

    /**
     * The main logic loop for the editing process.  This starts an endless loop,
     * where each iteration prints information on the current bean and prompts
     * the user for an action.  A stack is maintained of the current beans from
     * root (bottom of the stack) to the current leaf node (top of the stack).
     * The main options offered here are to move to a parent or child bean (if
     * available) or to edit a property on the current bean.
     */
    private void initialize() throws IntrospectionException, IOException, InvocationTargetException, IllegalAccessException, ConfigurationException, InstantiationException {
        boolean forward = true;
        BeanInfo info;
        PropertyDescriptor[] properties = new PropertyDescriptor[0];
        PropertyDescriptor[] readOnly = new PropertyDescriptor[0];
        PropertyDescriptor[] childProps = new PropertyDescriptor[0];
        Map childTypes = new HashMap();
        while(true) {
            out.println("\n\n");
            Object bean = beans.peek();
            int count;

            // Load top-level info
            info = Introspector.getBeanInfo(bean.getClass());
            String indent = printLocation();

            // Load children
            childTypes.clear();
            if(bean instanceof DConfigBean) {
                DConfigBean dcb = (DConfigBean) bean;
                String[] xpaths = dcb.getXpaths();
                for(int i=0; i<xpaths.length; i++) {
                    DDBean[] ddbs = dcb.getDDBean().getChildBean(xpaths[i]);
                    if(ddbs.length != 0) {
                        DConfigBean[] list = new DConfigBean[ddbs.length];
                        for(int j = 0; j < ddbs.length; j++) {
                            list[j] = dcb.getDConfigBean(ddbs[j]);
                        }
                        childTypes.put(Introspector.getBeanInfo(list[0].getClass()).getBeanDescriptor().getDisplayName(), list);
                    }
                }
                for(Iterator iterator = childTypes.keySet().iterator(); iterator.hasNext();) {
                    String s = (String)iterator.next();
                    int number = ((DConfigBean[])childTypes.get(s)).length;
                    out.println(indent+"+ "+s+" ("+number+" entr"+(number == 1 ? "y" : "ies")+")");
                }
            }
            childProps = getChildProperties(info.getPropertyDescriptors());
            for(int i = 0; i < childProps.length; i++) {
                PropertyDescriptor prop = childProps[i];
                if(prop instanceof IndexedPropertyDescriptor) {
                    int number = ((Object[])prop.getReadMethod().invoke(bean, new Object[0])).length;
                    out.println(indent+"+ "+prop.getDisplayName()+" ("+number+" entr"+(number == 1 ? "y" : "ies")+")");
                } else {
                    out.println(indent+"+ "+prop.getDisplayName()+" (child property)");
                }
            }
            out.println();

            // Load properties todo: handle properties of type bean but not DConfigBean and indexed properties
            count = 0;
            properties = getNormalProperties(info.getPropertyDescriptors());
            readOnly = getReadOnly(info.getPropertyDescriptors());
            for(int i = 0; i < readOnly.length; i++) {
                PropertyDescriptor property = readOnly[i];
                out.println(property.getDisplayName()+": "+property.getReadMethod().invoke(bean, new Object[0]));
            }
            if(properties.length > 0) {
                out.println("Properties for "+getFullName(bean)+":");
            }
            for(int i = 0; i < properties.length; i++) {
                PropertyDescriptor property = properties[i];
                out.println("  "+(++count)+": "+property.getDisplayName()+" ("+property.getReadMethod().invoke(bean, new Object[0])+")");
            }
            out.flush();

            // Auto-navigate
            if(properties.length == 0 && childTypes.size() == 1 && childProps.length == 0) {
                DConfigBean[] children = (DConfigBean[])childTypes.values().iterator().next();
                if(children.length == 1) {
                    if(forward) {
                        out.println("Nothing interesting to do here.  Moving on.");
                        beans.push(children[0]);
                        continue;
                    } else if(beans.size() > 1) {
                        out.println("Nothing interesting to do here.  Moving on.");
                        beans.pop();
                        continue;
                    }
                }
            } else if(properties.length == 0 && childTypes.size() == 0 && childProps.length == 1) {
                if(!(childProps[0] instanceof IndexedPropertyDescriptor)) {
                    if(forward) {
                        out.println("Nothing interesting to do here.  Moving on.");
                        beans.push(childProps[0].getReadMethod().invoke(bean, new Object[0]));
                        continue;
                    } else if(beans.size() > 1) {
                        out.println("Nothing interesting to do here.  Moving on.");
                        beans.pop();
                        continue;
                    }
                }
            }
            if(properties.length > 0) {
                out.println();
            }

            // Show navigation options
            out.print("Action (");
            boolean first = true;
            if(properties.length > 0) {
                if(!first) {out.print(" / ");}
                out.print("Edit [P]roperty");
                first = false;
            }
            if(childTypes.size() > 0 || childProps.length > 0) {
                if(!first) {out.print(" / ");}
                out.print("Move [D]own");
                first = false;
            }
            if(beans.size() > 1) {
                if(!first) {out.print(" / ");}
                out.print("Move [U]p");
                first = false;
            }
            if(!first) {out.print(" / ");}
            out.print("[Q]uit");
            first = false;
            out.print("): ");
            out.flush();
            String choice = in.readLine().trim().toLowerCase();
            if(choice.equals("u")) {
                forward = false;
                beans.pop();
                continue;
            } else if(choice.equals("d")) {
                forward = true;
                if(childTypes.size() == 0 && childProps.length == 0) {
                    log.warn("No children available here.");
                    continue;
                } else {
                    selectChildBean(childTypes, bean, childProps);
                    continue;
                }
            } else if(choice.equals("q")) {
                return;
            } else if(choice.equals("p")) {
                if(properties.length == 0) {
                    log.warn("No editable properties available here.");
                    continue;
                } else {
                    editProperty(bean, properties);
                    continue;
                }
            } else if(isNumber(choice)) {
                int value = Integer.parseInt(choice);
                if(value > 0 && value <= properties.length) {
                    editProperty(bean, properties[value-1]);
                    continue;
                }
            }
            log.error("I don't know how to do that (yet)");
        }
    }

    /**
     * The user wants to edit a property.  This method figures out which one (of
     * the properties available for the bean).
     */
    private void editProperty(Object bean, PropertyDescriptor[] properties) throws IOException, InvocationTargetException, IllegalAccessException {
        if(properties.length == 1) {
            editProperty(bean, properties[0]);
            return;
        }
        String choice = null;
        while(true) {
            out.print("Edit which property (1-"+properties.length+")? ");
            out.flush();
            choice = in.readLine();
            try {
                int value = Integer.parseInt(choice);
                if(value > 0 && value <= properties.length) {
                    editProperty(bean, properties[value-1]);
                    return;
                }
            } catch(NumberFormatException e) {}
        }
    }

    /**
     * Manages the editing of a single property.
     */
    private void editProperty(final Object bean, final PropertyDescriptor property) throws InvocationTargetException, IllegalAccessException, IOException {
        final PropertyEditor pe = PropertyEditors.findEditor(property.getPropertyType());
        pe.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    property.getWriteMethod().invoke(bean, new Object[]{pe.getValue()});
                    pe.removePropertyChangeListener(this);
                } catch(IllegalAccessException e) {
                    log.error("Not allowed to set property", e);
                } catch(IllegalArgumentException e) {
                    log.error("Invalid value for property", e);
                } catch(InvocationTargetException e) {
                    log.error("Exception occured while setting property", e.getTargetException());
                }
            }
        });
        out.println("\nEditing Property "+property.getDisplayName());
        Object value = property.getReadMethod().invoke(bean, new Object[0]);
        if(value == null) {
            value = pe.getJavaInitializationString();
        }
        out.println("  Old value is: '"+value+"'");
        out.println("  Specify a new value.  Enter nothing to keep the current value.\n" +
                    "  Type (empty) for an empty string or (null) for a null.");
        out.print("New Value: ");
        out.flush();
        String choice = in.readLine();
        if(choice.equals("")) {
            return;
        } else if(choice.equals("(null)")) {
            choice = null;
        } else if(choice.equals("(empty)")) {
            choice = "";
        }
        pe.setAsText(choice);
    }

    /**
     * The user wants to move to a child bean.  This method figures out which
     * one.  It may be a child DConfigBean or a child property where we don't
     * have a property editor for that property type so we treat the whole
     * thing as a child bean.
     */
    private void selectChildBean(Map types, Object bean, PropertyDescriptor[] props) throws IOException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DConfigBean[] cbs = null;
        PropertyDescriptor prop = null;
        int count;
        String choice;
        if(types.size()+props.length > 1) {
            count = 0;
            out.println("\nAvailable Children:");
            for(Iterator iterator = types.keySet().iterator(); iterator.hasNext();) {
                String name = (String) iterator.next();
                out.println("  ["+(++count)+"] "+name);
            }
            for(int i = 0; i < props.length; i++) {
                out.println("  ["+(++count)+"] "+props[i].getDisplayName());
            }
            while(true) {
                out.print("Select child type (1-"+(types.size()+props.length)+"): ");
                out.flush();
                choice = in.readLine();
                try {
                    int value = Integer.parseInt(choice);
                    if(value > 0 && value <= types.size()) {
                        count = 0;
                        String key = null;
                        for(Iterator iterator = types.keySet().iterator(); iterator.hasNext() && count++ < value;) {
                            key = (String) iterator.next();
                        }
                        cbs = (DConfigBean[]) types.get(key);
                        if(cbs != null) {
                            break;
                        }
                    } else if(value > types.size() && value <= (types.size()+props.length)) {
                        prop = props[value-types.size()-1];
                        break;
                    }
                } catch(NumberFormatException e) {}
            }
        } else {
            if(types.size() == 1) {
                cbs = (DConfigBean[])types.values().iterator().next();
            } else if(props.length == 1) {
                prop = props[0];
            } else {
                log.error("You've confused me.  Please try again.");
            }
        }
        if(cbs != null) {
            selectChildDConfigBean(cbs);
        } else if(prop != null) {
            selectChildProperty(bean, prop);
        }
    }

    /**
     * It turns out the user wants navigate to a child property (where we don't
     * have an editor for the property type, so we treat it as a child bean).
     * If the is a plain property, this method will just go there.  If it's an
     * indexed property, this method presents CRUD options.
     */
    private void selectChildProperty(Object bean, PropertyDescriptor prop) throws InvocationTargetException, IllegalAccessException, IOException, InstantiationException {
        //todo: consider handling indexed properties that are themselves arrays?
        if(!(prop instanceof IndexedPropertyDescriptor)) {
            beans.push(prop.getReadMethod().invoke(bean, new Object[0]));
            return;
        }
        String choice;
        Object[] values;
        while(true) {
            out.println("\nEditing list of "+prop.getDisplayName());
            values = (Object[]) prop.getReadMethod().invoke(bean, new Object[0]);
            if(values.length == 0) {
                out.println("  (list is currently empty)");
            }
            for(int i = 0; i < values.length; i++) {
                out.println("  "+(i+1)+": "+values[i]);
            }
            out.print("Action ([C]reate entry");
            if(values.length > 0) {
                out.print(" / [D]elete entry / edit entry [1"+(values.length > 1 ? "-"+values.length : "")+"]");
            }
            out.print(" / [B]ack): ");
            out.flush();
            choice = in.readLine().trim().toLowerCase();
            if(choice.equals("c")) {
                Object[] newv = (Object[])java.lang.reflect.Array.newInstance(values.getClass().getComponentType(), values.length+1);
                System.arraycopy(values, 0, newv, 0, values.length);
                newv[values.length] = values.getClass().getComponentType().newInstance();
                prop.getWriteMethod().invoke(bean, new Object[]{newv});
                continue;
            } else if(choice.equals("b")) {
                return;
            } else if(isNumber(choice)) {
                int number = Integer.parseInt(choice);
                if(number > 0 && number <= values.length) {
                    beans.push(values[number-1]);
                    return;
                }
            } else {
                log.warn("I didn't understand that");
            }
        }
    }

    /**
     * Checks whether a value entered by the user is composed entirely of digits.
     */
    private boolean isNumber(String choice) {
        for(int i=0; i<choice.length(); i++) {
            if(!Character.isDigit(choice.charAt(i))) {
                return false;
            }
        }
        return choice.length() > 0;
    }

    /**
     * It turns out the user wants to edit a child DConfigBean.  So far, we just
     * know what type of child bean they want (e.g. "one of the resource
     * references").  This method figures out which specific instance of that
     * they want to edit (identify a specific resource reference).
     */
    private void selectChildDConfigBean(DConfigBean[] cbs) throws IOException {
        String choice;
        if(cbs.length == 1) {
            beans.push(cbs[0]);
            return;
        }
        out.println("\nAvailable Children:");
        for(int i = 0; i < cbs.length; i++) {
            out.println("  ["+(i+1)+"] "+cbs[i]);
        }
        while(true) {
            out.print("Select child (1-"+cbs.length+"): ");
            out.flush();
            choice = in.readLine();
            try {
                int value = Integer.parseInt(choice);
                if(value > 0 && value <= cbs.length) {
                    beans.push(cbs[value-1]);
                    break;
                }
            } catch(NumberFormatException e) {}
        }
    }

    /**
     * Displays the user's current position in the stack of beans.  This
     * method shows everything down to the current position.  The caller
     * must add on the children of the current node.
     *
     * @return The String full of spaces representating the indentation
     *         for any children of the last bean displayed.
     */
    private String printLocation() throws IntrospectionException {
        out.println("          ---------- Editing Server-Specific DD ----------          ");
        String here = "";
        int count = 0;
        for(Iterator iterator = beans.iterator(); iterator.hasNext();) {
            ++count;
            Object temp = iterator.next();
            if(!here.equals("")) {
                out.print(here);
                out.print("+ ");
            }
            if(count == beans.size()) {out.print("[[[ ");}
            out.print(getFullName(temp));
            if(count == beans.size()) {out.print(" ]]]");}
            here = here + "  ";
            out.println();
        }
        return here;
    }

    /**
     * Gets the name of a class of beans (e.g. "Resource Reference")
     * followed by the description of the specific instances (e.g.
     * jdbc/SomeDatabase).
     */
    private String getFullName(Object bean) throws IntrospectionException {
        String name = bean.toString();
        if(name.length() > 40 || name.indexOf("@") > 0) {//todo: check whether toString has been overridden
            name = "";
        } else {
            name = " ("+name+")";
        }
        return Introspector.getBeanInfo(bean.getClass()).getBeanDescriptor().getDisplayName()+name;
    }

    /**
     * Gets the sub-list of the supplied properties that are readable, writable,
     * have a property editor, and are not on the list to specifically exclude.
     */
    private PropertyDescriptor[] getNormalProperties(PropertyDescriptor[] descriptors) {
        List list = new ArrayList(descriptors.length);
        for(int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            if(isInvisible(descriptor) || descriptor.getReadMethod() == null || descriptor.getWriteMethod() == null || PropertyEditors.findEditor(descriptor.getPropertyType()) == null) {
                continue;
            }
            list.add(descriptors[i]);
        }
        return (PropertyDescriptor[]) list.toArray(new PropertyDescriptor[list.size()]);
    }

    /**
     * Gets the sub-list of the supplied properties that are readable, not
     * writable, and are not on the list to specifically exclude.
     */
    private PropertyDescriptor[] getReadOnly(PropertyDescriptor[] descriptors) {
        List list = new ArrayList(descriptors.length);
        for(int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            if(isInvisible(descriptor) || descriptor.getWriteMethod() != null || descriptor.getReadMethod() == null) {
                continue;
            }
            list.add(descriptors[i]);
        }
        return (PropertyDescriptor[]) list.toArray(new PropertyDescriptor[list.size()]);
    }

    /**
     * Gets the sub-list of the supplied properties that are readable, writeable,
     * and have no property editor.  These will be treated as child properties,
     * so their properties will in turn be presented for editing.
     */
    private PropertyDescriptor[] getChildProperties(PropertyDescriptor[] descriptors) {
        List list = new ArrayList(descriptors.length);
        for(int i = 0; i < descriptors.length; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            if(isInvisible(descriptor) || descriptor.getWriteMethod() == null || descriptor.getReadMethod() == null || PropertyEditors.findEditor(descriptor.getPropertyType()) != null) {
                continue;
            }
            list.add(descriptors[i]);
        }
        return (PropertyDescriptor[]) list.toArray(new PropertyDescriptor[list.size()]);
    }

    /**
     * Checks whether a property is one of the ones we want to specifically
     * ignore/suppress.
     */
    private boolean isInvisible(PropertyDescriptor descriptor) {
        return descriptor.getName().equals("class") || descriptor.getName().equals("DDBean") || descriptor.getName().equals("xpaths");
    }
}
