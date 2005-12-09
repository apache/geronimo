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

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.text.*;
import java.math.BigDecimal;

public class AuditRenderer extends DefaultTableCellRenderer {
	private NumberFormat nf = NumberFormat.getCurrencyInstance();

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof AuditModel) {
			AuditModel am = (AuditModel)value;
			// TODO: Terrible abuse of Swing and Models - fix this
			TradeStreamerQuoteDataBean quote = TradeClient.getTradeClient().getAuditStats().getSymbol(row);
			switch (am.getType()) {
				case AuditModel.PRICE: {
					BigDecimal aPrice = am.getAuditPrice();
					setText(nf.format(aPrice));
					if (aPrice.compareTo(quote.getPrice()) != 0) {
						setForeground(Color.red);
						setFont(table.getFont().deriveFont(Font.BOLD));
					}
					else {
						setForeground(table.getForeground());
						setFont(table.getFont());
					}
					break;
				}
				case AuditModel.VOLUME: {
					double aVolume = am.getAuditVolume();
					setText(String.valueOf(aVolume));
					if (aVolume != quote.getVolume()) {
						setForeground(Color.red);
						setFont(table.getFont().deriveFont(Font.BOLD));
					}
					else {
						setForeground(table.getForeground());
						setFont(table.getFont());
					}
					break;
				}
				default: {
					System.out.println("should not be reached");
				}
			}
		}
		return this;
	}
}

		
