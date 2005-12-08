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

import java.math.BigDecimal;

public class AuditModel {
	// TODO: For now using composition, later move to base classes
	public static final int PRICE = 0;
	public static final int VOLUME = 1;

	private BigDecimal aPrice;
	private double aVolume;
	private int type;

	public AuditModel(BigDecimal aPrice) {
		this.aPrice = aPrice;
		type = PRICE;
	}

	public AuditModel(double aVolume) {
		this.aVolume = aVolume;
		type = VOLUME;
	}

	public BigDecimal getAuditPrice() {
		return aPrice;
	}

	public void setAuditPrice(BigDecimal aPrice) {
	 this.aPrice = aPrice;
	}

	public double getAuditVolume() {
		return aVolume;
	}

	public void setAuditVolume(double aVolume) {
		this.aVolume = aVolume;
	}

	public int getType() {
		return type;
	}
}
