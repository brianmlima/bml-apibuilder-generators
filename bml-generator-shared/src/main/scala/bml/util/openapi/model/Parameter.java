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

@Accessors(fluent = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class Parameter {

    private static final long serialVersionUID = 0L;

    @JsonProperty(required = true)
    @Getter
    protected String name;

    @JsonProperty(required = true)
    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected String description;

    @JsonProperty(required = true)
    @Getter
    @Builder.Default
    protected boolean required = true;

    @JsonProperty(required = true)
    @Getter
    @Builder.Default
    protected boolean deprecated = false;

    @JsonProperty(required = true)
    @Getter
    @Builder.Default
    protected In in = In.query;

    @JsonProperty(required = false)
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected ParamSchema schema;

    @JsonProperty(value = "default", required = false)
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Object defaultValue;


}
