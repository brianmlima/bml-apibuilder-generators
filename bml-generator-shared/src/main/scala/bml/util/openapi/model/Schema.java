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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Accessors(fluent = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schema {
    private static final long serialVersionUID = 0L;
    @JsonProperty(value = "description", required = true)
    @Getter
    protected String description;

    @JsonProperty(value = "type", required = true)
    @Getter
    @Builder.Default
    protected Type type = Type.object;

    @JsonProperty(value = "properties", required = false)
    @Getter
    @Singular
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected Map<String, Property> properties;


    @JsonProperty(value = "enum", required = false)
    @Getter
    @Singular
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected List<String> enums = new LinkedList<>();


    @JsonProperty(value = "required", required = false)
    @Getter
    @Singular
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected List<String> requiredFields = new LinkedList<>();


}
