package org.mikusch.opentdb.api.requests;

/**
 * Indicates an error that is returned by a OpenTDB API Request.<br>
 * It holds a {@link ResponseCode ResponseCode} whose {@link ResponseCode#isError()} method is guaranteed to return {@code true}.
 */
public class ErrorResponseException extends RuntimeException {

    private static final long serialVersionUID = 3647944889441414747L;
    private final ResponseCode responseCode;

    public ErrorResponseException(final ResponseCode responseCode) {
        this(responseCode, responseCode.getCode() + ": " + responseCode.getMeaning());
    }

    public ErrorResponseException(final ResponseCode responseCode, final String message) {
        super(message);
        if (!responseCode.isError()) {
            throw new IllegalArgumentException("Constructing an ErrorResponseException with a non-error ResponseCode is forbidden");
        }
        this.responseCode = responseCode;
    }

    /**
     * Returns the {@link ResponseCode} corresponding to this error response.
     *
     * @return the {@link ResponseCode}
     */
    public ResponseCode getResponseCode() {
        return responseCode;
    }

    /**
     * The code of the {@link ResponseCode} corresponding to this error response.<br>
     *
     * @return the error code
     */
    public int getErrorCode() {
        return responseCode.getCode();
    }

    /**
     * The meaning of the {@link ResponseCode} corresponding to this error response.<br>
     *
     * @return the meaning
     */
    public String getMeaning() {
        return responseCode.getMeaning();
    }
}
