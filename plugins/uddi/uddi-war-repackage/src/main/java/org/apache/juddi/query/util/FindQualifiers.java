/*
 * Copyright 2001-2008 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.juddi.query.util;

import java.util.List;

import org.apache.juddi.v3.error.ErrorMessage;
import org.apache.juddi.v3.error.UnsupportedException;
import org.uddi.v3_service.DispositionReportFaultMessage;

/**
 * @author <a href="mailto:jfaath@apache.org">Jeff Faath</a>
 */
public class FindQualifiers {

	public static final String AND_ALL_KEYS = "andAllKeys";
	public static final String AND_ALL_KEYS_TMODEL = "uddi:uddi.org:findqualifier:andallkeys";

	public static final String APPROXIMATE_MATCH = "approximateMatch";
	public static final String APPROXIMATE_MATCH_TMODEL = "uddi:uddi.org:findqualifier:approximatematch";

	public static final String BINARY_SORT = "binarySort";
	public static final String BINARY_SORT_TMODEL = "uddi:uddi.org:sortorder:binarysort";

	public static final String BINDING_SUBSET = "bindingSubset";
	public static final String BINDING_SUBSET_TMODEL = "uddi:uddi.org:findqualifier:bindingsubset";

	public static final String CASE_INSENSITIVE_SORT = "caseInsensitiveSort";
	public static final String CASE_INSENSITIVE_SORT_TMODEL = "uddi:uddi.org:findqualifier:caseinsensitivesort";

	public static final String CASE_INSENSITIVE_MATCH = "caseInsensitiveMatch";
	public static final String CASE_INSENSITIVE_MATCH_TMODEL = "uddi:uddi.org:findqualifier:caseinsensitivematch";

	public static final String CASE_SENSITIVE_SORT = "caseSensitiveSort";
	public static final String CASE_SENSITIVE_SORT_TMODEL = "uddi:uddi.org:findqualifier:casesensitivesort";

	public static final String CASE_SENSITIVE_MATCH = "caseSensitiveMatch";
	public static final String CASE_SENSITIVE_MATCH_TMODEL = "uddi:uddi.org:findqualifier:casesensitivematch";

	public static final String COMBINE_CATEGORY_BAGS = "combineCategoryBags";
	public static final String COMBINE_CATEGORY_BAGS_TMODEL = "uddi:uddi.org:findqualifier:combinecategorybags";

	public static final String DIACRITIC_INSENSITIVE_MATCH = "diacriticInsensitiveMatch";
	public static final String DIACRITIC_INSENSITIVE_MATCH_TMODEL = "uddi:uddi.org:findqualifier:diacriticsinsensitivematch";

	public static final String DIACRITIC_SENSITIVE_MATCH = "diacriticSensitiveMatch";
	public static final String DIACRITIC_SENSITIVE_MATCH_TMODEL = "uddi:uddi.org:findqualifier:diacriticssensitivematch";

	public static final String EXACT_MATCH = "exactMatch";
	public static final String EXACT_MATCH_TMODEL = "uddi:uddi.org:findqualifier:exactmatch";
	
    public static final String EXACT_NAME_MATCH = "exactNameMatch";
    public static final String EXACT_NAME_MATCH_TMODEL = "uddi:uddi.org:findqualifier:exactnamematch";	

	public static final String SIGNATURE_PRESENT = "signaturePresent";
	public static final String SIGNATURE_PRESENT_TMODEL = "uddi:uddi.org:findqualifier:signaturepresent";

	public static final String OR_ALL_KEYS = "orAllKeys";
	public static final String OR_ALL_KEYS_TMODEL = "uddi:uddi.org:findqualifier:orallkeys";

	public static final String OR_LIKE_KEYS = "orLikeKeys";
	public static final String OR_LIKE_KEYS_TMODEL = "uddi:uddi.org:findqualifier:orlikekeys";

	public static final String SERVICE_SUBSET = "serviceSubset";
	public static final String SERVICE_SUBSET_TMODEL = "uddi:uddi.org:findqualifier:servicesubset";

	public static final String SORT_BY_NAME_ASC = "sortByNameAsc";
	public static final String SORT_BY_NAME_ASC_TMODEL = "uddi:uddi.org:findqualifier:sortbynameasc";

	public static final String SORT_BY_NAME_DESC = "sortByNameDesc";
	public static final String SORT_BY_NAME_DESC_TMODEL = "uddi:uddi.org:findqualifier:sortbynamedesc";

