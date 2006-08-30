/**
 * BookQuote.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC1 Sep 29, 2004 (08:29:40 EDT) WSDL2Java emitter.
 */

package org.apache.geronimo.axis.builder.bookquote;

public interface BookQuote extends java.rmi.Remote {
    public float getBookPrice(java.lang.String isbn) throws java.rmi.RemoteException, InvalidIsbnException;
}
