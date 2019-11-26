package fr.insee.rmes.api.classifications;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import fr.insee.rmes.api.MetadataApi;
import fr.insee.rmes.modeles.classification.Activite;
import fr.insee.rmes.modeles.classification.Activites;
import fr.insee.rmes.modeles.classification.cj.CategorieJuridique;
import fr.insee.rmes.modeles.classification.cj.CategorieJuridiqueNiveauII;
import fr.insee.rmes.modeles.classification.cj.CategorieJuridiqueNiveauIII;
import fr.insee.rmes.modeles.classification.cj.CategoriesJuridiques;
import fr.insee.rmes.modeles.classification.na1973.GroupeNA1973;
import fr.insee.rmes.modeles.classification.naf1993.ClasseNAF1993;
import fr.insee.rmes.modeles.classification.naf2003.ClasseNAF2003;
import fr.insee.rmes.modeles.classification.naf2008.ClasseNAF2008;
import fr.insee.rmes.modeles.classification.naf2008.SousClasseNAF2008;
import fr.insee.rmes.queries.classifications.ActivitesQueries;
import fr.insee.rmes.queries.classifications.CJQueries;
import fr.insee.rmes.queries.classifications.Na1973Queries;
import fr.insee.rmes.queries.classifications.Naf1993Queries;
import fr.insee.rmes.queries.classifications.Naf2003Queries;
import fr.insee.rmes.queries.classifications.Naf2008Queries;
import fr.insee.rmes.utils.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/codes")
@Tag(name = "nomenclatures", description = "Nomenclatures API")
public class CodesAPI extends MetadataApi {

    private static Logger logger = LogManager.getLogger(CodesAPI.class);

