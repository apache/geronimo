package org.apache.geronimo.system.util;

import java.io.Serializable;

import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;


public class EncryptionManagerWrapperGBean {

    public String encrypt(Serializable source) {
        return EncryptionManager.encrypt(source);
    }

    public Serializable decrypt(String source) {
        return EncryptionManager.decrypt(source);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EncryptionManagerWrapperGBean.class, "GBean");
        infoBuilder.addOperation("encrypt", new Class[] {Serializable.class}, "java.io.Serializable");
        infoBuilder.addOperation("decrypt", new Class[] {String.class}, "java.lang.String");
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