	public static final String SORT_BY_DATE_ASC = "sortByDateAsc";
	public static final String SORT_BY_DATE_ASC_TMODEL = "uddi:uddi.org:findqualifier:sortbydateasc";
	
	public static final String SORT_BY_DATE_DESC = "sortByDateDesc";
	public static final String SORT_BY_DATE_DESC_TMODEL = "uddi:uddi.org:findqualifier:sortbydatedesc";

	public static final String SUPPRESS_PROJECTED_SERVICES = "suppressProjectedServices";
	public static final String SUPPRESS_PROJECTED_SERVICES_TMODEL = "uddi:uddi.org:findqualifier:suppressprojectedservices";

	public static final String UTS_10 = "UTS-10";
	public static final String UTS_10_TMODEL = "uddi:uddi.org:sortorder:uts-10";
	
	private boolean andAllKeys = false;
	private boolean approximateMatch = false;
	private boolean binarySort = false;
	private boolean bindingSubset = false;
	private boolean caseInsensitiveSort = false;
	private boolean caseInsensitiveMatch = false;
	private boolean caseSensitiveSort = false;
	private boolean caseSensitiveMatch = false;
	private boolean combineCategoryBags = false;
	private boolean diacriticInsensitiveMatch = false;
	private boolean diacriticSensitiveMatch = false;
	private boolean exactMatch = false;
	private boolean signaturePresent = false;
	private boolean orAllKeys = false;
	private boolean orLikeKeys = false;
	private boolean serviceSubset = false;
	private boolean sortByNameAsc = false;
	private boolean sortByNameDesc = false;
	private boolean sortByDateAsc = false;
	private boolean sortByDateDesc = false;
	private boolean suppressProjectedServices = false;
	private boolean uts10 = false;

	public FindQualifiers() {
		// These are the defaults as defined by the UDDI specification.
		this.setExactMatch(true);
		this.setCaseSensitiveMatch(true);
		this.setCaseSensitiveSort(true);
		this.setDiacriticSensitiveMatch(true);
		
	}

