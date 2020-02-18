package bml.util.openapi;

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
public class Info {

    private static final long serialVersionUID = 0L;

    ////////////////////////////////////////////////
    ////////////////////////////////////////////////
    @JsonProperty(required = true)
    @Getter
    protected Contact contact;
    @JsonProperty(required = true)
    @Getter
    protected License license;
    ////////////////////////////////////////////////
//    @JsonProperty(required = true)
//    @Getter
//    @Singular
//    protected List<Servers> servers;
    ////////////////////////////////////////////////
    @JsonProperty(required = true)
    @Getter
    protected String termsOfService;
    ////////////////////////////////////////////////
    @JsonProperty(required = true)
    @Getter
    protected String title;
    ////////////////////////////////////////////////
    @JsonProperty(required = true)
    @Getter
    protected String version;
    ////////////////////////////////////////////////
    ////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Accessors(fluent = true)
    @Builder
//    @AllArgsConstructor
    @NoArgsConstructor
//    @FieldNameConstants
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Accessors(fluent = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldNameConstants
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class License {

        @JsonProperty( required = true)
        @Getter
        @Builder.Default
        @JsonInclude(JsonInclude.Include.ALWAYS)
        protected String name = "";


    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    @Accessors(fluent = true)
//    @Builder
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @FieldNameConstants
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class Servers {
//
//        @JsonProperty( required = true)
//        @Getter
//        @Builder.Default
//        @JsonInclude(JsonInclude.Include.ALWAYS)
//        protected String url = "";
//    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////





}
