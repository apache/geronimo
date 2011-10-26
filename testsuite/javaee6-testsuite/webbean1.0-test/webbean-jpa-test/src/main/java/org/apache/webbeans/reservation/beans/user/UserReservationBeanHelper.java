/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
package org.apache.webbeans.reservation.beans.user;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.webbeans.reservation.entity.Hotel;
import org.apache.webbeans.reservation.model.ReservationModel;
import org.apache.webbeans.reservation.util.JSFUtility;

@Named
@RequestScoped
public class UserReservationBeanHelper {
    private HtmlDataTable model;

    private @Inject
    UserReservationBean userReservationBean;

    /**
     * @return the model
     */
    public HtmlDataTable getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(HtmlDataTable model) {
        this.model = model;
    }

    public String addReservation() {
        if (userReservationBean.getReservationDate() == null) {
            JSFUtility.addErrorMessage("Reservation date can not be empty!", "");
            return null;
        }

        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy").parse(userReservationBean.getReservationDate());

        } catch (Exception e) {

            JSFUtility.addErrorMessage("Please give a date with dd/MM/yyyy", "");
            return null;
        }

        if (userReservationBean.getConversation().isTransient()) {
            userReservationBean.getConversation().begin();

            JSFUtility.addInfoMessage("Reservation conversation with started with id : "
                    + userReservationBean.getConversation().getId(), "");
        }

        Hotel hotel = (Hotel) model.getRowData();

        SelectItem item = new SelectItem();
        item.setValue(hotel.getId());
        item.setLabel(hotel.getName());

        if (userReservationBean.contains(item.getValue()) != null) {
            JSFUtility.addErrorMessage("Given hotel is already added", "");

            return null;
        }

        userReservationBean.getReservations().add(item);

        ReservationModel model = new ReservationModel(item, date);
        userReservationBean.getModels().put(item.getValue().toString(), model);

        return null;

    }

}