	public void mapApiFindQualifiers(org.uddi.api_v3.FindQualifiers apiFindQualifiers) 
			throws DispositionReportFaultMessage {
		if (apiFindQualifiers == null)
			return;
		
		List<String> fqList = apiFindQualifiers.getFindQualifier();
		if (fqList != null) {
			for (String fq : fqList) {
				if (fq.equalsIgnoreCase(AND_ALL_KEYS) || fq.equalsIgnoreCase(AND_ALL_KEYS_TMODEL))
					this.setAndAllKeys(true);
				else if (fq.equalsIgnoreCase(APPROXIMATE_MATCH) || fq.equalsIgnoreCase(APPROXIMATE_MATCH_TMODEL))
					this.setApproximateMatch(true);
				else if (fq.equalsIgnoreCase(BINARY_SORT) || fq.equalsIgnoreCase(BINARY_SORT_TMODEL))
					this.setBinarySort(true);
				else if (fq.equalsIgnoreCase(BINDING_SUBSET) || fq.equalsIgnoreCase(BINDING_SUBSET_TMODEL))
					this.setBindingSubset(true);
				else if (fq.equalsIgnoreCase(CASE_INSENSITIVE_SORT) || fq.equalsIgnoreCase(CASE_INSENSITIVE_SORT_TMODEL))
					this.setCaseInsensitiveSort(true);
				else if (fq.equalsIgnoreCase(CASE_INSENSITIVE_MATCH) || fq.equalsIgnoreCase(CASE_INSENSITIVE_MATCH_TMODEL))
					this.setCaseInsensitiveMatch(true);
				else if (fq.equalsIgnoreCase(CASE_SENSITIVE_SORT) || fq.equalsIgnoreCase(CASE_SENSITIVE_SORT_TMODEL))
					this.setCaseSensitiveSort(true);
				else if (fq.equalsIgnoreCase(CASE_SENSITIVE_MATCH) || fq.equalsIgnoreCase(CASE_SENSITIVE_MATCH_TMODEL))
					this.setCaseSensitiveMatch(true);
				else if (fq.equalsIgnoreCase(COMBINE_CATEGORY_BAGS) || fq.equalsIgnoreCase(COMBINE_CATEGORY_BAGS_TMODEL))
					this.setCombineCategoryBags(true);
				else if (fq.equalsIgnoreCase(DIACRITIC_INSENSITIVE_MATCH) || fq.equalsIgnoreCase(DIACRITIC_INSENSITIVE_MATCH_TMODEL))
					this.setDiacriticInsensitiveMatch(true);
				else if (fq.equalsIgnoreCase(DIACRITIC_SENSITIVE_MATCH) || fq.equalsIgnoreCase(DIACRITIC_SENSITIVE_MATCH_TMODEL))
					this.setDiacriticSensitiveMatch(true);
				else if (fq.equalsIgnoreCase(EXACT_MATCH) || fq.equalsIgnoreCase(EXACT_MATCH_TMODEL))
					this.setExactMatch(true);
				else if (fq.equalsIgnoreCase(EXACT_NAME_MATCH) || fq.equalsIgnoreCase(EXACT_NAME_MATCH_TMODEL))
                    this.setExactMatch(true);
				else if (fq.equalsIgnoreCase(SIGNATURE_PRESENT) || fq.equalsIgnoreCase(SIGNATURE_PRESENT_TMODEL))
					this.setSignaturePresent(true);
				else if (fq.equalsIgnoreCase(OR_ALL_KEYS) || fq.equalsIgnoreCase(OR_ALL_KEYS_TMODEL))
					this.setOrAllKeys(true);
				else if (fq.equalsIgnoreCase(OR_LIKE_KEYS) || fq.equalsIgnoreCase(OR_LIKE_KEYS_TMODEL))
					this.setOrLikeKeys(true);
				else if (fq.equalsIgnoreCase(SERVICE_SUBSET) || fq.equalsIgnoreCase(SERVICE_SUBSET_TMODEL))
					this.setServiceSubset(true);
				else if (fq.equalsIgnoreCase(SORT_BY_NAME_ASC) || fq.equalsIgnoreCase(SORT_BY_NAME_ASC_TMODEL))
					this.setSortByNameAsc(true);
				else if (fq.equalsIgnoreCase(SORT_BY_NAME_DESC) || fq.equalsIgnoreCase(SORT_BY_NAME_DESC_TMODEL))
					this.setSortByNameDesc(true);
				else if (fq.equalsIgnoreCase(SORT_BY_DATE_ASC) || fq.equalsIgnoreCase(SORT_BY_DATE_ASC_TMODEL))
					this.setSortByDateAsc(true);
				else if (fq.equalsIgnoreCase(SORT_BY_DATE_DESC) || fq.equalsIgnoreCase(SORT_BY_DATE_DESC_TMODEL))
					this.setSortByDateDesc(true);
				else if (fq.equalsIgnoreCase(SUPPRESS_PROJECTED_SERVICES) || fq.equalsIgnoreCase(SUPPRESS_PROJECTED_SERVICES_TMODEL))
					this.setSuppressProjectedServices(true);
				else if (fq.equalsIgnoreCase(UTS_10) || fq.equalsIgnoreCase(UTS_10_TMODEL))
					this.setUts10(true);
				else 
					throw new UnsupportedException(new ErrorMessage("errors.Unsupported.findQualifier", fq));
			}
		}
	}
	
	public boolean isAndAllKeys() {
		return andAllKeys;
	}
	public void setAndAllKeys(boolean andAllKeys) {
		this.andAllKeys = andAllKeys;
		this.orAllKeys = !andAllKeys;
		this.orLikeKeys = !andAllKeys;
	}

	public boolean isApproximateMatch() {
		return approximateMatch;
	}
	public void setApproximateMatch(boolean approximateMatch) {
		this.approximateMatch = approximateMatch;
		this.exactMatch = !approximateMatch;
	}

	public boolean isBinarySort() {
		return binarySort;
	}
	public void setBinarySort(boolean binarySort) {
		this.binarySort = binarySort;
		this.uts10 = !binarySort;
	}

	public boolean isBindingSubset() {
		return bindingSubset;
	}
	public void setBindingSubset(boolean bindingSubset) {
		this.bindingSubset = bindingSubset;
		this.combineCategoryBags = !bindingSubset;
		this.bindingSubset = !bindingSubset;
	}

	public boolean isCaseInsensitiveSort() {
		return caseInsensitiveSort;
	}
	public void setCaseInsensitiveSort(boolean caseInsensitiveSort) {
		this.caseInsensitiveSort = caseInsensitiveSort;
		this.caseSensitiveSort = !caseInsensitiveSort;
	}

	public boolean isCaseInsensitiveMatch() {
		return caseInsensitiveMatch;
	}
	public void setCaseInsensitiveMatch(boolean caseInsensitiveMatch) {
		this.caseInsensitiveMatch = caseInsensitiveMatch;
		this.caseSensitiveMatch = !caseInsensitiveMatch;
		this.exactMatch = !caseInsensitiveMatch;
	}

