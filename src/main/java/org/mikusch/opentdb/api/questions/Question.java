package org.mikusch.opentdb.api.questions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.mikusch.opentdb.api.OpenTDB;
import org.mikusch.opentdb.api.requests.RequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Abstract class representing a question received from the OpenTDB API.
 *
 * @param <C> the correct answer type
 * @param <I> the incorrect answer type
 * @see MultipleChoiceQuestion
 * @see BooleanQuestion
 */
public abstract class Question<C, I> {

    private final Category category;
    private final Difficulty difficulty;
    private final String question;
    private final C correctAnswer;
    private final I incorrectAnswers;

    protected Question(final Category category, final Difficulty difficulty, final String question, final C correctAnswer, final I incorrectAnswers) {
        this.category = category;
        this.difficulty = difficulty;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
    }

    /**
     * The {@link Category} this question is in.
     *
     * @return the {@link Category}
     */
    public Category getCategory() {
        return category;
    }

    /**
     * The {@link Difficulty} of this question.
     *
     * @return the {@link Difficulty}
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * The text of this question.
     *
     * @return the question
     */
    public String getQuestion() {
        return question;
    }

    /**
     * The correct answer of this question.
     *
     * @return the correct answer
     */
    public C getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * The incorrect answer(s) of this question.
     *
     * @return the incorrect answer(s)
     */
    public I getIncorrectAnswers() {
        return incorrectAnswers;
    }

    /**
     * Checks if the given answer is the correct one.
     *
     * @param answer the answer to check
     * @return {@code true} if the given answer is correct
     */
    public boolean isCorrectAnswer(@NotNull final C answer) {
        return correctAnswer.equals(answer);
    }

    /**
     * Attempts to cast this question to a {@link MultipleChoiceQuestion}.<br>
     * <b>This method can throw a {@link ClassCastException}!</b>
     *
     * @return this question, converted to a {@link MultipleChoiceQuestion}
     */
    public MultipleChoiceQuestion toMultiple() {
        return ((MultipleChoiceQuestion) this);
    }

    /**
     * Attempts to cast this question to a {@link BooleanQuestion}.<br>
     * <b>This method can throw a {@link ClassCastException}!</b>
     *
     * @return this question, converted to a {@link BooleanQuestion}
     */
    public BooleanQuestion toBoolean() {
        return ((BooleanQuestion) this);
    }

    @Override
    public String toString() {
        return "Q:" + question + "(" + category + "/" + this.getType() + "/" + difficulty + ")";
    }

    /**
     * The {@link Type} of this question.
     *
     * @return the {@link Type}
     */
    public abstract Type getType();

    /**
     * Represents the type of question.
     **/
    public enum Type implements RequestParameter {

        /**
         * True/False
         */
        BOOLEAN("boolean"),
        /**
         * Multiple Choice
         */
        MULTIPLE("multiple");

        private final String code;

        Type(final String code) {
            this.code = code;
        }

        @Override
        public String getParameterName() {
            return "type";
        }

        /**
         * Returns the code of this question type.
         *
         * @return the code
         */
        public String getParameterValue() {
            return code;
        }
    }

    /**
     * Represents the difficulty of a question.
     */
    public enum Difficulty implements RequestParameter {

        /**
         * Easy
         */
        EASY("easy"),

        /**
         * Medium
         */
        MEDIUM("medium"),

        /**
         * Hard
         */
        HARD("hard");

        private final String code;

        Difficulty(final String code) {
            this.code = code;
        }

        @Override
        public String getParameterName() {
            return "difficulty";
        }

        /**
         * Returns the code of this difficulty.
         *
         * @return the code
         */
        public String getParameterValue() {
            return code;
        }
    }

    /**
     * Represents the category a question belongs to.
     * <p/>
     * This class will statically load in all available categories which can be inspected with {@link #getAvailableCategories()}.
     */
    public static class Category implements RequestParameter {

        private static final Logger LOGGER = LoggerFactory.getLogger(Category.class);
        private static final String CATEGORY_ENDPOINT = OpenTDB.BASE_URL + "/api_category.php";
        private static final List<Category> CATEGORIES = new ArrayList<>();

        static {
            // static block used to load categories
            LOGGER.info("Fetching OpenTDB categories");
            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest request = HttpRequest.newBuilder(URI.create(CATEGORY_ENDPOINT)).build();
            try {
                final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                new JSONObject(response.body()).getJSONArray("trivia_categories").forEach(e -> {
                    final JSONObject jsonObject = (JSONObject) e;
                    final Category category = new Category(jsonObject.getInt("id"), jsonObject.getString("name"));
                    LOGGER.trace("Found OpenTDB category: {}", category);
                    CATEGORIES.add(category);
                });
            } catch (final IOException | InterruptedException e) {
                LOGGER.error("Failed to fetch OpenTDB categories", e);
            }
        }

        private final int id;
        private final String name;

        private Category(final int id, final String name) {
            this.id = id;
            this.name = name;
        }

        /**
         * Returns a list of all available categories.
         *
         * @return an unmodifiable list of all available categories
         */
        public static Collection<Category> getAvailableCategories() {
            return Collections.unmodifiableCollection(CATEGORIES);
        }

        /**
         * Searches all available categories for the category with the given id.
         *
         * @param id the id of the category
         * @return a possibly-null {@link Category}
         */
        @Nullable
        public static Category fromId(final int id) {
            return CATEGORIES.stream().filter(category -> category.id == id).findFirst().orElse(null);
        }

        /**
         * Searches all available categories for the category with the given name.
         *
         * @param name the name of the category
         * @return a possibly-null {@link Category}
         */
        @Nullable
        public static Category fromName(final String name) {
            return CATEGORIES.stream().filter(category -> category.name.equals(name)).findFirst().orElse(null);
        }

        /**
         * Returns the ID of this category.
         *
         * @return the category ID
         */
        public int getId() {
            return id;
        }

        /**
         * Returns the name of this category.
         *
         * @return the category name
         */
        public String getReadableName() {
            return name;
        }

        @Override
        public String toString() {
            return "C:" + name + "(" + id + ")";
        }

        @Override
        public String getParameterName() {
            return "category";
        }

        @Override
        public String getParameterValue() {
            return String.valueOf(id);
        }
    }
}
