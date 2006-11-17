/**
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.management.geronimo;

import java.math.BigInteger;

/**
 * Management interface for dealing with a specific CertificateRequestStore
 *
 * @version $Rev$ $Date$
 */
public interface CertificateRequestStore {
    /**
     * This method returns the ids of all certificate requests in the store.
     */
    public String[] getAllRequestIds();

    /**
     * This method returns the ids of all certificate requests with verification due.
     */
    public String[] getVerificatonDueRequestIds();

    /**
     * This method returns the ids of all certificate requests that are verified.
     */
    public String[] getVerifiedRequestIds();

    /**
     * This method returns the certificate request text corresponding to a specified id.
     * @param id Id of the certificate request.
     */
    public String getRequest(String id);

    /**
     * This method deletes a certificate request with the specified id.
     * @param id Id of the certificate request to be deleted.
     * @return True if the request is deleted succssfully
     */
    public boolean deleteRequest(String id);

    /**
     * This method stores the given certificate request under the given id.  If a request with the id
     * exists in the store, it will generate a new id and store the request under that id.
     * @param id Id under which the certificate request is to be stored
     * @param csrText Certificate Request text
     * @return Id under which the certificate request is stored
     */
    public String storeRequest(String id, String csrText);

    /**
     * This method sets the status of the specifed certificate request as verified.
     * @param id Id of the certificate request
     * @return True if the status is set successfully.
     */
    public boolean setRequestVerified(String id);

    /**
     * This method sets the status of a certificate request as fulfilled.
     * @param id Id of the certificate request
     * @param sNo Serial number of the certificate issued against the certificate request.
     * @return True if the operation is successfull.
     */
    public boolean setRequestFulfilled(String id, BigInteger sNo);

    /**
     * This method returns the Serial number of the certificate issued against the certificate request
     * specified by the given id.
     * @param id Id of the certificate request
     * @return Serial number of the certificate issued.
     * @return null if there is no such certificate request or the certificate request is not fulfilled.
     */
    public BigInteger getSerialNumberForRequest(String id);

    /**
     * This method removes the certificate request id from the status list.
     * @param id Id of the certificate request to be removed.
     * @param sNo Serial number of certificate issued against the certificate request whose Id is to be removed.
     */
    public void removeRequestStatus(String id, BigInteger sNo);
}
