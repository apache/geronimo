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

package org.apache.geronimo.kernel.service;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Adapter for editing array types.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/08 04:38:35 $
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
