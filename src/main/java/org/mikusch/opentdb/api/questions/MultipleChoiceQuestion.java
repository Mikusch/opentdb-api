package org.mikusch.opentdb.api.questions;

import java.util.Collection;

/**
 * Represents a "Multiple Choice" question received from the OpenTDB API.
 */
public class MultipleChoiceQuestion extends Question<String, Collection<String>> {

    public MultipleChoiceQuestion(final Category category,
                                  final Difficulty difficulty,
                                  final String question,
                                  final String correctAnswer,
                                  final Collection<String> incorrectAnswers) {
        super(category, difficulty, question, correctAnswer, incorrectAnswers);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Type getType() {
        return Type.MULTIPLE;
    }
}
