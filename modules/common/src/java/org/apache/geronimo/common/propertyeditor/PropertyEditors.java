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

package org.apache.geronimo.common.propertyeditor;

import java.util.List;
import java.util.ArrayList;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import org.apache.geronimo.common.Classes;
import org.apache.geronimo.common.NullArgumentException;

/**
 * A collection of PropertyEditor utilities.
 *
 * <p>Allows editors to be nested sub-classes named PropertyEditor.
 *
 * @version $Revision: 1.8 $ $Date: 2004/03/10 09:58:26 $
 */
public class PropertyEditors
{
    /**
     * Augment the PropertyEditorManager search path to incorporate the
     * Geronimo specific editors.
     */
    static
    {
        // Append our package to the serach path
        appendEditorSearchPath("org.apache.geronimo.common.propertyeditor");
    }

    /**
     * Locate a value editor for a given target type.
     *
     * @param type   The class of the object to be edited.
     * @return       An editor for the given type or null if none was found.
     */
    public static PropertyEditor findEditor(final Class type)
    {
        if (type == null) {
            throw new NullArgumentException("type");
        }

        PropertyEditor editor = PropertyEditorManager.findEditor(type);

        // Try to use adapter for array types
        if (editor == null && type.isArray()) {
            Class ctype = type.getComponentType();
            editor = findEditor(ctype);
            if (editor != null) {
                editor = new ArrayPropertyEditorAdapter(ctype, editor);
            }
        }

        return editor;
    }

    /**
     * Locate a value editor for a given target type.
     *
     * @param typeName    The class name of the object to be edited.
     * @return            An editor for the given type or null if none was found.
     */
    public static PropertyEditor findEditor(final String typeName)
        throws ClassNotFoundException
    {
        if (typeName == null) {
            throw new NullArgumentException("typeName");
        }

        Class type = null;
        try {
            type = Classes.loadClass(typeName);
        }
        catch (ClassNotFoundException e) {
            // look for a nested class
            type = Classes.loadClass(typeName + "$PropertyEditor");
        }

        return findEditor(type);
    }

    /**
     * Get a value editor for a given target type.
     *
     * @param type    The class of the object to be edited.
     * @return        An editor for the given type.
     *
     * @throws PropertyEditorException   No editor was found.
     */
    public static PropertyEditor getEditor(final Class type)
    {
        PropertyEditor editor = findEditor(type);
        if (editor == null) {
            throw new PropertyEditorException("No property editor for type: " + type);
        }

        return editor;
    }

    /**
     * Get a value editor for a given target type.
     *
     * @param typeName    The class name of the object to be edited.
     * @return            An editor for the given type.
     *
     * @throws PropertyEditorException   No editor was found.
     */
    public static PropertyEditor getEditor(final String typeName)
        throws ClassNotFoundException
    {
        PropertyEditor editor = findEditor(typeName);
        if (editor == null) {
            throw new PropertyEditorException("No property editor for type: " + typeName);
        }

        return editor;
    }

    /**
     * Register an editor class to be used to editor values of a given target class.
     *
     * @param type         The class of the objetcs to be edited.
     * @param editorType   The class of the editor.
     */
    public static void registerEditor(final Class type, final Class editorType)
    {
        if (type == null) {
            throw new NullArgumentException("type");
        }
        if (editorType == null) {
            throw new NullArgumentException("editorType");
        }

        PropertyEditorManager.registerEditor(type, editorType);
    }

    /**
     * Register an editor class to be used to editor values of a given target class.
     *
     * @param typeName         The classname of the objetcs to be edited.
     * @param editorTypeName   The class of the editor.
     */
    public static void registerEditor(final String typeName,
                                      final String editorTypeName)
        throws ClassNotFoundException
    {
        if (typeName == null) {
            throw new NullArgumentException("typeName");
        }
        if (editorTypeName == null) {
            throw new NullArgumentException("editorTypeName");
        }

        Class type = Classes.loadClass(typeName);
        Class editorType = Classes.loadClass(editorTypeName);

        registerEditor(type, editorType);
    }

    /**
     * Gets the package names that will be searched for property editors.
     *
     * @return   The package names that will be searched for property editors.
     */
    public static List getEditorSearchPath()
    {
        String[] path = PropertyEditorManager.getEditorSearchPath();

        List list = new ArrayList(path.length);
        for (int i=0; i<path.length; i++) {
            list.add(path[i]);
        }

        return list;
    }

    /**
     * Sets the package names that will be searched for property editors.
     *
     * @param path   The serach path.
     */
    public static void setEditorSearchPath(final List path)
    {
        if (path == null) {
            throw new NullArgumentException("path");
        }

        String[] elements = (String[])path.toArray(new String[path.size()]);
        PropertyEditorManager.setEditorSearchPath(elements);
    }

    /**
     * Append package names to the property editor search path.
     *
     * @param names   The package names to append.
     */
    public static void appendEditorSearchPath(final List names)
    {
        if (names == null) {
            throw new NullArgumentException("names");
        }
        if (names.size() == 0) return;

        List path = getEditorSearchPath();
        path.addAll(names);

        setEditorSearchPath(path);
    }

    /**
     * Append package names to the property editor search path.
     *
     * @param names   The package names to append.
     */
    public static void appendEditorSearchPath(final String[] names)
    {
        if (names == null) {
            throw new NullArgumentException("names");
        }
        if (names.length == 0) return;

        List list = new ArrayList(names.length);
        for (int i=0; i<names.length; i++) {
            list.add(names[i]);
        }

        appendEditorSearchPath(list);
    }

    /**
     * Append a package name to the property editor search path.
     *
     * @param name   The package name to append.
     */
    public static void appendEditorSearchPath(final String name)
    {
        if (name == null) {
            throw new NullArgumentException("name");
        }

        appendEditorSearchPath(new String[] { name });
    }
}
