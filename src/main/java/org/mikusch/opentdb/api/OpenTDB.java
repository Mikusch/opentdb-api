package org.mikusch.opentdb.api;

import org.mikusch.opentdb.api.questions.Question;
import org.mikusch.opentdb.api.requests.EncodingType;
import org.mikusch.opentdb.api.requests.ErrorResponseException;
import org.mikusch.opentdb.api.requests.Request;
import org.mikusch.opentdb.internal.OpenTDBImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The core of OpenTDB. All features of the API can be accessed using this class.
 */
public interface OpenTDB
{
	String BASE_URL = "https://opentdb.com";

	/**
	 * Returns a new {@code OpenTDB} instance with default settings.<br>
	 * Equivalent to {@link OpenTDB#newBuilder().build()}.
	 *
	 * @return a new {@code OpenTDB} instance
	 */
	@Nonnull
	static OpenTDB newOpenTDB()
	{
		return newBuilder().build();
	}

	/**
	 * Creates a new {@code OpenTDB} builder.
	 *
	 * @return an {@code OpenTDB.Builder}
	 */
	@Nonnull
	static Builder newBuilder()
	{
		return new OpenTDBImpl.BuilderImpl();
	}

	/**
	 * The {@link HttpClient} used to send requests to the API.
	 *
	 * @return an {@link HttpClient}
	 */
	@Nonnull
	HttpClient getHttpClient();

	/**
	 * Returns the Session Token of the current OpenTDB instance.
	 * <br>
	 * This method will return {@code null} if the use of a session token was explicitly disabled,
	 * or if this method was called before {@link #awaitToken()} finished.
	 *
	 * @return the possibly-null session token
	 * @see #awaitToken()
	 */
	@Nullable
	String getToken();

	/**
	 * Returns the Encoding Type of the current OpenTDB
	 * instance used for questions and answers.
	 *
	 * @return the encoding type
	 */
	@Nonnull
	EncodingType getEncodingType();

	/**
	 * Submits a request for resetting the session token and provides a {@link CompletableFuture} representing its completion task.
	 *
	 * @return never-null {@link CompletableFuture} task representing the completion promise
	 * @throws IllegalStateException if the session token is {@code null}
	 */
	@Nonnull
	CompletableFuture<Void> resetToken();

	/**
	 * This method will block until a session token has been successfully retrieved.
	 *
	 * @return the current OpenTDB instance, for chaining convenience
	 * @throws InterruptedException if this thread is interrupted while waiting
	 */
	@Nonnull
	OpenTDB awaitToken() throws InterruptedException;

	/**
	 * Fetches a certain amount of questions without any parameters.
	 *
	 * @param amount the amount of questions
	 * @return a {@link CompletableFuture} task representing the completion promise
	 */
	@Nonnull
	CompletableFuture<List<Question<?, ?>>> fetchQuestionsAsync(int amount);

	/**
	 * Submits the passed {@link Request} and provides a {@link CompletableFuture} containing a {@code List<Question>} representing its completion task.
	 *
	 * @param request the {@link Request} to send
	 * @return a {@link CompletableFuture} task representing the completion promise
	 */
	@Nonnull
	CompletableFuture<List<Question<?, ?>>> sendAsync(Request request);

	/**
	 * Submits the passed {@link Request} and blocks the thread until its completion.
	 *
	 * @param request the {@link Request} to send
	 * @return a {@code List<Question>}
	 * @throws ErrorResponseException if the request failed to send
	 */
	@Nonnull
	List<Question<?, ?>> send(Request request) throws ErrorResponseException;

	interface Builder
	{
		/**
		 * The {@link HttpClient} that should be used when making requests.
		 *
		 * @param httpClient the {@link HttpClient}
		 * @return the current {@code OpenTDB.Builder} instance for chaining
		 */
		@Nonnull
		Builder setHttpClient(HttpClient httpClient);

		/**
		 * The {@link EncodingType} to use for all responses, {@link EncodingType#HTML_CODES} by default.
		 * <p/>
		 * This determines the value of the {@code encode} parameter.
		 *
		 * @param encoding the {@link EncodingType}
		 * @return the current {@code OpenTDB.Builder} instance for chaining
		 */
		@Nonnull
		Builder setEncoding(EncodingType encoding);

		/**
		 * Enables or disables the use of a Session Token, {@code true} by default.
		 * <p/>
		 * Session Tokens are unique keys that will help keep track of the questions the API has already retrieved.<br>
		 * By using a Session Token, the API will never give you the same question twice.
		 * Over the lifespan of a Session Token, it will eventually reach a point where you have exhausted
		 * all the possible questions in the database.<br>
		 * At this point, you can either call {@link OpenTDB#resetToken()}, which will wipe all past memory,
		 * or you can construct a new {@link OpenTDB} instance.
		 *
		 * @return the current {@code OpenTDB.Builder} instance for chaining
		 */
		@Nonnull
		Builder useSessionToken(boolean useSessionToken);

		/**
		 * Builds the {@link OpenTDB} instance.
		 * <p/>
		 * If {@link #useSessionToken} is {@code true}, the OpenTDB object will start retrieving a session token after this method returns.
		 * You may want to call {@link OpenTDB#awaitToken()} to ensure the token was successfully initialized before sending requests.
		 *
		 * @return the {@link OpenTDB} instance
		 * @see #awaitToken()
		 */
		@Nonnull
		OpenTDB build();
	}
}
