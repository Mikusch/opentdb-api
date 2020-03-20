package org.mikusch.opentdb.api.requests;

import javax.annotation.Nonnull;

/**
 * Indicates a request parameter used in HTTP requests to the OpenTDB API.
 */
public interface RequestParameter
{
    /**
     * The name of the HTTP request parameter.
     *
     * @return the parameter name
     */
    @Nonnull
    String getParameterName();

    /**
     * The value of the HTTP request parameter.
     *
     * @return the parameter value
     */
    @Nonnull
    String getParameterValue();
}
