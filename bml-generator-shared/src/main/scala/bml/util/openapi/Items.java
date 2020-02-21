package bml.util.openapi;

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
public class Items {


    private static final long serialVersionUID = 0L;

    ////////////////////////////////////////////////
    ////////////////////////////////////////////////
    @JsonProperty(required = false)
    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected String type;

    @JsonProperty(value = "$ref", required = false)
    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected String ref;


}
