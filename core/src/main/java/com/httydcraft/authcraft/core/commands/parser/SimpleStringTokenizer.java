package com.httydcraft.authcraft.core.commands.parser;

import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.exception.ArgumentParseException;

import java.util.Collections;
import java.util.List;

// #region Class Documentation
/**
 * A simple string tokenizer for parsing command arguments without quote support.
 * Splits input strings into arguments based on whitespace.
 */
public class SimpleStringTokenizer {
    private static final GoogleLogger LOGGER = GoogleLogger.forEnclosingClass();
    private static final List<String> EMPTY_TEXT = Collections.singletonList("");
    // #endregion

    // #region Parsing Methods
    /**
     * Parses a string into an argument stack.
     *
     * @param arguments The input string to parse. May be null or empty.
     * @return An {@link ArgumentStack} containing the parsed arguments.
     * @throws ArgumentParseException if parsing fails.
     */
    public static ArgumentStack parse(String arguments) throws ArgumentParseException {
        LOGGER.atFine().log("Parsing arguments: %s", arguments);
        if (arguments == null || arguments.isEmpty()) {
            LOGGER.atFine().log("Returning empty argument stack for null or empty input");
            return ArgumentStack.empty();
        }

        TokenizerState state = new TokenizerState(arguments);
        ArgumentStack returnedArgs = ArgumentStack.empty();
        while (state.hasMore()) {
            skipWhiteSpace(state);
            String arg = nextArg(state);
            returnedArgs.add(arg);
        }
        LOGGER.atFine().log("Parsed %d arguments", returnedArgs.size());
        return returnedArgs;
    }

    /**
     * Parses a string for auto-completion purposes.
     *
     * @param args The input string to parse. Must not be null.
     * @return An {@link ArgumentStack} containing the parsed arguments.
     */
    public static ArgumentStack parseForAutoCompletion(String args) {
        Preconditions.checkNotNull(args, "args must not be null");
        LOGGER.atFine().log("Parsing for auto-completion: %s", args);

        if (args.isEmpty()) {
            LOGGER.atFine().log("Returning empty text for empty input");
            return ArgumentStack.copyExact(EMPTY_TEXT);
        }
        return parse(args);
    }
    // #endregion

    // #region Helper Methods
    /**
     * Skips whitespace characters in the tokenizer state.
     *
     * @param state The tokenizer state. Must not be null.
     * @throws ArgumentParseException if parsing fails.
     */
    private static void skipWhiteSpace(TokenizerState state) throws ArgumentParseException {
        Preconditions.checkNotNull(state, "state must not be null");

        if (!state.hasMore()) {
            return;
        }
        if (Character.isWhitespace(state.peek())) {
            state.next();
            LOGGER.atFine().log("Skipped whitespace");
        }
    }

    /**
     * Extracts the next argument from the tokenizer state.
     *
     * @param state The tokenizer state. Must not be null.
     * @return The next argument as a string.
     * @throws ArgumentParseException if parsing fails.
     */
    private static String nextArg(TokenizerState state) throws ArgumentParseException {
        Preconditions.checkNotNull(state, "state must not be null");

        StringBuilder argBuilder = new StringBuilder();
        if (state.hasMore()) {
            parseString(state, argBuilder);
        }
        String arg = argBuilder.toString();
        LOGGER.atFine().log("Extracted argument: %s", arg);
        return arg;
    }

    /**
     * Parses a string until a whitespace is encountered.
     *
     * @param state   The tokenizer state. Must not be null.
     * @param builder The builder to append characters to. Must not be null.
     * @throws ArgumentParseException if parsing fails.
     */
    private static void parseString(TokenizerState state, StringBuilder builder) throws ArgumentParseException {
        Preconditions.checkNotNull(state, "state must not be null");
        Preconditions.checkNotNull(builder, "builder must not be null");

        while (state.hasMore()) {
            int nextCodePoint = state.peek();
            if (Character.isWhitespace(nextCodePoint)) {
                return;
            }
            builder.appendCodePoint(state.next());
        }
    }
    // #endregion

    // #region Tokenizer State
    /**
     * Internal state for the tokenizer, tracking the current position in the input string.
     */
    private static class TokenizerState {
        private final String buffer;
        private int index = -1;

        /**
         * Constructs a new tokenizer state.
         *
         * @param buffer The input string to tokenize. Must not be null.
         */
        TokenizerState(String buffer) {
            this.buffer = Preconditions.checkNotNull(buffer, "buffer must not be null");
        }

        /**
         * Checks if there are more characters to process.
         *
         * @return {@code true} if there are more characters, {@code false} otherwise.
         */
        public boolean hasMore() {
            return index + 1 < buffer.length();
        }

        /**
         * Peeks at the next character without advancing the index.
         *
         * @return The next character's code point.
         * @throws ArgumentParseException if there are no more characters.
         */
        public int peek() throws ArgumentParseException {
            if (!hasMore()) {
                throw createException("Buffer overrun while parsing args");
            }
            return buffer.codePointAt(index + 1);
        }

        /**
         * Advances to the next character and returns its code point.
         *
         * @return The current character's code point.
         * @throws ArgumentParseException if there are no more characters.
         */
        public int next() throws ArgumentParseException {
            if (!hasMore()) {
                throw createException("Buffer overrun while parsing args");
            }
            return buffer.codePointAt(++index);
        }

        /**
         * Creates an exception for parsing errors.
         *
         * @param message The error message.
         * @return An {@link ArgumentParseException}.
         */
        public ArgumentParseException createException(String message) {
            return new ArgumentParseException(message, buffer, index);
        }
    }
    // #endregion
}