package org.apache.geronimo.deployment.service;

/**
 * adapter interface for multiple copies of xml gbean type.
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/09 18:00:57 $
 *
 * */
public interface GBeanAdapter {
    String getName();
    String getClass1();
    int getAttributeCount();
    String getAttributeName(int i);
    String getAttributeType(int i);
    String getAttributeStringValue(int i);
    int getReferenceCount();
    String getReferenceName(int i);
    String getReferenceStringValue(int i);
    int getReferencesCount();
    String getReferencesName(int i);
    String[] getReferencesPatternArray(int i);
}
