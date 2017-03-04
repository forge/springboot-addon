/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * @author <a href="mailto:cmoullia@redhat.com">Charles Moulliard</a>
 */
public class ConvertHelper
{

   public static Map<String, Object> jsonToMap(String content) throws IOException
   {
      JsonReader reader = Json.createReader(new StringReader(content));
      JsonObject jsonObject = reader.readObject();
      return toMap(jsonObject);
   }

   public static Map<String, Object> toMap(JsonObject object) throws JsonException
   {
      Map<String, Object> map = new HashMap<String, Object>();

      Iterator<String> keysItr = object.keySet().iterator();
      while(keysItr.hasNext()) {
         String key = keysItr.next();
         Object value = object.get(key);

         if(value instanceof JsonArray) {
            value = toList((JsonArray) value);
         }
         else if(value instanceof JsonObject) {
            value = toMap((JsonObject) value);
         }
         map.put(key, value);
      }
      return map;
   }

   public static List<Object> toList(JsonArray array) throws JsonException
   {
      List<Object> list = new ArrayList<Object>();
      for (int i = 0; i < array.size(); i++)
      {
         Object value = array.get(i);
         if (value instanceof JsonArray)
         {
            value = toList((JsonArray) value);
         }
         else if (value instanceof JsonObject)
         {
            value = toMap((JsonObject) value);
         }
         list.add(value);
      }
      return list;
   }

   public static String removeDoubleQuotes(Object content) {
      return content.toString().replaceAll("\"", "");
   }
}
