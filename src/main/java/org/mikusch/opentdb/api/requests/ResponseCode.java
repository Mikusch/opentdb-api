package org.mikusch.opentdb.api.requests;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Enum representing the various types of responses the OpenTDB API may provide.
 * <p/>
 * For responses that only occur if something went wrong, {@link #isError()} will return {@code true}.
 */
public enum ResponseCode {

    /**
     * Unknown response.
     */
    UNKNOWN(-1, "Unknown Response Code"),

    /**
     * Returned results successfully.
     */
    SUCCESS(0, "Success"),

    /**
     * Could not return results.<br/>
     * The API doesn't have enough questions for your query (e.g. asking for 50 questions in a category that only has 20).
     */
    NO_RESULTS(1, "No Results", true),

    /**
     * Contains an invalid parameter.
     * <p/>
     * This should <u>never</u> happen unless there are breaking API changes.
     */
    INVALID_PARAMETER(2, "Invalid Parameter", true),

    /**
     * Session Token does not exist.
     * <p/>
     * This most commonly occurs when OpenTDB invalidates the token after 6 hours of inactivity.
     */
    TOKEN_NOT_FOUND(3, "Token Not Found", true),

    /**
     * Session Token has returned all possible questions for the specified query.<br>
     * Resetting the Token is necessary.
     */
    TOKEN_EMPTY(4, "Token Empty", true);

    private final int code;
    private final String meaning;
    private final boolean isError;

    ResponseCode(final int code, final String meaning) {
        this(code, meaning, false);
    }

    ResponseCode(final int code, final String meaning, final boolean isError) {
        this.code = code;
        this.meaning = meaning;
        this.isError = isError;
    }

    /**
     * Utility method to fetch a {@code ResponseCode} from its code.
     *
     * @param code the code
     * @return the {@code ResponseCode}, or {@link ResponseCode#UNKNOWN} if none could be found
     */
    @Nonnull
    public static ResponseCode fromCode(final int code) {
        return Arrays.stream(ResponseCode.values()).filter(responseCode -> responseCode.code == code).findFirst().orElse(UNKNOWN);
    }

    /**
     * The code returned by the API.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * The meaning of this response code.
     *
     * @return the meaning
     */
    @Nonnull
    public String getMeaning() {
        return meaning;
    }

    /**
     * Whether or not the response code indicates an error.
     *
     * @return {@code true} if the response code indicates an error
     */
    public boolean isError() {
        return isError;
    }
}
