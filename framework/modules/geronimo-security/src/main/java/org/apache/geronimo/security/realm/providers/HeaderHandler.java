package org.apache.geronimo.security.realm.providers;

import java.util.Map;

public interface HeaderHandler {
    public String getUser(Map<String,String> headerMap);
    public String getSession(Map<String,String> headerMap);
    /* Add new methods as the work progresses */

}
