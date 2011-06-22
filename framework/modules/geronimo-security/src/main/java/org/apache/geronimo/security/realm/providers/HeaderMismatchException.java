package org.apache.geronimo.security.realm.providers;

public class HeaderMismatchException extends Exception {

    public HeaderMismatchException(String hostName){
        super("The request originated from Host " + 
                hostName +
                "does not have valid headers.");
    }
}
