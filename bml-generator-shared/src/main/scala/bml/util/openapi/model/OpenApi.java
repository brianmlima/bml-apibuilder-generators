package bml.util.openapi.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OpenApi {

    private static final long serialVersionUID = 0L;

    @JsonProperty(required = true)
    @Getter
    @Builder.Default
    protected String openapi = "3.0.2";


    @JsonProperty(required = true)
    @Getter
    protected Info info;

    @JsonProperty(required = false)
    @Getter
    @Singular
    protected List<Server> servers;

    @JsonProperty(required = false)
    @Getter
    @Singular
    protected List<Tag> tags;


    @JsonProperty(required = true)
    @Getter
    protected Components components;


    @JsonProperty(required = true)
    @Getter
    @Singular
    protected Map<String, Object> paths;


}






