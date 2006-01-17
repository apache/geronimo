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

import java.text.*;
import java.util.*;
import java.math.BigDecimal;
import javax.swing.table.*;

public class TradeQuoteAuditStats extends AbstractTableModel {
	public static final int SYMBOL_COL = 0;
	public static final int COMPANY_COL = 1;
	public static final int PRICE_COL = 2;
	public static final int AUDIT_PRICE_COL = 3;
	public static final int VOLUME_COL = 4;
	public static final int AUDIT_VOLUME_COL = 5;
	public static final int OPEN_COL = 6;
	public static final int LOW_COL = 7;
	public static final int HIGH_COL = 8;
	public static final int CHANGE_COL = 9;
	public static final int UPDATE_COL = 10;

  private static String columns[] = {
		"Symbol", "Company Name", "Price",
		"Audit Price", "Volume", "Audit Volume",
		"Open", "Low", "High",
		"Change", "Last Update"};

	private static Class columnClasses[] = {
		String.class, String.class, String.class,
		AuditModel.class, String.class, AuditModel.class,
		String.class, String.class, String.class,
		ChangeModel.class, String.class};

  private int numColumns = columns.length;

	private NumberFormat nf;
	private DateFormat df;
	private Map map;
	private ArrayList sortedList;
	private Object lock;
	private int lastSortCol = SYMBOL_COL;
	private boolean lastSortForward = true;

	public TradeQuoteAuditStats() {
		init();
	}

	public void init() {
		map = new HashMap();
		sortedList = new ArrayList();
		nf = NumberFormat.getCurrencyInstance();
		df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		lock = new Object();
	}

	public void updateSymbol(String symbol, String company, BigDecimal newPrice, BigDecimal open, BigDecimal low, BigDecimal high, double volume, long publishTime, BigDecimal priceChange, double volumeChange) {
		boolean newSymbol = false;
		TradeStreamerQuoteDataBean bean;

		synchronized(lock) {
			bean = getSymbol(symbol);
			if (bean == null) { // first time bean was used
				bean = new TradeStreamerQuoteDataBean();
				bean.setSymbol(symbol);
				bean.setCompanyName(company);
				bean.setPrice(newPrice);
				bean.setOpen(open);
				bean.setLow(low);
				bean.setHigh(high);
				bean.setVolume(volume);
				bean.setLastUpdate(new Date(publishTime));
				bean.setAuditPrice(priceChange);
				bean.setAuditVolume(volumeChange);
				bean.setChange(newPrice.subtract(open).doubleValue());
				newSymbol = true;
			}
			else { // update to an existing bean
				bean.setPrice(newPrice);
				bean.setOpen(open);
				bean.setLow(low);
				bean.setHigh(high);
				bean.setVolume(volume);
				bean.setLastUpdate(new Date(publishTime));
				bean.setAuditPrice((bean.getAuditPrice()).add(priceChange));
				bean.setAuditVolume(bean.getAuditVolume() + volumeChange);
				bean.setChange(newPrice.subtract(open).doubleValue());
			}

			map.put(symbol, bean);

			if (newSymbol) {
				sort(lastSortCol, false);
			}
		}
	}

	public TradeStreamerQuoteDataBean getSymbol(String symbol) {
		synchronized(lock) {
			TradeStreamerQuoteDataBean quote = (TradeStreamerQuoteDataBean)map.get(symbol);
			return quote;
		}
	}

	public TradeStreamerQuoteDataBean getSymbol(int row) {
		synchronized(lock) {
			Iterator it = sortedList.iterator();
			int ii = 0;
			while (true) {
				if (!it.hasNext()) {
					break;
				}
				TradeStreamerQuoteDataBean bean = (TradeStreamerQuoteDataBean)it.next();
				if (ii == row) {
					return bean;
				}
				ii++;
			}
			return null;
		}
	}

	public void clearStats() {
		synchronized(lock) {
			init();
		}
	}

  public int getColumnCount() {
    return numColumns;
  }

  public int getRowCount() {
		synchronized(lock) {
			return map.size();
		}
  }

  public Object getValueAt (int row, int column) {
		synchronized(lock) {
			Iterator it = sortedList.iterator();
			int ii = 0;
			while (true) {
				if (!it.hasNext()) {
					break;
				}
				TradeStreamerQuoteDataBean bean = (TradeStreamerQuoteDataBean)it.next();
				double price;
				if (ii == row) {
					switch (column) {
						case SYMBOL_COL:
							return bean.getSymbol();
						case COMPANY_COL:
							return bean.getCompanyName();
						case PRICE_COL:
							price = bean.getPrice().doubleValue();
							return nf.format(price);
						case AUDIT_PRICE_COL:
							return bean.getAuditPriceModel();
						case VOLUME_COL:
							return String.valueOf(bean.getVolume());
						case AUDIT_VOLUME_COL:
							return bean.getAuditVolumeModel();
						case OPEN_COL:
							price = bean.getOpen().doubleValue();
							return nf.format(price);
						case LOW_COL:
							price = bean.getLow().doubleValue();
							return nf.format(price);
						case HIGH_COL:
							price = bean.getHigh().doubleValue();
							return nf.format(price);
						case CHANGE_COL:
							return bean.getChangeModel();
						case UPDATE_COL:
							Date update = bean.getLastUpdate();
							return df.format(update);
						default:
							break;
					}
				}
				ii++;
			}
			return null;
		}
  }

  public void setValueAt (Object aValue, int row, int column) {
		// not editable
	}

  public String getColumnName(int columnIndex) {
    return columns[columnIndex];
  }

  public Class getColumnClass(int columnIndex) {
    return columnClasses[columnIndex];
  }

  public boolean isCellEditable(int row, int column) {
		return false;
  }

	public void sort(int column, boolean fromUserGUI) {
		synchronized(lock) {
			Collection values = map.values();
			sortedList.clear();
			sortedList.addAll(values);
			// TODO: be more carefull with obect allocation - make static comps?
			Comparator comp = new TradeQuoteAuditStatsComparator(column);
			Collections.sort(sortedList, comp);
			if (fromUserGUI) {
				if (column == lastSortCol) {
					if (lastSortForward) { // if we already sorted forward, reverse
						Collections.reverse(sortedList);
					}
					lastSortForward = !lastSortForward; // switch the last sort
				}
			}
			lastSortCol = column;
		}
	}
}
