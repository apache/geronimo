package org.apache.geronimo.connector.deployment.dconfigbean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.model.XpathListener;

import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/18 20:57:07 $
 *
 * */
public class ConfigPropertiesHelper {

    public static void initializeConfigSettings(DDBean ddBean, ConfigPropertiesSource configPropertiesSource, Map configPropertiesMap) {
        DDBean[] configProperties = ddBean.getChildBean("config-property");
        GerConfigPropertySettingType[] configPropertySettings = configPropertiesSource.getConfigPropertySettingArray();

        if (configPropertySettings.length == 0) {
            //we are new
            for (int i = 0; i < configProperties.length; i++) {
                DDBean configProperty = configProperties[i];
                GerConfigPropertySettingType configPropertySetting = configPropertiesSource.addNewConfigPropertySetting();
                String name = configProperty.getText("config-property-name")[0];
                ConfigPropertySettingDConfigBean configPropertySettingDConfigBean = new ConfigPropertySettingDConfigBean(configProperty, configPropertySetting);
                configPropertiesMap.put(name, configPropertySettingDConfigBean);
            }
        } else {
            //we are read in from xml.  Check correct length
            assert configProperties.length == configPropertySettings.length;
            for (int i = 0; i < configProperties.length; i++) {
                DDBean configProperty = configProperties[i];
                GerConfigPropertySettingType configPropertySetting = configPropertySettings[i];
                String name = configProperty.getText("config-property-name")[0];
                assert name.equals(configPropertySetting.getName());
                ConfigPropertySettingDConfigBean configPropertySettingDConfigBean = new ConfigPropertySettingDConfigBean(configProperty, configPropertySetting);
                configPropertiesMap.put(name, configPropertySettingDConfigBean);
            }
        }
    }

    public static XpathListener initialize(DDBean parentDDBean, final ConfigPropertiesHelper.ConfigPropertiesSource configPropertiesSource) {
        DDBean[] beans = parentDDBean.getChildBean("config-property");
        ConfigPropertySettings[] configs = new ConfigPropertySettings[beans.length];
        Set xmlBeans = new HashSet(Arrays.asList(configPropertiesSource.getConfigPropertySettingArray()));
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            String[] names = bean.getText("config-property-name");
            String name = names.length == 1 ? names[0] : "";
            GerConfigPropertySettingType target = null;
            for (Iterator it = xmlBeans.iterator(); it.hasNext();) {
                GerConfigPropertySettingType setting = (GerConfigPropertySettingType) it.next();
                if (setting.getName().equals(name)) {
                    target = setting;
                    xmlBeans.remove(target);
                    break;
                }
            }
            if (target == null) {
                target = configPropertiesSource.addNewConfigPropertySetting();
            }
            configs[i] = new ConfigPropertySettings();
            configs[i].initialize(target, bean);
        }
        for (Iterator it = xmlBeans.iterator(); it.hasNext();) { // used to be in XmlBeans, no longer anything matching in J2EE DD
            GerConfigPropertySettingType target = (GerConfigPropertySettingType) it.next();
            GerConfigPropertySettingType[] xmlConfigs = configPropertiesSource.getConfigPropertySettingArray();
            for (int i = 0; i < xmlConfigs.length; i++) {
                if (xmlConfigs[i] == target) {
                    configPropertiesSource.removeConfigPropertySetting(i);
                    break;
                }
            }
        }
        configPropertiesSource.setConfigPropertySettings(configs);
        XpathListener configListener = new XpathListener() {
            public void fireXpathEvent(XpathEvent xpe) {
                ConfigPropertySettings[] configs = configPropertiesSource.getConfigPropertySettings();
                if (xpe.isAddEvent()) {
                    ConfigPropertySettings[] bigger = new ConfigPropertySettings[configs.length + 1];
                    System.arraycopy(configs, 0, bigger, 0, configs.length);
                    bigger[configs.length] = new ConfigPropertySettings();
                    bigger[configs.length].initialize(configPropertiesSource.addNewConfigPropertySetting(), xpe.getBean());
                    configPropertiesSource.setConfigPropertySettings(bigger);
                } else if (xpe.isRemoveEvent()) {
                    int index = -1;
                    for (int i = 0; i < configs.length; i++) {
                        if (configs[i].matches(xpe.getBean())) {
                            // remove the XMLBean
                            GerConfigPropertySettingType[] xmlConfigs = configPropertiesSource.getConfigPropertySettingArray();
                            for (int j = 0; j < xmlConfigs.length; j++) {
                                GerConfigPropertySettingType test = xmlConfigs[j];
                                if (test == configs[i].getConfigPropertySetting()) {
                                    configPropertiesSource.removeConfigPropertySetting(j);
                                    break;
                                }
                            }
                            // clean up the JavaBean
                            configs[i].dispose();
                            index = i;
                            break;
                        }
                    }
                    // remove the JavaBean from my list
                    if (index > -1) {
                        ConfigPropertySettings[] smaller = new ConfigPropertySettings[configs.length - 1];
                        System.arraycopy(configs, 0, smaller, 0, index);
                        System.arraycopy(configs, index + 1, smaller, index, smaller.length - index);
                        configPropertiesSource.setConfigPropertySettings(smaller);
                    }
                }
                // ignore change event (no contents, no attributes)
            }
        };
        parentDDBean.addXpathListener("config-property", configListener);
        return configListener;
    }

    public interface ConfigPropertiesSource {
        GerConfigPropertySettingType[] getConfigPropertySettingArray();

        GerConfigPropertySettingType addNewConfigPropertySetting();

        void removeConfigPropertySetting(int j);

        ConfigPropertySettings[] getConfigPropertySettings();

        void setConfigPropertySettings(ConfigPropertySettings[] configs);
    }
}
