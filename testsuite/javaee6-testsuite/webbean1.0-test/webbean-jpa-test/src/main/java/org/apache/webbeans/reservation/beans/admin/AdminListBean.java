/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.reservation.beans.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.webbeans.reservation.controller.admin.AdminController;
import org.apache.webbeans.reservation.entity.Hotel;
import org.apache.webbeans.reservation.util.JSFUtility;

@Named
@SessionScoped
public class AdminListBean implements Serializable
{

    private static final long serialVersionUID = 2552807646330035889L;

    private List<Hotel> hotels = new ArrayList<Hotel>();
    
    private @Inject @Default AdminController controller;
    
    private Hotel selected;
    
    private boolean renderedDetailPanel = false;
    
    
    public String update()
    {
        //System.out.println("enter AdminListBean.update");
        if(selected == null)
        {
            JSFUtility.addErrorMessage("Pleasee select the hotel to update", "");
            
            return null;
        }
        
        controller.updateHotel(selected.getId(), selected.getName(), selected.getStar(), selected.getCity(), selected.getCountry());
        
        JSFUtility.addInfoMessage("Hotel with name " + selected.getName()+ " is succesfully updated." , "");
        
        
        return null;
    }
    
    
    
    /**
     * @return the hotels
     */
    public List<Hotel> getHotels()
    {
        hotels = controller.getHotels();
        
        return hotels;
    }

    /**
     * @param hotels the hotels to set
     */
    public void setHotels(List<Hotel> hotels)
    {
        this.hotels = hotels;
    }

    /**
     * @return the selected
     */
    public Hotel getSelected()
    {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(Hotel selected)
    {
        this.selected = selected;
    }

    /**
     * @return the renderedDetailPanel
     */
    public boolean isRenderedDetailPanel()
    {
        return renderedDetailPanel;
    }

    /**
     * @param renderedDetailPanel the renderedDetailPanel to set
     */
    public void setRenderedDetailPanel(boolean renderedDetailPanel)
    {
        this.renderedDetailPanel = renderedDetailPanel;
    }
    
}
