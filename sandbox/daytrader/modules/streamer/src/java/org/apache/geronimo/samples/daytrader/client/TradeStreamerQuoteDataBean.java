/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable 
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.samples.daytrader.client;

import java.util.Date;
import java.math.BigDecimal;
import org.apache.geronimo.samples.daytrader.*;

public class TradeStreamerQuoteDataBean extends QuoteDataBean {

	private BigDecimal auditPrice;
	private double auditVolume;
	private Date lastUpdate;
	private ChangeModel changeModel;
	private AuditModel auditPriceModel;
	private AuditModel auditVolumeModel;

	/**
	 * Gets the price
	 * @return Returns a BigDecimal
	 */
	public BigDecimal getAuditPrice() {
		return auditPrice;
	}

	/**
	 * Sets the price
	 * @param price The price to set
	 */
	public void setAuditPrice(BigDecimal auditPrice) {
		this.auditPrice = auditPrice;
		setAuditPriceModel(auditPrice);
	}

	/**
	 * Gets the volume
	 * @return Returns a BigDecimal
	 */
	public double getAuditVolume() {
		return auditVolume;
	}

	/**
	 * Sets the volume
	 * @param volume The volume to set
	 */
	public void setAuditVolume(double auditVolume) {
		this.auditVolume = auditVolume;
		setAuditVolumeModel(auditVolume);
	}

	/**
	 * Gets the last update date of the quote
	 * @return Returns the last update date
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Sets the change since open
	 * Overridden to provide model class for JTable to render
	 */
	public void setChange(double change) {
		changeModel = new ChangeModel(change);
		super.setChange(change);
	}

	/**
	 * Gets the change model since open
	 * @return the change model
	 */
	public ChangeModel getChangeModel() {
		return changeModel;
	}

	/**
	 * Gets the audit price model
	 * @return the audit price model
	 */
	public AuditModel getAuditPriceModel() {
		return auditPriceModel;
	}

	/**
	 * Sets the audit price model
	 * @param price the audit price
	 */
	public void setAuditPriceModel(BigDecimal aPrice) {
		if (auditPriceModel != null) {
			auditPriceModel.setAuditPrice(aPrice);
		}
		else {
			auditPriceModel = new AuditModel(aPrice);
		}
	}

	/**
	 * Gets the audit volume model
	 * @return the audit volume model
	 */
	public AuditModel getAuditVolumeModel() {
		return auditVolumeModel;
	}

	/**
	 * Sets the audit volume model
	 * @param volume the audit volume
	 */
	public void setAuditVolumeModel(double aVolume) {
		if (auditVolumeModel != null) {
			auditVolumeModel.setAuditVolume(aVolume);
		}
		else {
			auditVolumeModel = new AuditModel(aVolume);
		}
	}

	/**
	 * Sets the last update date of the quote
	 * @param lastUpdate the last update date of the quote
	 */
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String toString() {
		return "\n\tAudit Quote Data for: " + getSymbol() +
			//"\n\t\t   statsOrder: " + getStatsOrder() +
			"\n\t\t       volume: " + getVolume() +
			"\n\t\t        price: " + getPrice() +
			"\n\t\t audit volume: " + getAuditVolume() +
			"\n\t\t  audit price: " + getAuditPrice();
	}
}
