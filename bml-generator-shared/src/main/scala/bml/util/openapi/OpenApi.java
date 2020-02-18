package bml.util.openapi;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Map;
import java.util.UUID;

@Accessors(fluent = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApi {

    private static final long serialVersionUID = 0L;


    @JsonProperty( required = true)
    @Getter
    @Builder.Default
    protected String openapi = "3.0.2";

    @JsonProperty(required = true)
    @Getter
    protected Info info;

    @JsonProperty(required = true)
    @Getter
    protected Components components;


    @JsonProperty(required = true)
    @Getter
    @Builder.Default
    protected Object paths=new Object();


}






