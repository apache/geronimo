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

package org.apache.juddi.query;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.juddi.query.util.DynamicQuery;
import org.apache.juddi.query.util.FindQualifiers;
import org.uddi.api_v3.Name;

/**
 * 
 * Returns the list of tmodel keys possessing the passed Name argument.
 * Output is restricted by list of tModel keys passed in.  If null, all tModels are searched.
 * Output is produced by building the appropriate JPA query based on input and find qualifiers.
 * 
 * From specification:
 * "This string value  represents the name of the tModel elements to be found.  Since tModel data only has a single 
 * name, only a single name may be passed.  The argument must match exactly since "exactMatch" is the default behavior, 
 * but if the "approximateMatch" findQualifier is used together with the appropriate wildcard character, then matching 
 * is done according to wildcard rules. See Section 5.1.6 About Wildcards for additional information.  The name MAY be 
 * marked with an xml:lang adornment.  If a language markup is specified, the search results report a match only on those 
 * entries that match both the name value and language criteria. The match on language is a leftmost case-insensitive 
 * comparison of the characters supplied. This allows one to find all tModels whose name begins with an "A" and are expressed 
 * in any dialect of French, for example.  Values which can be passed in the language criteria adornment MUST obey the rules 
 * governing the xml:lang data type as defined in Section 3.3.2.3 name."
 * 
 * @author <a href="mailto:jfaath@apache.org">Jeff Faath</a>
 */
public class FindTModelByNameQuery extends TModelQuery {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(FindTModelByNameQuery.class);

	public static List<?> select(EntityManager em, FindQualifiers fq, Name name, List<?> keysIn, DynamicQuery.Parameter... restrictions) {
		// If keysIn is not null and empty, then search is over.
		if ((keysIn != null) && (keysIn.size() == 0))
			return keysIn;
		
		if (name == null)
			return keysIn;
		
		DynamicQuery dynamicQry = new DynamicQuery(selectSQL);
		appendConditions(dynamicQry, fq, name);
		// Since this is a tModel, don't need to search the lazily deleted ones.
		dynamicQry.AND().pad().appendGroupedAnd(new DynamicQuery.Parameter(ENTITY_ALIAS + ".deleted", new Boolean(false), DynamicQuery.PREDICATE_EQUALS));
		if (restrictions != null && restrictions.length > 0)
			dynamicQry.AND().pad().appendGroupedAnd(restrictions);
		
		return getQueryResult(em, dynamicQry, keysIn, ENTITY_ALIAS + "." + KEY_NAME);
	}
	
	public static void appendConditions(DynamicQuery qry, FindQualifiers fq, Name name) {
		String namePredicate = DynamicQuery.PREDICATE_EQUALS;
		if (fq.isApproximateMatch()) {
			namePredicate = DynamicQuery.PREDICATE_LIKE;
		}

		qry.WHERE().pad().openParen().pad();

		String nameTerm = ENTITY_ALIAS + ".name";
		String nameValue = name.getValue();
		
		namePredicate = nameValue.indexOf("%") > -1 ? DynamicQuery.PREDICATE_LIKE : namePredicate;
		
		if (fq.isCaseInsensitiveMatch()) {
			nameTerm = "upper(" + ENTITY_ALIAS + ".name)";
			nameValue = name.getValue().toUpperCase();
		}
		// JUDDI-235: wildcards are provided by user (only commenting in case a new interpretation arises)
		//if (fq.isApproximateMatch())
		//	nameValue = nameValue.endsWith(DynamicQuery.WILDCARD)?nameValue:nameValue + DynamicQuery.WILDCARD;
		
		if (name.getLang() == null || name.getLang().length() == 0 ) {
			qry.appendGroupedAnd(new DynamicQuery.Parameter(nameTerm, nameValue, namePredicate));
		}
		else {
			String langValue = name.getLang().endsWith(DynamicQuery.WILDCARD)?name.getLang().toUpperCase():name.getLang().toUpperCase() + DynamicQuery.WILDCARD;
			qry.appendGroupedAnd(new DynamicQuery.Parameter(nameTerm, nameValue, namePredicate), 
								 new DynamicQuery.Parameter("upper(" + ENTITY_ALIAS + ".langCode)", langValue, DynamicQuery.PREDICATE_LIKE));
		}

		qry.closeParen().pad();
		
	}
	
}
