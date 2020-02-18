package bml.util.openapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Accessors(fluent = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Property {

    private static final long serialVersionUID = 0L;

    @JsonProperty(value = "description", required = true)
    @Getter
    @JsonInclude(JsonInclude.Include.ALWAYS)
    protected String description;

    @JsonProperty(value = "type", required = false)
    @Getter
    protected String type;

    @JsonProperty(value = "format", required = false)
    @Getter
    protected String format;

    @JsonProperty(value = "oneOf", required = false)
    @Getter
    @Singular("oneOf")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected List<Ref> oneOf;

}
