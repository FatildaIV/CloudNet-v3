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

package eu.cloudnetservice.driver.document.gson;

import com.google.common.base.Preconditions;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.cloudnetservice.driver.document.Document;
import eu.cloudnetservice.driver.document.DocumentSerialisationException;
import eu.cloudnetservice.driver.document.StandardSerialisationStyle;
import eu.cloudnetservice.driver.document.gson.send.GsonRootObjectVisitor;
import eu.cloudnetservice.driver.document.property.DefaultedDocPropertyHolder;
import eu.cloudnetservice.driver.document.send.DocumentSend;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Set;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mutable version of a gson document implementing the full mutable document functionality.
 *
 * @since 4.0
 */
final class MutableGsonDocument
  extends ImmutableGsonDocument
  implements Document.Mutable, DefaultedDocPropertyHolder.Mutable<Document.Mutable> {

  @Serial
  private static final long serialVersionUID = 4248891600084741117L;

  /**
   * Constructs a new, empty gson document instance.
   */
  public MutableGsonDocument() {
    this(new JsonObject());
  }

  /**
   * Constructs a new gson document instance using the given initial internal object. Note that the given object is not
   * copied, it is up to the caller to ensure no races or data leaks are created when using this constructor.
   *
   * @param internalObject the initial internal json object to use.
   * @throws NullPointerException if the given internal object is null.
   */
  MutableGsonDocument(@NonNull JsonObject internalObject) {
    super(internalObject);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable clear() {
    var keys = Set.copyOf(this.internalObject.keySet());
    for (var key : keys) {
      this.internalObject.remove(key);
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable remove(@NonNull String key) {
    this.internalObject.remove(key);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable receive(@NonNull DocumentSend send) {
    var visitor = new GsonRootObjectVisitor(this.internalObject);
    send.rootElement().accept(visitor);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable appendNull(@NonNull String key) {
    this.internalObject.add(key, JsonNull.INSTANCE);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable appendTree(@Nullable Object value) {
    var element = value == null ? JsonNull.INSTANCE : GsonProvider.NORMAL_GSON_INSTANCE.toJsonTree(value);
    if (element.isJsonObject()) {
      // append all key-value pairs of the document
      var jsonObject = element.getAsJsonObject();
      for (var entry : jsonObject.entrySet()) {
        this.internalObject.add(entry.getKey(), entry.getValue());
      }
    }

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable append(@NonNull Document document) {
    var documentSend = document.send();
    return this.receive(documentSend);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable append(@NonNull String key, @Nullable Object value) {
    var element = value == null ? JsonNull.INSTANCE : GsonProvider.NORMAL_GSON_INSTANCE.toJsonTree(value);
    this.internalObject.add(key, element);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable append(@NonNull String key, @Nullable Number value) {
    this.internalObject.addProperty(key, value);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable append(@NonNull String key, @Nullable Boolean value) {
    this.internalObject.addProperty(key, value);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable append(@NonNull String key, @Nullable String value) {
    this.internalObject.addProperty(key, value);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable append(@NonNull String key, @Nullable Document value) {
    if (value == null) {
      this.internalObject.add(key, JsonNull.INSTANCE);
      return this;
    }

    // put in a new object for the document
    var object = new JsonObject();
    this.internalObject.add(key, object);

    // receive and convert the content of the document into this document
    var send = value.send();
    var visitor = new GsonRootObjectVisitor(object);
    send.rootElement().accept(visitor);

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Document.Mutable propertyHolder() {
    return this;
  }

  /**
   * Writes this document in a compact way to the given output stream. This method is part of the java serialisation
   * api.
   *
   * @param out the target stream to write the content of this document to.
   * @throws IOException                    if an i/o error occurs while writing the content.
   * @throws DocumentSerialisationException if an exception occurs serialising the document.
   */
  @Serial
  private void writeObject(@NonNull ObjectOutputStream out) throws IOException {
    out.writeUTF(this.serializeToString(StandardSerialisationStyle.COMPACT));
  }

  /**
   * Reads a json object from the given stream and copies all it's members into this document. This method does not take
   * into account whether a key of the deserialized object is already present in this document. This method is part of
   * the java serialisation api.
   *
   * @param in the stream to read the json content from.
   * @throws IOException              if an i/o error occurs while reading the document content.
   * @throws IllegalArgumentException if the decoded json element from the stream is not a json object.
   */
  @Serial
  private void readObject(@NonNull ObjectInputStream in) throws IOException {
    var parsedDocument = JsonParser.parseString(in.readUTF());
    Preconditions.checkArgument(parsedDocument.isJsonObject(), "Input is not a json object");

    // put all elements of the parsed object into the internal object
    var object = parsedDocument.getAsJsonObject();
    for (var entry : object.entrySet()) {
      this.internalObject.add(entry.getKey(), entry.getValue());
    }
  }
}
