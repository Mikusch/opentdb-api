package org.mikusch.opentdb.api.requests;

public enum EncodingType implements RequestParameter {

    HTML_CODES("", "Default Encoding (HTML Codes)"),
    LEGACY_URL("urlLegacy", "Legacy URL Encoding"),
    RFC_3986("url3986", "URL Encoding (RFC 3986)"),
    BASE_64("base64", "Base64 Encoding");

    private final String code;
    private final String name;

    EncodingType(final String code, final String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getParameterName() {
        return "encode";
    }

    @Override
    public String getParameterValue() {
        return code;
    }

    public String getReadableName() {
        return name;
    }
}
