package org.jboss.forge.addon.springboot;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.jboss.forge.addon.springboot.utils.OkHttpClientHelper.createOkHttpClient;

/**
 * @author <a href="mailto:cmoullia@redhat.com">Charles Moulliard</a>
 */
public class TestParsing {

   private static final String STARTER_URL = "https://start.spring.io";

   public static void main(String[] args) throws Exception
   {
      // Fetch the dependencies list from the start.spring.io server
      OkHttpClient client = createOkHttpClient();
      Request request = new Request.Builder().url(STARTER_URL).build();
      Response response = client.newCall(request).execute();
      //Map data = jsonToMap(response.body().string());
      Map data = jsonToMap2(response.body().string());
      Map dependencies = (Map) data.get("dependencies");
      List<Map> deps = (List) dependencies.get("values");
      for (Object dep : deps)
      {
         Map group = (Map) dep;
         String groupName = (String) group.get("name");
         List content;
         // Add this test as the json file & yaml file uses a different key
         if (group.get("content") != null)
         {
            content = (List) group.get("content");
         } else {
            content = (List) group.get("values");
         }
         for (Object row : content) {
            Map item = (Map) row;
            String id = (String) item.get("id");
            String name = (String) item.get("name");
            String description = (String) item.get("description");
            //list.add(new SpringBootDependencyDTO(groupName, id, name, description));
         }
      }
   }

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

   public static String getString(JsonObject obj, String name)
   {
      return obj.getString(name);
   }

   public static Map<String, Object> jsonToMap2(String content) throws IOException
   {
      HashMap<String, Object> result = new ObjectMapper().readValue(content, HashMap.class);
      JSONObject jObject = new JSONObject(result);
      return toMap2(jObject);
   }

   public static Map<String, Object> toMap2(JSONObject object) throws JSONException
   {
      Map<String, Object> map = new HashMap<String, Object>();

      Iterator<String> keysItr = object.keys();
      while (keysItr.hasNext())
      {
         String key = keysItr.next();
         Object value = object.get(key);

         if (value instanceof JSONArray)
         {
            value = toList2((JSONArray) value);
         }

         else if (value instanceof JSONObject)
         {
            value = toMap2((JSONObject) value);
         }
         map.put(key, value);
      }
      return map;
   }

   public static List<Object> toList2(JSONArray array) throws JSONException
   {
      List<Object> list = new ArrayList<Object>();
      for (int i = 0; i < array.length(); i++)
      {
         Object value = array.get(i);
         if (value instanceof JSONArray)
         {
            value = toList2((JSONArray) value);
         }

         else if (value instanceof JSONObject)
         {
            value = toMap2((JSONObject) value);
         }
         list.add(value);
      }
      return list;
   }
}
