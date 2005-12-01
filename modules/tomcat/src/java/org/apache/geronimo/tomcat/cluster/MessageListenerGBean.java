package org.apache.geronimo.tomcat.cluster;

import java.util.Map;

import org.apache.catalina.cluster.MessageListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;

public class MessageListenerGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

    private static final Log log = LogFactory.getLog(MessageListenerGBean.class);

    public static final String J2EE_TYPE = "MessageListener";
        
    private final MessageListener listener;
    private final MessageListenerGBean nextListener;
    private final String className;
 
    
    public MessageListenerGBean(){      
        listener = null;
        nextListener = null;
        className = null;
    }
    
    public MessageListenerGBean(String className, Map initParams, MessageListenerGBean nextListener) throws Exception{

        //Validate
        if (className == null){
            throw new IllegalArgumentException("className cannot be null.");
        }
        
        if (nextListener != null){
            if (!(nextListener.getInternalObject() instanceof MessageListener)){
                throw new IllegalArgumentException("nextListener is not of type MessageListener.");                
            }
            
            this.nextListener = nextListener;
        } else {
            this.nextListener = null;
        }
        
        this.className = className;
        
        //Create the Valve object
        listener = (MessageListener)Class.forName(className).newInstance();

        //Set the parameters
        setParameters(listener, initParams);
        
    }
    
    public void doStart() throws Exception {
        log.info(className + " started.");
    }

    public void doStop() throws Exception {
        log.info(className + " stopped.");
    }

    public void doFail() {
        log.info(className + " failed.");
    }

    public Object getInternalObject() {
        return listener;
    }

    public MessageListenerGBean getNextValve() {
        return nextListener;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(MessageListenerGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("NextListener", MessageListenerGBean.class, J2EE_TYPE);
        infoFactory.addOperation("getInternalObject");
        infoFactory.addOperation("getNextValve");
        infoFactory.setConstructor(new String[] { 
                "className", 
                "initParams", 
                "NextListener" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