	public boolean isCaseSensitiveSort() {
		return caseSensitiveSort;
	}
	public void setCaseSensitiveSort(boolean caseSensitiveSort) {
		this.caseSensitiveSort = caseSensitiveSort;
		this.caseInsensitiveSort = !caseSensitiveSort;
	}

	public boolean isCaseSensitiveMatch() {
		return caseSensitiveMatch;
	}
	public void setCaseSensitiveMatch(boolean caseSensitiveMatch) {
		this.caseSensitiveMatch = caseSensitiveMatch;
		this.caseInsensitiveMatch = !caseSensitiveMatch;
	}
	
	public boolean isCombineCategoryBags() {
		return combineCategoryBags;
	}
	public void setCombineCategoryBags(boolean combineCategoryBags) {
		this.combineCategoryBags = combineCategoryBags;
		this.serviceSubset = !combineCategoryBags;
		this.bindingSubset = !combineCategoryBags;
	}

	public boolean isDiacriticInsensitiveMatch() {
		return diacriticInsensitiveMatch;
	}
	public void setDiacriticInsensitiveMatch(boolean diacriticInsensitiveMatch) {
		this.diacriticInsensitiveMatch = diacriticInsensitiveMatch;
		this.diacriticSensitiveMatch = !diacriticInsensitiveMatch;
	}

	public boolean isDiacriticSensitiveMatch() {
		return diacriticSensitiveMatch;
	}
	public void setDiacriticSensitiveMatch(boolean diacriticSensitiveMatch) {
		this.diacriticSensitiveMatch = diacriticSensitiveMatch;
		this.diacriticInsensitiveMatch = !diacriticSensitiveMatch;
	}

	public boolean isExactMatch() {
		return exactMatch;
	}
	public void setExactMatch(boolean exactMatch) {
		this.exactMatch = exactMatch;
		this.approximateMatch = !exactMatch;
		this.caseInsensitiveMatch = !exactMatch;
	}

	public boolean isSignaturePresent() {
		return signaturePresent;
	}
	public void setSignaturePresent(boolean signaturePresent) {
		this.signaturePresent = signaturePresent;
	}

	public boolean isOrAllKeys() {
		return orAllKeys;
	}
	public void setOrAllKeys(boolean orAllKeys) {
		this.orAllKeys = orAllKeys;
		this.andAllKeys = !orAllKeys;
		this.orLikeKeys = !orAllKeys;
	}

	public boolean isOrLikeKeys() {
		return orLikeKeys;
	}
	public void setOrLikeKeys(boolean orLikeKeys) {
		this.orLikeKeys = orLikeKeys;
		this.andAllKeys = !orLikeKeys;
		this.orAllKeys = !orLikeKeys;
	}

	public boolean isServiceSubset() {
		return serviceSubset;
	}
	public void setServiceSubset(boolean serviceSubset) {
		this.serviceSubset = serviceSubset;
		this.combineCategoryBags = !serviceSubset;
		this.bindingSubset = !serviceSubset;
	}

	public boolean isSortByNameAsc() {
		return sortByNameAsc;
	}
	public void setSortByNameAsc(boolean sortByNameAsc) {
		this.sortByNameAsc = sortByNameAsc;
		this.sortByNameDesc = !sortByNameAsc;
	}

	public boolean isSortByNameDesc() {
		return sortByNameDesc;
	}
	public void setSortByNameDesc(boolean sortByNameDesc) {
		this.sortByNameDesc = sortByNameDesc;
		this.sortByNameAsc = !sortByNameDesc;
	}

	public boolean isSortByDateAsc() {
		return sortByDateAsc;
	}
	public void setSortByDateAsc(boolean sortByDateAsc) {
		this.sortByDateAsc = sortByDateAsc;
		this.sortByDateDesc = !sortByDateAsc;
	}

	public boolean isSortByDateDesc() {
		return sortByDateDesc;
	}
	public void setSortByDateDesc(boolean sortByDateDesc) {
		this.sortByDateDesc = sortByDateDesc;
		this.sortByDateAsc = !sortByDateDesc;
	}

	public boolean isSuppressProjectedServices() {
		return suppressProjectedServices;
	}
	public void setSuppressProjectedServices(boolean suppressProjectedServices) {
		this.suppressProjectedServices = suppressProjectedServices;
	}

	public boolean isUts10() {
		return uts10;
	}
	public void setUts10(boolean uts10) {
		this.uts10 = uts10;
		this.binarySort = !uts10;
	}
	
	
}
