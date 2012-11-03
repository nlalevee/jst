/*
 *  Copyright 2012 JST contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hibnet.jst.ui.contentassist.antlr.internal;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class JstLexerImpl extends InternalJstLexer {

	private boolean rawText = false;

	private boolean directiveParameters = false;

	private int directiveStackSize = 0;

	private int parenthesisStackSize = 0;

	public JstLexerImpl() {
		super();
	}

	public JstLexerImpl(CharStream input) {
		super(input);
	}

	public JstLexerImpl(CharStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override
	public void mTokens() throws RecognitionException {
		char next = (char) input.LA(1);
		char nextnext = (char) input.LA(2);
		if (rawText && (next != '#' || nextnext == '#') && (next != '$' || nextnext == '$')) {
			actual_mRULE_TEXT();
		} else if (rawText && next == '#' && nextnext == '*') {
			readMultiLineComment();
		} else if (rawText && next == '#' && nextnext == '-') {
			readSingleLineComment();
		} else {
			rawText = false;
			super.mTokens();
			if (directiveParameters) {
				if (next == '(') {
					parenthesisStackSize++;
				} else if (next == ')') {
					parenthesisStackSize--;
					if (parenthesisStackSize == 0) {
						directiveParameters = false;
						rawText = true;
					}
				}
			} else {
				switch (state.type) {
					case RULE_DIRECTIVE_ELSE:
						rawText = true;
						break;
					case RULE_DIRECTIVE_TEMPLATE:
					case RULE_DIRECTIVE_FOR:
					case RULE_DIRECTIVE_IF:
						directiveStackSize++;
						directiveParameters = true;
						break;
					case RULE_DIRECTIVE_ELSEIF:
					case RULE_DIRECTIVE:
					case RULE_DIRECTIVE_ECHO:
					case RULE_DIRECTIVE_ECHO_ELVIS:
						directiveParameters = true;
						break;
					case RULE_DIRECTIVE_END:
						directiveStackSize--;
						if (directiveStackSize > 0) {
							rawText = true;
						}
						break;
				}
			}
		}
	}

	private void readSingleLineComment() {
		state.type = RULE_SL_COMMENT;
		state.channel = DEFAULT_TOKEN_CHANNEL;

		// consume #-
		input.consume();
		input.consume();
		while (true) {
			int next = input.LA(1);
			if (next == -1) {
				return;
			}
			input.consume();
			if (next == '\n' || next == '\r') {
				return;
			}
		}
	}

	private void readMultiLineComment() {
		state.type = RULE_ML_COMMENT;
		state.channel = DEFAULT_TOKEN_CHANNEL;

		// consume #*
		input.consume();
		input.consume();
		while (true) {
			int next = input.LA(1);
			if (next == -1) {
				return;
			}
			input.consume();
			if (next != '*') {
				continue;
			}
			int nextNext = input.LA(1);
			if (nextNext == -1) {
				return;
			}
			input.consume();
			if (nextNext == '#') {
				return;
			}
		}
	}

	private void actual_mRULE_TEXT() throws RecognitionException {
		state.type = RULE_TEXT;
		state.channel = DEFAULT_TOKEN_CHANNEL;

		while (true) {
			int next = input.LA(1);
			if (next == -1) {
				return;
			}
			if (next != '#' && next != '$') {
				input.consume();
			} else {
				int nextNext = input.LA(2);
				if (next == '#' && nextNext == '#' || next == '$' && nextNext == '$') {
					input.consume();
					input.consume();
				} else {
					return;
				}
			}
		}
	}

}