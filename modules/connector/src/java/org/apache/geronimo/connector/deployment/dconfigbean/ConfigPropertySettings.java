package org.apache.geronimo.connector.deployment.dconfigbean;

import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.XpathEvent;

/**
 * @version $Revision 1.0$
 */
public class ConfigPropertySettings extends XmlBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();
    private String type;
    private DDBean ddBean;
    private XpathListener typeListener;
    private XpathListener nameListener;

    public ConfigPropertySettings() {
        super(null, SCHEMA_TYPE_LOADER);
    }

    void initialize(GerConfigPropertySettingType xmlObject, DDBean configPropertyBean) {
        setXmlObject(xmlObject);
        ddBean = configPropertyBean;
        DDBean[] child = configPropertyBean.getChildBean("config-property-type");
        if(child.length == 1) {
            setConfigPropertyType(child[0]);
        }
        child = configPropertyBean.getChildBean("config-property-name");
        if(child.length == 1) {
            setConfigPropertyName(child[0]);
        }
        configPropertyBean.addXpathListener("config-property-type", typeListener = new XpathListener() {
            public void fireXpathEvent(XpathEvent xpe) {
                if(xpe.isChangeEvent() || xpe.isAddEvent()) {
                    setConfigPropertyType(xpe.getBean());
                } else if(xpe.isRemoveEvent()) {
                    setConfigPropertyType((String)null);
                }
            }
        });
        configPropertyBean.addXpathListener("config-property-name", nameListener = new XpathListener() {
            public void fireXpathEvent(XpathEvent xpe) {
                if(xpe.isChangeEvent() || xpe.isAddEvent()) {
                    setConfigPropertyName(xpe.getBean());
                } else if(xpe.isRemoveEvent()) {
                    setConfigPropertyName((String)null);
                }
            }
        });
    }

    boolean matches(DDBean target) {
        return target.equals(ddBean);
    }

    void dispose() {
        if(ddBean != null) {
            ddBean.removeXpathListener("config-property-type", typeListener);
            ddBean.removeXpathListener("config-property-name", nameListener);
        }
        nameListener = null;
        typeListener = null;
        ddBean = null;
    }

    GerConfigPropertySettingType getConfigPropertySetting() {
        return (GerConfigPropertySettingType)getXmlObject();
    }

    public String getConfigPropertyName() {
        return getConfigPropertySetting().getName();
    }

    private void setConfigPropertyName(DDBean configPropertyBean) {
        if(configPropertyBean == null) {
            setConfigPropertyName((String)null);
        } else {
            setConfigPropertyName(configPropertyBean.getText());
        }
    }

    private void setConfigPropertyName(String name) {
        String old = getConfigPropertyName();
        getConfigPropertySetting().setName(name);
        pcs.firePropertyChange("configPropertyName", old, name);
    }

    public String getConfigPropertyType() {
        return type;
    }

    private void setConfigPropertyType(DDBean configPropertyBean) {
        if(configPropertyBean == null) {
            setConfigPropertyType((String)null);
        } else {
            setConfigPropertyType(configPropertyBean.getText());
        }
    }

    private void setConfigPropertyType(String type) {
        String old = getConfigPropertyType();
        this.type = type;
        pcs.firePropertyChange("configPropertyType", old, type);
    }

    public String getConfigPropertyValue() {
        return getConfigPropertySetting().getStringValue();
    }

    public void setConfigPropertyValue(String configPropertyValue) {
        String old = getConfigPropertyValue();
        getConfigPropertySetting().setStringValue(configPropertyValue);
        pcs.firePropertyChange("configPropertyValue", old, configPropertyValue);
    }
}
