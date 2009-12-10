/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.tomcat.cluster.wadi.builder;

import javax.xml.namespace.QName;

import org.apache.geronimo.schema.ElementConverter;
import org.apache.geronimo.xbeans.tomcat.cluster.wadi.GerTomcatClusteringWadiDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.xmlbeans.XmlCursor;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class TomcatClusteringWADIConverter implements ElementConverter {
    private static final String TOMCAT_CLUSTERING_WADI_NS = GerTomcatClusteringWadiDocument.type.getDocumentElementName().getNamespaceURI();
    private static final String NAMING_NS = GerPatternType.type.getName().getNamespaceURI();
    private static final String CLUSTER_ELEMENT_NAME = "cluster";
    private static final String BACKING_STRATEGY_FACTORY_ELEMENT_NAME = "backing-strategy-factory";

    public void convertElement(XmlCursor cursor, XmlCursor end) {
        end.toCursor(cursor);
        end.toEndToken();

        while (cursor.hasNextToken() && cursor.isLeftOf(end)) {
            if (cursor.isStart()) {
                String localPart = cursor.getName().getLocalPart();
                cursor.setName(new QName(TOMCAT_CLUSTERING_WADI_NS, localPart));
                if (localPart.equals(CLUSTER_ELEMENT_NAME) || localPart.equals(BACKING_STRATEGY_FACTORY_ELEMENT_NAME)) {
                    convertChildrenToNamingNS(cursor);
                    cursor.toEndToken();
                }
            }
            cursor.toNextToken();
        }
    }

    protected void convertChildrenToNamingNS(XmlCursor cursor) {
        XmlCursor namingCursor = cursor.newCursor();
        try {
            if (namingCursor.toFirstChild()) {
                XmlCursor endNamingCursor = namingCursor.newCursor();
                try {
                    convertToNamingNS(namingCursor, endNamingCursor);
                } finally {
                    endNamingCursor.dispose();
                }
            }
        } finally {
            namingCursor.dispose();
        }
    }

    protected void convertToNamingNS(XmlCursor cursor, XmlCursor end) {
        end.toCursor(cursor);
        end.toEndToken();
        while (cursor.hasNextToken() && cursor.isLeftOf(end)) {
            if (cursor.isStart()) {
                String localPart = cursor.getName().getLocalPart();
                cursor.setName(new QName(NAMING_NS, localPart));
            }
            cursor.toNextToken();
        }
    }

}
