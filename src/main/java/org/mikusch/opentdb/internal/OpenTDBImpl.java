package org.mikusch.opentdb.internal;

import org.jetbrains.annotations.NotNull;
import org.mikusch.opentdb.api.OpenTDB;
import org.mikusch.opentdb.api.questions.Question;
import org.mikusch.opentdb.api.requests.EncodingType;
import org.mikusch.opentdb.api.requests.ErrorResponseException;
import org.mikusch.opentdb.api.requests.Request;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class OpenTDBImpl implements OpenTDB {

    private final Requester requester = new Requester(this);
    private final HttpClient httpClient;
    private final EncodingType encoding;
    private String token;

    public OpenTDBImpl(final HttpClient httpClient, final EncodingType encoding, final boolean useSessionToken) {
        Objects.requireNonNull(httpClient, "httpClient may not be null");
        Objects.requireNonNull(encoding, "encoding may not be null");

        this.httpClient = httpClient;
        this.encoding = encoding;
        if (useSessionToken) {
            requester.fetchToken();
        }
    }

    @NotNull
    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    @NotNull
    @Override
    public EncodingType getEncodingType() {
        return encoding;
    }

    @NotNull
    @Override
    public CompletableFuture<Void> resetToken() throws IllegalStateException {
        if (token == null) throw new IllegalStateException("Can't reset a null session token");
        return requester.resetToken();
    }

    @Override
    public OpenTDB awaitToken() throws InterruptedException {
        while (token == null) {
            Thread.sleep(50);
        }
        return this;
    }

    @Override
    public CompletableFuture<List<Question>> fetchQuestionsAsync(final int amount) {
        return requester.sendAsync(Request.newRequest(amount));
    }

    @Override
    public CompletableFuture<List<Question>> sendAsync(final Request request) {
        return requester.sendAsync(request);
    }

    @Override
    public List<Question> send(final Request request) throws ErrorResponseException {
        return requester.send(request);
    }

    public static class BuilderImpl implements Builder {

        private HttpClient httpClient = HttpClient.newHttpClient();
        private EncodingType encoding = EncodingType.HTML_CODES;
        private boolean useSessionToken = true;

        @Override
        public Builder setHttpClient(final HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        @Override
        public Builder setEncoding(final EncodingType encoding) {
            this.encoding = encoding;
            return this;
        }

        @Override
        public Builder useSessionToken(final boolean useSessionToken) {
            this.useSessionToken = useSessionToken;
            return this;
        }

        @Override
        public OpenTDB build() {
            return new OpenTDBImpl(httpClient, encoding, useSessionToken);
        }
    }
}