package org.entando.entando.plugins.jacms.web.resource.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateResourceRequest {
    private String correlationCode;
    private String type;
    private String group;
    private String folderPath;
    private List<String> categories;

    public String getCorrelationCode() {
        return correlationCode;
    }

    public void setCorrelationCode(String correlationCode) {
        this.correlationCode = correlationCode;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