    @Path("/cj/n2/{code: [0-9]{2}}")
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(operationId = "getcjn2", summary = "Informations sur une catégorie juridique de niveau 2", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = CategorieJuridiqueNiveauII.class)))
    })
    public Response getCategorieJuridiqueNiveauII(
        @Parameter(
            required = true,
            description = "Identifiant de la catégorie juridique de niveau 2 (deux chiffres)") @PathParam("code") String code,
        @Parameter(hidden = true) @HeaderParam("Accept") String header) {
        logger.debug("Received GET request for CJ 2nd level " + code);

        CategorieJuridiqueNiveauII cjNiveau2 = new CategorieJuridiqueNiveauII(code);
        String csvResult = sparqlUtils.executeSparqlQuery(CJQueries.getCategorieJuridiqueNiveauII(code));
        cjNiveau2 = (CategorieJuridiqueNiveauII) csvUtils.populatePOJO(csvResult, cjNiveau2);

        if (cjNiveau2.getUri() == null) return Response.status(Status.NOT_FOUND).entity("").build();
        return Response.ok(responseUtils.produceResponse(cjNiveau2, header)).build();
    }

    @Path("/cj/n3/{code: [0-9]{4}}")
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(operationId = "getcjn3", summary = "Informations sur une catégorie juridique de niveau 3", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = CategorieJuridiqueNiveauIII.class)))
    })
    public Response getCategorieJuridiqueNiveauIII(
        @Parameter(
            required = true,
            description = "Identifiant de la catégorie juridique de niveau 3 (quatre chiffres)") @PathParam("code") String code,
        @Parameter(hidden = true) @HeaderParam("Accept") String header) {
        logger.debug("Received GET request for CJ 3rd level " + code);

        CategorieJuridiqueNiveauIII cjNiveau3 = new CategorieJuridiqueNiveauIII(code);
        String csvResult = sparqlUtils.executeSparqlQuery(CJQueries.getCategorieJuridiqueNiveauIII(code));
        cjNiveau3 = (CategorieJuridiqueNiveauIII) csvUtils.populatePOJO(csvResult, cjNiveau3);

        if (cjNiveau3.getUri() == null) return Response.status(Status.NOT_FOUND).entity("").build();
        return Response.ok(responseUtils.produceResponse(cjNiveau3, header)).build();
    }

    @Path("/cj")
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = "getcj",
        summary = "Informations sur une catégorie juridique identifiée par son code et une date",
        responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = CategoriesJuridiques.class)))
        })
    public Response getCategoriesJuridiques(
        @Parameter(required = true, description = "Code de la catégorie juridique") @QueryParam("code") String code,
        @Parameter(
            description = "Date à laquelle la catégorie juridique est valide (Format : 'AAAA-MM-JJ' ; '*' pour obtenir tout l'historique ; paramètre absent pour la date du jour)") @QueryParam("date") String date,
        @Parameter(hidden = true) @HeaderParam("Accept") String header) {

        logger.debug("Received GET request for CJ: code:" + code + " date:" + date);

        String csvResult = "";
        if (date == null)
            csvResult = sparqlUtils.executeSparqlQuery(CJQueries.getCJByCode(code));
        else if (date.equals("*"))
            csvResult = sparqlUtils.executeSparqlQuery(CJQueries.getCJ(code));
        else {
            if ( ! DateUtils.isValidDate(date)) return Response.status(Status.BAD_REQUEST).entity("").build();
            DateTime dt = DateUtils.getDateTimeFromDateString(date);
            csvResult = sparqlUtils.executeSparqlQuery(CJQueries.getCJByCodeAndDate(code, dt));
        }

        List<CategorieJuridique> cjList = csvUtils.populateMultiPOJO(csvResult, CategorieJuridique.class);

        // sub query return ,,,, result. So check list size and first element is not empty
        if (cjList.size() == 0 || cjList.get(0).getCode().equals(""))
            return Response.status(Status.NOT_FOUND).entity("").build();

        else if (header.equals(MediaType.APPLICATION_XML))
            return Response.ok(responseUtils.produceResponse(new CategoriesJuridiques(cjList), header)).build();

        else
            return Response.ok(responseUtils.produceResponse(cjList, header)).build();
    }

    @Path("/nafr2/sousClasse/{code: [0-9]{2}\\.[0-9]{2}[A-Z]}")
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = "getnafr2n5",
        summary = "Informations sur une sous-classe de la NAF rév.2 identifiée par son code",
        responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = SousClasseNAF2008.class)))
        })
    public Response getSousClasseNAF2008(
        @Parameter(
            required = true,
            description = "Code de la sous-classe (deux chiffres, un point, deux chiffres et une lettre majuscule)") @PathParam("code") String code,
        @Parameter(hidden = true) @HeaderParam("Accept") String header) {
        logger.debug("Received GET request for NAF sub-class " + code);

        SousClasseNAF2008 sousClasse = new SousClasseNAF2008(code);
        String csvResult = sparqlUtils.executeSparqlQuery(Naf2008Queries.getSousClasseNAF2008(code));
        sousClasse = (SousClasseNAF2008) csvUtils.populatePOJO(csvResult, sousClasse);

        if (sousClasse.getUri() == null) return Response.status(Status.NOT_FOUND).entity("").build();
        return Response.ok(responseUtils.produceResponse(sousClasse, header)).build();
    }

    @Path("/nafr2/classe/{code: [0-9]{2}\\.[0-9]{2}}")
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = "getnafr2n4",
        summary = "Informations sur une classe de la NAF rév.2 identifiée par son code",
        responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = ClasseNAF2008.class)))
        })
    public Response getClasseNAF2008(
        @Parameter(
            required = true,
            description = "Code de la classe (deux chiffres, un point, deux chiffres)") @PathParam("code") String code,
        @Parameter(hidden = true) @HeaderParam("Accept") String header) {

        logger.debug("Received GET request for NAF rev. 2 class " + code);

        ClasseNAF2008 classe = new ClasseNAF2008(code);
        String csvResult = sparqlUtils.executeSparqlQuery(Naf2008Queries.getClasseNAF2008(code));
        classe = (ClasseNAF2008) csvUtils.populatePOJO(csvResult, classe);

        if (classe.getUri() == null) return Response.status(Status.NOT_FOUND).entity("").build();
        return Response.ok(responseUtils.produceResponse(classe, header)).build();
    }

    @Path("/nafr1/classe/{code: [0-9]{2}\\.[0-9][A-Z]}")
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = "getnafr1n5",
        summary = "Informations sur une classe de la NAF rév.1 identifiée par son code",
        responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = ClasseNAF2003.class)))
        })
    public Response getClasseNAF2003(
        @Parameter(
            required = true,
            description = "Code de la classe (deux chiffres, un point, deux chiffres)") @PathParam("code") String code,
        @Parameter(hidden = true) @HeaderParam("Accept") String header) {

        logger.debug("Received GET request for NAF rev. 1 class " + code);

        ClasseNAF2003 classe = new ClasseNAF2003(code);
        String csvResult = sparqlUtils.executeSparqlQuery(Naf2003Queries.getClasseNAF2003(code));
        classe = (ClasseNAF2003) csvUtils.populatePOJO(csvResult, classe);

        if (classe.getUri() == null) return Response.status(Status.NOT_FOUND).entity("").build();
        return Response.ok(responseUtils.produceResponse(classe, header)).build();
    }

    @Path("/naf/classe/{code: [0-9]{2}\\.[0-9][A-Z]}")
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = "getnafn5",
        summary = "Informations sur une classe de la NAF identifiée par son code",
        responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = ClasseNAF1993.class)))
        })
    public Response getClasseNAF1993(
        @Parameter(
            required = true,
            description = "Code de la classe (deux chiffres, un point, deux chiffres)") @PathParam("code") String code,
        @Parameter(hidden = true) @HeaderParam("Accept") String header) {

        logger.debug("Received GET request for NAF class " + code);

        ClasseNAF1993 classe = new ClasseNAF1993(code);
        String csvResult = sparqlUtils.executeSparqlQuery(Naf1993Queries.getClasseNAF1993(code));
        classe = (ClasseNAF1993) csvUtils.populatePOJO(csvResult, classe);

        if (classe.getUri() == null) return Response.status(Status.NOT_FOUND).entity("").build();
        return Response.ok(responseUtils.produceResponse(classe, header)).build();
    }

    @Path("/na73/groupe/{code: [0-9]{2}\\.[0-9]{2}}")
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = "getna73n2",
        summary = "Informations sur un groupe de la NA 1973 identifié par son code",
        responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = GroupeNA1973.class)))
        })
    public Response getGroupeNA1973(
        @Parameter(
            required = true,
            description = "Code du groupe (deux chiffres, un point, deux chiffres)") @PathParam("code") String code,
        @Parameter(hidden = true) @HeaderParam("Accept") String header) {

        logger.debug("Received GET request for NA 1973 group " + code);

        GroupeNA1973 groupe = new GroupeNA1973(code);
        String csvResult = sparqlUtils.executeSparqlQuery(Na1973Queries.getGroupeNA1973(code));
        groupe = (GroupeNA1973) csvUtils.populatePOJO(csvResult, groupe);

        if (groupe.getUri() == null) return Response.status(Status.NOT_FOUND).entity("").build();
        return Response.ok(responseUtils.produceResponse(groupe, header)).build();
    }

    @Path("/activites")
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = "getactivities",
        summary = "Informations sur une activité identifiée par son code et une date",
        responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = Activites.class)))
        })
    public Response getActivities(
        @Parameter(required = true, description = "Code de l'activité") @QueryParam("code") String code,
        @Parameter(
            description = "Date à laquelle l'activité est valide (Format : 'AAAA-MM-JJ' ; '*' pour obtenir tout l'historique ; paramètre absent pour la date du jour)") @QueryParam("date") String date,
        @Parameter(hidden = true) @HeaderParam("Accept") String header) {

        logger.debug("Received GET request for Activities: " + code + " date:" + date);

        String csvResult = "";

        if (date == null)
            csvResult = sparqlUtils.executeSparqlQuery(ActivitesQueries.getActiviteByCode(code));
        else if (date.equals("*"))
            csvResult = sparqlUtils.executeSparqlQuery(ActivitesQueries.getActivites(code));
        else {
            if ( ! DateUtils.isValidDate(date)) return Response.status(Status.BAD_REQUEST).entity("").build();
            DateTime dt = DateUtils.getDateTimeFromDateString(date);
            csvResult = sparqlUtils.executeSparqlQuery(ActivitesQueries.getActiviteByCodeAndDate(code, dt));
        }

        List<Activite> activityList = csvUtils.populateMultiPOJO(csvResult, Activite.class);

        if (activityList.size() == 0)
            return Response.status(Status.NOT_FOUND).entity("").build();

        else if (header.equals(MediaType.APPLICATION_XML))
            return Response.ok(responseUtils.produceResponse(new Activites(activityList), header)).build();

        else
            return Response.ok(responseUtils.produceResponse(activityList, header)).build();
    }

}
