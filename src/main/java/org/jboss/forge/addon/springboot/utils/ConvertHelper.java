/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:cmoullia@redhat.com">Charles Moulliard</a>
 */
public class ConvertHelper
{

   public static Map<String, Object> jsonToMap(String content) throws IOException
   {
      HashMap<String, Object> result = new ObjectMapper().readValue(content, HashMap.class);
      JSONObject jObject = new JSONObject(result);
      return toMap(jObject);
   }

   public static Map<String, Object> toMap(JSONObject object) throws JSONException
   {
      Map<String, Object> map = new HashMap<String, Object>();

      Iterator<String> keysItr = object.keys();
      while (keysItr.hasNext())
      {
         String key = keysItr.next();
         Object value = object.get(key);

         if (value instanceof JSONArray)
         {
            value = toList((JSONArray) value);
         }

         else if (value instanceof JSONObject)
         {
            value = toMap((JSONObject) value);
         }
         map.put(key, value);
      }
      return map;
   }

   public static List<Object> toList(JSONArray array) throws JSONException
   {
      List<Object> list = new ArrayList<Object>();
      for (int i = 0; i < array.length(); i++)
      {
         Object value = array.get(i);
         if (value instanceof JSONArray)
         {
            value = toList((JSONArray) value);
         }

         else if (value instanceof JSONObject)
         {
            value = toMap((JSONObject) value);
         }
         list.add(value);
      }
      return list;
   }


}
