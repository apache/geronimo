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

package org.apache.geronimo.common.propertyeditor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.repository.Artifact;
import org.osgi.framework.Bundle;

/**
 * The property editor manager.  This orchestrates Geronimo usage of
 * property editors, allowing additional search paths to be added and
 * specific editors to be registered.
 *
 * @version $Rev$
 */
public class PropertyEditors {
    /**
     * We need to register the standard register seach path and explicitly
     * register a boolean editor to make sure ours overrides.
     */
    static {
        // Append the geronimo propertyeditors package to the global search path.
        appendEditorSearchPath("org.apache.geronimo.common.propertyeditor");
        // and explicitly register the Boolean editor.
        PropertyEditorManager.registerEditor(AbstractName.class, AbstractNameEditor.class);
        PropertyEditorManager.registerEditor(AbstractNameQuery.class, AbstractNameQueryEditor.class);
        PropertyEditorManager.registerEditor(ArrayList.class, ArrayListEditor.class);
        PropertyEditorManager.registerEditor(Artifact.class, ArtifactEditor.class);
        PropertyEditorManager.registerEditor(Boolean.class, BooleanEditor.class);
        PropertyEditorManager.registerEditor(File.class, FileEditor.class);
        PropertyEditorManager.registerEditor(Integer.class, IntegerEditor.class);
        PropertyEditorManager.registerEditor(Properties.class, PropertiesEditor.class);
        PropertyEditorManager.registerEditor(URI.class, URIEditor.class);
        PropertyEditorManager.registerEditor(URL.class, URLEditor.class);
    }

    /**
     * Locate an editor for qiven class of object.
     *
     * @param type The target object class of the property.
     * @return The resolved editor, if any.  Returns null if a suitable editor
     *         could not be located.
     */
    public static PropertyEditor findEditor(Class type) {
        // explicit argument checking is required.
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }


        // try to locate this directly from the editor manager first.
        PropertyEditor editor = PropertyEditorManager.findEditor(type);

        // we're outta here if we got one.
        if (editor != null) {
            return editor;
        }

