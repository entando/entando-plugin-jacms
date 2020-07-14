package org.entando.entando.plugins.jacms.web.contentsettings.model;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class ContentSettingsCropRatioRequest {

    @NotNull
    private String ratio;
}
