SELECT DISTINCT ?uri ?type ?code ?typeArticle ?intitule ?intituleSansArticle ?dateCreation ?dateSuppression ?chefLieu
FROM <http://rdf.insee.fr/graphes/geo/cog>

WHERE {
			?origine a igeo:${typeOrigine} ;
			       igeo:codeINSEE '${code}' .
			 BIND('${typeOrigine}' as ?type)       
			 
			 OPTIONAL{
				?evenementCreationOrigine igeo:creation ?origine ;
					                  	igeo:date ?dateCreationOrigine.
			}
			OPTIONAL{
				?evenementSuppressionOrigine igeo:suppression ?origine ;
					                  igeo:date ?dateSuppressionOrigine.
			}
			
			FILTER(!BOUND(?dateCreationOrigine) || ?dateCreationOrigine <= '${date}'^^xsd:date) 
			FILTER(!BOUND(?dateSuppressionOrigine) || ?dateSuppressionOrigine > '${date}'^^xsd:date)


			BIND(IF (
				(!BOUND(?dateCreationOrigine) || ?dateCreationOrigine <= '${dateProjection}'^^xsd:date)
				&& (!BOUND(?dateSuppressionOrigine) || ?dateSuppressionOrigine > '${dateProjection}'^^xsd:date),
			?origine,
			"0"
		) AS ?origineProj)
			   
<#if previous = "true">
		OPTIONAL{          		
				?origine (^igeo:creation/igeo:suppression)+ ?uriProj .
		}

<#else>
		OPTIONAL{          		
				?origine (^igeo:suppression/igeo:creation)+ ?uriProj .
		}

</#if>				

		BIND(IF((?origineProj != "0"),
				?origine, 
				IF(!BOUND(?uriProj),"no response",?uriProj)) 
				AS ?uri).

			?uri a igeo:${typeOrigine} ;
					igeo:codeINSEE ?code ;
					igeo:codeArticle ?typeArticle;
					igeo:nom ?intitule;
					igeo:nomSansArticle ?intituleSansArticle .
			OPTIONAL {
				?evenementCreation igeo:creation ?uri ;
					               igeo:date ?dateCreation .
			}
			OPTIONAL {
				?evenementSuppression igeo:suppression ?uri ;
					                  igeo:date ?dateSuppression.
			}
			
			OPTIONAL {
			 	?uri ((igeo:sousPrefecture|igeo:prefecture|igeo:prefectureDeRegion)/igeo:codeINSEE) ?chefLieu.
			}
			
			FILTER(!BOUND(?dateCreation) || ?dateCreation <= '${dateProjection}'^^xsd:date)
			FILTER(!BOUND(?dateSuppression) || ?dateSuppression > '${dateProjection}'^^xsd:date)
				   
		
}
ORDER BY  ?code