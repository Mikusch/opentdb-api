package org.mikusch.opentdb.internal;

import org.mikusch.opentdb.api.OpenTDB;
import org.mikusch.opentdb.api.questions.Question;
import org.mikusch.opentdb.api.requests.EncodingType;
import org.mikusch.opentdb.api.requests.ErrorResponseException;
import org.mikusch.opentdb.api.requests.Request;

import javax.annotation.Nonnull;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class OpenTDBImpl implements OpenTDB
{
	private final Requester requester = new Requester(this);
	private final HttpClient httpClient;
	private final EncodingType encoding;
	private String token;

	public OpenTDBImpl(final HttpClient httpClient, final EncodingType encoding, final boolean useSessionToken)
	{
		Objects.requireNonNull(httpClient, "httpClient may not be null");
		Objects.requireNonNull(encoding, "encoding may not be null");

		this.httpClient = httpClient;
		this.encoding = encoding;
		if (useSessionToken)
		{
			requester.fetchToken();
		}

		// Fetch all categories with provided HttpClient
		Question.Category.loadCategories(httpClient);
	}

	@Nonnull
	@Override
	public HttpClient getHttpClient()
	{
		return httpClient;
	}

	@Override
	public String getToken()
	{
		return token;
	}

	public void setToken(final String token)
	{
		this.token = token;
	}

	@Nonnull
	@Override
	public EncodingType getEncodingType()
	{
		return encoding;
	}

	@Nonnull
	@Override
	public CompletableFuture<Void> resetToken()
	{
		if (token == null) throw new IllegalStateException("Can't reset a null session token");
		return requester.resetToken();
	}

	@Nonnull
	@Override
	public OpenTDB awaitToken() throws InterruptedException
	{
		while (token == null)
		{
			Thread.sleep(50);
		}
		return this;
	}

	@Nonnull
	@Override
	public CompletableFuture<List<Question<?, ?>>> fetchQuestionsAsync(final int amount)
	{
		return requester.sendAsync(Request.newRequest(amount));
	}

	@Nonnull
	@Override
	public CompletableFuture<List<Question<?, ?>>> sendAsync(final Request request)
	{
		return requester.sendAsync(request);
	}

	@Nonnull
	@Override
	public List<Question<?, ?>> send(final Request request) throws ErrorResponseException
	{
		return requester.send(request);
	}

	public static class BuilderImpl implements Builder
	{

		private HttpClient httpClient = HttpClient.newHttpClient();
		private EncodingType encoding = EncodingType.HTML_CODES;
		private boolean useSessionToken = true;

		@Nonnull
		@Override
		public Builder setHttpClient(final HttpClient httpClient)
		{
			this.httpClient = httpClient;
			return this;
		}

		@Nonnull
		@Override
		public Builder setEncoding(final EncodingType encoding)
		{
			this.encoding = encoding;
			return this;
		}

		@Nonnull
		@Override
		public Builder useSessionToken(final boolean useSessionToken)
		{
			this.useSessionToken = useSessionToken;
			return this;
		}

		@Nonnull
		@Override
		public OpenTDB build()
		{
			return new OpenTDBImpl(httpClient, encoding, useSessionToken);
		}
	}
}