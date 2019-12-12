package org.mikusch.opentdb.api.requests;

import org.mikusch.opentdb.api.OpenTDB;
import org.mikusch.opentdb.api.questions.Question;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The class modeling a request to the OpenTDB API.
 * <p>
 * Requests can be built using the {@link Request.Builder} class.
 *
 * @see OpenTDB#sendAsync(Request)
 * @see #sendAsync(OpenTDB)
 */
public class Request {

    private final int amount;
    private final Question.Category category;
    private final Question.Type type;
    private final Question.Difficulty difficulty;

    protected Request(final int amount, final Question.Category category, final Question.Type type, final Question.Difficulty difficulty) {
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.difficulty = difficulty;
    }

    /**
     * Creates a new {@code Request} with the specified amount and default parameters.<br/>
     * Equivalent to {@code Request#newBuilder().build()}.
     * <p/>
     * <b>Note:</b> While an {@code amount} of over 50 will <u>not</u> throw an {@link IllegalArgumentException}, the API will not return more than 50 questions at a time.
     *
     * @param amount the amount of questions, maximum 50
     * @return a new {@code Request}
     * @throws IllegalArgumentException if the amount is zero or negative
     */
    @Nonnull
    public static Request newRequest(final int amount) throws IllegalArgumentException {
        return newBuilder(amount).build();
    }

    /**
     * Creates a new {@link Request.Builder} with the specified amount of questions.
     * <p/>
     * <b>Note:</b> While an {@code amount} of over 50 will <u>not</u> throw an {@link IllegalArgumentException}, the API will not return more than 50 questions at a time.
     *
     * @param amount the amount of questions, maximum 50
     * @return a {@link Request.Builder}
     * @throws IllegalArgumentException if the amount is zero or negative
     */
    @Nonnull
    public static Builder newBuilder(final int amount) throws IllegalArgumentException {
        return new Builder(amount);
    }

    /**
     * Submits this request using the requester of the provided {@link OpenTDB} object and blocks the thread until its completion.
     *
     * @param api the {@link OpenTDB} object
     * @return a {@code List<Question>}
     * @throws IOException          if an I/O error occurs when sending
     * @throws InterruptedException if the operation is interrupted
     */
    @Nonnull
    public List<Question<?, ?>> send(final OpenTDB api) throws IOException, InterruptedException {
        return api.send(this);
    }

    /**
     * The amount of questions in this request.
     *
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * The category of questions in this request.
     *
     * @return the {@link Question.Category}
     */
    @Nullable
    public Question.Category getCategory() {
        return category;
    }

    /**
     * The type of questions in this request.
     *
     * @return the {@link Question.Type}
     */
    @Nullable
    public Question.Type getType() {
        return type;
    }

    /**
     * The difficulty of questions in this request.
     *
     * @return the {@link Question.Difficulty}
     */
    @Nullable
    public Question.Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Submits this request to the requester of the provided {@link OpenTDB} object and provides a {@link CompletableFuture} representing its completion task.
     *
     * @param api the {@link OpenTDB} object
     * @return a {@link CompletableFuture} task representing the completion promise
     */
    @Nonnull
    public CompletableFuture<List<Question<?, ?>>> sendAsync(final OpenTDB api) {
        return api.sendAsync(this);
    }

    public static class Builder {

        private final int amount;
        private Question.Category category;
        private Question.Type type;
        private Question.Difficulty difficulty;

        /**
         * Creates a new {@link Request.Builder}.
         *
         * <b>Note:</b> While an {@code amount} of over 50 will <u>not</u> throw an {@link IllegalArgumentException}, the API will not return more than 50 questions at a time.
         *
         * @param amount the amount of questions, maximum 50
         * @throws IllegalArgumentException if the amount is zero or negative
         */
        @Nonnull
        public Builder(final int amount) throws IllegalArgumentException {
            if (amount <= 0) throw new IllegalArgumentException("Can't create a request with an amount of 0 or less");
            this.amount = amount;
        }

        /**
         * The {@link Question.Category Category} to fetch questions from.
         *
         * @param category the category
         * @return the current {@link Builder} instance
         * @see Question.Category#fromId(int)
         * @see Question.Category#fromName(String)
         */
        @Nonnull
        public Builder fromCategory(@Nullable final Question.Category category) {
            this.category = category;
            return this;
        }

        /**
         * The {@link Question.Type Type} of questions to be fetched.
         *
         * @param type {@link Question.Type#MULTIPLE Multiple Choice} or {@link Question.Type#BOOLEAN True/False}
         * @return the current {@link Builder} instance
         */
        @Nonnull
        public Builder ofType(@Nullable final Question.Type type) {
            this.type = type;
            return this;
        }

        /**
         * The {@link Question.Difficulty Difficulty} of questions to be fetched.
         *
         * @param difficulty {@link Question.Difficulty#EASY Easy}, {@link Question.Difficulty#MEDIUM Medium} or {@link Question.Difficulty#HARD Hard}
         * @return the current {@link Builder} instance
         */
        @Nonnull
        public Builder ofDifficulty(@Nullable final Question.Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        /**
         * Builds this {@link Request}.
         *
         * @return a new {@link Request}
         */
        @Nonnull
        public Request build() {
            return new Request(amount, category, type, difficulty);
        }
    }
}
