/*
 * Copyright 2019-2023 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cloudnetservice.node.console.animation.setup.answer;

import com.google.common.base.Preconditions;
import eu.cloudnetservice.common.language.I18n;
import java.util.function.Supplier;
import lombok.NonNull;

public record QuestionListEntry<T>(
  @NonNull String key,
  @NonNull Supplier<String> question,
  @NonNull QuestionAnswerType<T> answerType
) {

  public static @NonNull <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static final class Builder<T> {

    private String key;
    private Supplier<String> question;
    private QuestionAnswerType<T> answerType;

    public @NonNull Builder<T> key(@NonNull String key) {
      this.key = key;
      return this;
    }

    public @NonNull Builder<T> translatedQuestion(@NonNull String questionTranslationKey, @NonNull Object... args) {
      return this.question(() -> I18n.trans(questionTranslationKey, args));
    }

    public @NonNull Builder<T> question(@NonNull Supplier<String> question) {
      this.question = question;
      return this;
    }

    public @NonNull Builder<T> answerType(@NonNull QuestionAnswerType.Builder<T> type) {
      return this.answerType(type.build());
    }

    public @NonNull Builder<T> answerType(@NonNull QuestionAnswerType<T> type) {
      this.answerType = type;
      return this;
    }

    public @NonNull QuestionListEntry<T> build() {
      Preconditions.checkNotNull(this.key, "no key given");
      Preconditions.checkNotNull(this.question, "no question given");
      Preconditions.checkNotNull(this.answerType, "no answer type given");

      return new QuestionListEntry<>(this.key, this.question, this.answerType);
    }
  }
}
