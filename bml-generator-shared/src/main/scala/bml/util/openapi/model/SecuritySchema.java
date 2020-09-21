package bml.util.openapi.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.List;
import java.util.Map;

@Accessors(fluent = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecuritySchema {
    private static final long serialVersionUID = 0L;

    //    @JsonProperty(required = false)
    //    @Getter
    //    private Map<String,Object> name;

    @JsonProperty(required = false)
    @Getter
    private String type;

    //    @JsonProperty(required = false)
    //    @Getter
    //    private String description;

    @JsonProperty(required = false)
    @Getter
    private Map<String, Object> flows;


}
