package org.apache.geronimo.security.realm.providers;

import java.util.Map;

public class SiteminderHeaderHandler implements HeaderHandler {
      
    public String getSession(Map<String,String> headerMap) {
        return null;
    }

    public String getUser(Map<String, String> headerMap) {
        String username=headerMap.get("SM_USER");
        return username;
    }
}
