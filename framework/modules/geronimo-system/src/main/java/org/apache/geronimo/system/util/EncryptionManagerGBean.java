package org.apache.geronimo.system.util;

import java.io.Serializable;

import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
/**
 * A GBean class to invoke EncryptionManager decrypt
 * method for command line utility for example unlockKeystore
 *
 */
public class EncryptionManagerGBean implements GBeanLifecycle, Serializable {

	public void doFail() {
		// TODO Auto-generated method stub

	}

	public void doStart() throws Exception {
		// TODO Auto-generated method stub

	}

	public void doStop() throws Exception {
		// TODO Auto-generated method stub

	}
	
	public String decrypt(String text){
        return (String)EncryptionManager.decrypt(text);
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EncryptionManagerGBean.class, "GBean");
        infoBuilder.addOperation("decrypt", new Class[] {String.class}, "java.lang.String");
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
