package org.apache.geronimo.connector.deployment.dconfigbean;

import java.util.Map;

import javax.enterprise.deploy.model.DDBean;

import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/09 23:13:27 $
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

    public interface ConfigPropertiesSource {
        GerConfigPropertySettingType[] getConfigPropertySettingArray();
        GerConfigPropertySettingType addNewConfigPropertySetting();
    }
}