        // it's possible this was a request for an array class.  We might not
        // recognize the array type directly, but the component type might be
        // resolvable
        if (type.isArray()) {
            // do a recursive lookup on the base type
            editor = findEditor(type.getComponentType());
            // if we found a suitable editor for the base component type,
            // wrapper this in an array adaptor for real use
            if (editor != null) {
                return new ArrayPropertyEditorAdapter(type.getComponentType(), editor);
            }
        }
        // nothing found
        return null;
    }

    /**
     * Locate an editor for qiven class of object, resolved within the context of
     * a specific ClassLoader instance.
     *
     * @param typeName The type name of target property class.
     * @param loader The source ClassLoader instance.
     * @return The resolved editor, if any.  Returns null if a suitable editor
     *         could not be located.
     * @throws ClassNotFoundException Thrown if unable to resolve an appropriate editor class.
     */
    public static PropertyEditor findEditor(String typeName, ClassLoader loader) throws ClassNotFoundException {
        // explicit argument checking is required.
        if (typeName == null) {
            throw new IllegalArgumentException("typeName is null");
        }

        Class type = null;
        // load using the ClassLoading utility, which also manages arrays and primitive classes.
        try {
            type = ClassLoading.loadClass(typeName, loader);
        } catch (ClassNotFoundException e) {
            // We also support anonymous inner class nesting of property editors.  In that situation,
            // the package/class names are the same, but add on the inner class specifier.
            // If this one fails, we jump directly out with the ClassNotFoundException.
            type = ClassLoading.loadClass(typeName + "$PropertyEditor", loader);
        }

        // The PropertyEditorManager class uses the context class loader for all of its resolution
        // steps.  We need force PropertyManagerEditor to use our loader, so we override the
        // current context loader.
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            // now call the base findEditor() method that works directly from the property type.
            return findEditor(type);
        } finally {
            // make sure we restore the context....this will happen even if findEditor()
            // results in an exception.
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }
    public static PropertyEditor findEditor(String typeName, Bundle bundle) throws ClassNotFoundException {
        // explicit argument checking is required.
        if (typeName == null) {
            throw new IllegalArgumentException("typeName is null");
        }

        Class type = null;
        // load using the ClassLoading utility, which also manages arrays and primitive classes.
        try {
            type = ClassLoading.loadClass(typeName, bundle);
        } catch (ClassNotFoundException e) {
            // We also support anonymous inner class nesting of property editors.  In that situation,
            // the package/class names are the same, but add on the inner class specifier.
            // If this one fails, we jump directly out with the ClassNotFoundException.
            type = ClassLoading.loadClass(typeName + "$PropertyEditor", bundle);
        }

        // The PropertyEditorManager class uses the context class loader for all of its resolution
        // steps.  We need force PropertyManagerEditor to use our loader, so we override the
        // current context loader.
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            //TODO OSGI broken
//            Thread.currentThread().setContextClassLoader(bundle);
            // now call the base findEditor() method that works directly from the property type.
            return findEditor(type);
        } finally {
            // make sure we restore the context....this will happen even if findEditor()
            // results in an exception.
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    /**
     * Get a property editor for a given property type.  This is like
     * findEditor, but throws an exception if the property is not found.
     *
     * @param type The target object class of the property.
     * @return The resolved editor, if any.  Throws an exception if this cannot
     *         be resolved.
     * @throws PropertyEditorException Unable to find a suitable editor for this class.
     */
    public static PropertyEditor getEditor(Class type) {
        // just call the non-exceptional lookup
        PropertyEditor editor = findEditor(type);
        // this one throws an exception if not found.
        if (editor == null) {
            throw new PropertyEditorException("No property editor for type: " + type);
        }
        return editor;
    }

    /**
     * Explicity register an editor class for a given target class.
     *
     * @param type The property class.
     * @param editorType The editor class matched up with this type.
     */
    public static void registerEditor(Class type, Class editorType) {
        // explicit argument checking is required.
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }

        // explicit argument checking is required.
        if (editorType == null) {
            throw new IllegalArgumentException("editorType is null");
        }

        PropertyEditorManager.registerEditor(type, editorType);
    }

    /**
     * Explicity register a property/editor class pair by class name.
     *
     * @param typeName The classname of the property.
     * @param editorName The classname of the property editor.
     * @throws ClassNotFoundException Thrown if unable to resolve either the type or the editor from their names.
     */
    public static void registerEditor(String typeName, String editorName) throws ClassNotFoundException {
        // explicit argument checking is required.
        if (typeName == null) {
            throw new IllegalArgumentException("typeName is null");
        }

        // explicit argument checking is required.
        if (editorName == null) {
            throw new IllegalArgumentException("editorTypeName is null");
        }
        // we use the current context loader for this
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // load both of these loaders using our ClassLoading support.
        Class type = ClassLoading.loadClass(typeName, loader);
        Class editor = ClassLoading.loadClass(editorName, loader);

        // we have resolved classes, so register the class information.
        registerEditor(type, editor);
    }

    /**
     * Get a list containing all of the packages in the editor search path.
     *
     * @return a List object containing all of the registered search paths.
     */
    public static List getEditorSearchPath() {
        // grrrr, Arrays.asList() returns a readonly List item, which makes it difficult
        // to append additional items.  This means we have to do this manually.

        // start by getting the list from the editor manager, which is returned as an
        // array of Strings.
        String[] paths = PropertyEditorManager.getEditorSearchPath();

        // get a list matching the initial size...we don't always request this with the intend to append.
        List pathList = new ArrayList(paths.length);

        // now MANUALLY add each of the items in the array.
        for (int i = 0; i < paths.length; i++) {
            pathList.add(paths[i]);
        }

        return pathList;
    }

    /**
     * Sets the search order used for property editor resolution.
     *
     * @param path The serach path.
     */
    public static void setEditorSearchPath(List path) {
        // explicit argument checking is required.
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }

        // we deal in Lists, PropertyEditorManager does arrays, so we need to
        // extract the elements into a array of Strings.
        String[] elements = (String[]) path.toArray(new String[path.size()]);
        PropertyEditorManager.setEditorSearchPath(elements);
    }

    /**
     * Append additional package names to the property editor search path.
     *
     * @param newNames The package names to append.
     */
    public static void appendEditorSearchPath(List newNames) {
        // explicit argument checking is required.
        if (newNames == null) {
            throw new IllegalArgumentException("names is null");
        }

        // if there's nothing to do, then do nothing :-)
        if (newNames.isEmpty()) {
            return;
        }

        // append to the current names list, and set ammended list back as the current
        // search order.
        List currentPath = getEditorSearchPath();
        currentPath.addAll(newNames);

        setEditorSearchPath(currentPath);
    }

    /**
     * Append an array of package names to the editor search path.
     *
     * @param newNames A string array containing the added names.
     */
    public static void appendEditorSearchPath(String[] newNames) {
        // explicit argument checking is required.
        if (newNames == null) {
            throw new IllegalArgumentException("names is null");
        }

        // only bother continuing if the array contains something.
        if (newNames.length != 0) {
            // just convert this to a list and add as normal.
            appendEditorSearchPath(Arrays.asList(newNames));
        }
    }

    /**
     * Append a single package name to the editor search path.
     *
     * @param newName The new path name.
     */
    public static void appendEditorSearchPath(String newName) {
        // explicit argument checking is required.
        if (newName == null) {
            throw new IllegalArgumentException("name is null");
        }

        // append to the current names list, and set ammended list back as the current
        // search order.
        List currentPath = getEditorSearchPath();
        currentPath.add(newName);

        setEditorSearchPath(currentPath);
    }
}
