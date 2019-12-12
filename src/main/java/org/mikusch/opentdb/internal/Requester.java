package org.mikusch.opentdb.internal;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mikusch.opentdb.api.OpenTDB;
import org.mikusch.opentdb.api.questions.BooleanQuestion;
import org.mikusch.opentdb.api.questions.MultipleChoiceQuestion;
import org.mikusch.opentdb.api.questions.Question;
import org.mikusch.opentdb.api.requests.ErrorResponseException;
import org.mikusch.opentdb.api.requests.Request;
import org.mikusch.opentdb.api.requests.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Internal class used to send HTTP requests to the OpenTDB API.
 */
public class Requester {

    private static final Logger LOGGER = LoggerFactory.getLogger(Requester.class);

    private static final String QUESTION_ENDPOINT = OpenTDB.BASE_URL + "/api.php";
    private static final String TOKEN_ENDPOINT = OpenTDB.BASE_URL + "/api_token.php";

    private static final String QUERY_PARAM_AMOUNT = "amount";
    private static final String QUERY_PARAM_TOKEN = "token";

    private final OpenTDBImpl api;
    private Instant tokenInitTime;

    public Requester(final OpenTDBImpl api) {
        this.api = api;
    }

    public CompletableFuture<List<Question<?, ?>>> sendAsync(final Request request) {
        return api.getHttpClient().sendAsync(this.buildHttpRequest(request), HttpResponse.BodyHandlers.ofString()).thenApplyAsync(this::handleResponse);
    }

    private HttpRequest buildHttpRequest(final Request request) {
        final StringBuilder uri = new StringBuilder(QUESTION_ENDPOINT)
                .append("?").append(api.getEncodingType().getParameterName()).append("=").append(api.getEncodingType().getParameterValue())
                .append("&" + QUERY_PARAM_AMOUNT + "=").append(request.getAmount());

        if (request.getCategory() != null)
            uri.append("&").append(request.getCategory().getParameterName()).append("=").append(request.getCategory().getParameterValue());

        if (request.getType() != null)
            uri.append("&").append(request.getType().getParameterName()).append("=").append(request.getType().getParameterValue());

        if (request.getDifficulty() != null)
            uri.append("&").append(request.getDifficulty().getParameterName()).append("=").append(request.getDifficulty().getParameterValue());

        // token has been disabled or hasnt been fetched yet
        if (api.getToken() != null) uri.append("&" + QUERY_PARAM_TOKEN + "=").append(api.getToken());
        return HttpRequest.newBuilder(URI.create(uri.toString())).build();
    }

    private List<Question<?, ?>> handleResponse(final HttpResponse<String> httpResponse) throws ErrorResponseException {
        final JSONObject body = new JSONObject(httpResponse.body());
        final ResponseCode response = ResponseCode.fromCode(body.getInt("response_code"));
        switch (response) {
            case SUCCESS:
                return this.getQuestionsFromJson(body);
            case TOKEN_NOT_FOUND:
                if (tokenInitTime != null && this.isTokenExpired()) {
                    throw new ErrorResponseException(response, "Session Token has been invalidated after 6 hours of inactivity");
                }
            default:
                throw new ErrorResponseException(response);
        }
    }

    private List<Question<?, ?>> getQuestionsFromJson(final JSONObject body) {
        final List<Question<?, ?>> questions = new ArrayList<>();
        final JSONArray jsonArray = body.getJSONArray("results");
        jsonArray.forEach(element -> {
            final JSONObject jsonElement = (JSONObject) element;
            final Question.Type type = Question.Type.valueOf(jsonElement.getString("type").toUpperCase());

            final Question<?, ?> question;
            switch (type) {

                case BOOLEAN:
                    question = new BooleanQuestion(
                            Question.Category.fromName(jsonElement.getString("category")),
                            Question.Difficulty.valueOf(jsonElement.getString("difficulty").toUpperCase()),
                            jsonElement.getString("question"),
                            jsonElement.getBoolean("correct_answer"),
                            jsonElement.getJSONArray("incorrect_answers").toList().stream().map(a -> Boolean.valueOf((String) a)).findFirst().orElse(null)
                    );
                    break;

                default:
                case MULTIPLE:
                    question = new MultipleChoiceQuestion(
                            Question.Category.fromName(jsonElement.getString("category")),
                            Question.Difficulty.valueOf(jsonElement.getString("difficulty").toUpperCase()),
                            jsonElement.getString("question"),
                            jsonElement.getString("correct_answer"),
                            jsonElement.getJSONArray("incorrect_answers").toList().stream().map(a -> ((String) a)).collect(Collectors.toList())
                    );
                    break;
            }

            questions.add(question);
            LOGGER.debug("Fetched question {}", question);
        });
        return questions;
    }

    private boolean isTokenExpired() {
        return Duration.between(tokenInitTime, Instant.now()).compareTo(Duration.ofHours(6)) > 0;
    }

    public CompletableFuture<Void> resetToken() {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(TOKEN_ENDPOINT + "?command=reset&token=" + api.getToken())).build();
        return api.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAcceptAsync(response -> {
            LOGGER.info("Session token has been reset");
            tokenInitTime = Instant.now();
        });
    }

    public void fetchToken() {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(TOKEN_ENDPOINT + "?command=request")).build();
        api.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAcceptAsync(this::initTokenFromResponse);
    }

    private void initTokenFromResponse(final HttpResponse<String> response) {
        final JSONObject body = new JSONObject(response.body());
        final ResponseCode responseCode = ResponseCode.fromCode(body.getInt("response_code"));

        if (responseCode == ResponseCode.SUCCESS) {
            LOGGER.debug("Initializing session token");
            tokenInitTime = Instant.now();
            api.setToken(body.getString("token"));
        } else {
            throw new AssertionError("Somehow the API refused to generate a token for us, wtf?");
        }
    }

    public List<Question<?, ?>> send(final Request request) throws ErrorResponseException {
        try {
            return this.handleResponse(api.getHttpClient().send(this.buildHttpRequest(request), HttpResponse.BodyHandlers.ofString()));
        } catch (final IOException | InterruptedException e) {
            throw new ErrorResponseException(ResponseCode.UNKNOWN, e.getMessage());
        }
    }
}
