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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Adapter for editing array types.
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:26 $
 */
public final class ArrayPropertyEditorAdapter extends PropertyEditorSupport {
    private Class type;
    private PropertyEditor editor;

    public ArrayPropertyEditorAdapter(final Class type, final PropertyEditor editor) {
        if (type == null) {
            throw new IllegalArgumentException("Type is null");
        }
        if (editor == null) {
            throw new IllegalArgumentException("Editor is null");
        }

        this.type = type;
        this.editor = editor;
    }

    public void setAsText(String text) {
        if (text == null || text.length() == 0) {
            setValue(null);
        } else {
            StringTokenizer stok = new StringTokenizer(text, ",");
            final List list = new LinkedList();

            while (stok.hasMoreTokens()) {
                editor.setAsText(stok.nextToken());
                list.add(editor.getValue());
            }

            Object array = Array.newInstance(type, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }

            setValue(array);
        }
    }

    public String getAsText() {
        Object[] objects = (Object[]) getValue();
        if (objects == null || objects.length == 0) {
            return null;
        }

        StringBuffer result = new StringBuffer(String.valueOf(objects[0]));
        for (int i = 1; i < objects.length; i++) {
            result.append(",").append(objects[i]);
        }

        return result.toString();

    }
}
