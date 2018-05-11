package fr.insee.rmes.api.codes.cj;

import fr.insee.rmes.api.Configuration;

public class CJQueries {

	public static String getCategorieJuridiqueNiveauIII(String code) {
		return "SELECT ?uri ?intitule WHERE { \n"
				+ "FILTER(STRENDS(STR(?uri), '" + code + "')) \n"
				+ "?lastCJThirdLevel skos:member ?uri . \n"
				+ "?uri skos:prefLabel ?intitule  \n"
				+ "FILTER (lang(?intitule) = 'fr') \n"
				+ "{ \n"
				+ "SELECT ?lastCJThirdLevel WHERE { \n"
				+ "?lastCJThirdLevel xkos:organizedBy <" + Configuration.BASE_HOST + "/concepts/cj/cjNiveauIII> . \n"
				+ "BIND(STRBEFORE(STRAFTER(STR(?lastCJThirdLevel), '" + Configuration.BASE_HOST + "/codes/cj/cj'), '/niveauIII') AS ?lastCJVersion) \n"
				+ "BIND(xsd:float(?lastCJVersion) AS ?lastCJVersionFloat)"
				+ "} \n"
				+ "ORDER BY DESC (?lastCJVersionFloat) \n"
				+ "LIMIT 1 \n"
				+ "} \n"
				+ "}";
	}	

}
