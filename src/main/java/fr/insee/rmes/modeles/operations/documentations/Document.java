package fr.insee.rmes.modeles.operations.documentations;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import fr.insee.rmes.config.Configuration;
import fr.insee.rmes.modeles.StringWithLang;
import fr.insee.rmes.utils.Lang;

@JsonClassDescription("Objet représentant un document ou un lien vers une page internet")
public class Document {
    private List<StringWithLang> label = new ArrayList<>();
    private String dateMiseAJour;
    private String langue;
    private String url;

    @JacksonXmlProperty(localName = "label")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<StringWithLang> getLabel() {
        return label;
    }

    public void setLabelLg1(String labelLg1) {
        label.add(new StringWithLang(labelLg1, Lang.FR));
    }

    public void setLabelLg2(String labelLg2) {
        if (StringUtils.isNotEmpty(labelLg2)) {
            label.add(new StringWithLang(labelLg2, Lang.EN));
        }
    }

    public String getDateMiseAJour() {
        return dateMiseAJour;
    }

    public void setDateMiseAJour(String dateMiseAJour) {
        this.dateMiseAJour = dateMiseAJour;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getUrl() {
        if (url != null && url.contains(Configuration.getFileStorage())) {
            String[] temp = url.split(Configuration.getFileStorage());
            return temp[temp.length - 1];
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}