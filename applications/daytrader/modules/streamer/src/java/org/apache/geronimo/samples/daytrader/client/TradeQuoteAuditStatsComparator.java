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

import java.util.*;

public class TradeQuoteAuditStatsComparator implements Comparator {
	private int index;

	public TradeQuoteAuditStatsComparator(int index) {
		this.index = index;
	}

	public int compare(Object o1, Object o2) {
		TradeStreamerQuoteDataBean bean1 = (TradeStreamerQuoteDataBean)o1;
		TradeStreamerQuoteDataBean bean2 = (TradeStreamerQuoteDataBean)o2;

		switch (index) {
			case TradeQuoteAuditStats.SYMBOL_COL: {
				return bean1.getSymbol().compareTo(bean2.getSymbol());
			}
			case TradeQuoteAuditStats.COMPANY_COL: {
				return bean1.getCompanyName().compareTo(bean2.getCompanyName());
			}
			case TradeQuoteAuditStats.PRICE_COL: {
				return bean1.getPrice().compareTo(bean2.getPrice());
			}
			case TradeQuoteAuditStats.AUDIT_PRICE_COL: {
				return bean1.getAuditPrice().compareTo(bean2.getAuditPrice());
			}
			case TradeQuoteAuditStats.VOLUME_COL: {
				return (int)(bean1.getVolume() - bean2.getVolume());
			}
			case TradeQuoteAuditStats.AUDIT_VOLUME_COL: {
				return (int)(bean1.getAuditVolume() - bean2.getAuditVolume());
			}
			case TradeQuoteAuditStats.OPEN_COL: {
				return bean1.getOpen().compareTo(bean2.getOpen());
			}
			case TradeQuoteAuditStats.LOW_COL: {
				return bean1.getLow().compareTo(bean2.getLow());
			}
			case TradeQuoteAuditStats.HIGH_COL: {
				return bean1.getHigh().compareTo(bean2.getHigh());
			}
			case TradeQuoteAuditStats.CHANGE_COL: {
				return (int)(bean1.getChange() - bean2.getChange());
			}
			case TradeQuoteAuditStats.UPDATE_COL: {
				return bean1.getLastUpdate().compareTo(bean2.getLastUpdate());
			}
			default: {
				throw new IllegalArgumentException();
			}
		}
	}
}
