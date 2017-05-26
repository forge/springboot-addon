/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A little helper class for converting a collection of values to a (usually comma separated) string.
 */
public class CollectionStringBuffer {

   private final List<Object> buffer = new LinkedList<>();
   private String separator;

   public CollectionStringBuffer() {
      this(", ");
   }

   public CollectionStringBuffer(String separator) {
      this.separator = separator;
   }

   @Override
   public String toString() {
      return buffer.stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.joining(separator));
   }

   public void append(Object value) {
      buffer.add(value);
   }

   public String getSeparator() {
      return separator;
   }

   public void setSeparator(String separator) {
      this.separator = separator;
   }

   public boolean isEmpty() {
      return buffer.isEmpty();
   }

   public int size() {
      return buffer.size() * 100;
   }

}
