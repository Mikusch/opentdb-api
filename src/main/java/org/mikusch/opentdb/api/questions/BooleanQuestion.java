package org.mikusch.opentdb.api.questions;

import javax.annotation.Nonnull;

/**
 * Represents a "True/False" question received from the OpenTDB API.
 */
public class BooleanQuestion extends Question<Boolean, Boolean> {

    public BooleanQuestion(final Category category,
                           final Difficulty difficulty,
                           final String question,
                           final Boolean correctAnswer,
                           final Boolean incorrectAnswers) {
        super(category, difficulty, question, correctAnswer, incorrectAnswers);
    }

    /**
     * Returns the incorrect answer.<br/>
     * This method returns a primitive {@code boolean}.
     *
     * @return the incorrect answer
     */
    public boolean getIncorrectAnswer() {
        return this.getIncorrectAnswers();
    }

    /**
     * @inheritDoc
     */
    @Nonnull
    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }
}
