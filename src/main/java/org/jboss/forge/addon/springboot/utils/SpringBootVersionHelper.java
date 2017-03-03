/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.utils;

import java.io.IOException;
import java.io.InputStream;

public class SpringBootVersionHelper
{

   public static String getVersion(String name)
   {
      try (InputStream is = SpringBootVersionHelper.class
               .getResourceAsStream("/META-INF/maven/org.jboss.forge.addon/spring-boot/pom.xml"))
      {
         String xml = IOHelper.loadText(is);
         String version = between(xml, "<" + name + ">", "</" + name + ">");
         return version;
      }
      catch (IOException e)
      {
         // ignore
      }
      return "";
   }

   /**
    * Returns the string after the given token
    *
    * @param text  the text
    * @param after the token
    * @return the text after the token, or <tt>null</tt> if text does not contain the token
    */
   public static String after(String text, String after)
   {
      if (!text.contains(after))
      {
         return null;
      }
      return text.substring(text.indexOf(after) + after.length());
   }

   /**
    * Returns the string before the given token
    *
    * @param text   the text
    * @param before the token
    * @return the text before the token, or <tt>null</tt> if text does not contain the token
    */
   public static String before(String text, String before)
   {
      if (!text.contains(before))
      {
         return null;
      }
      return text.substring(0, text.indexOf(before));
   }

   /**
    * Returns the string between the given tokens
    *
    * @param text   the text
    * @param after  the before token
    * @param before the after token
    * @return the text between the tokens, or <tt>null</tt> if text does not contain the tokens
    */
   public static String between(String text, String after, String before)
   {
      text = after(text, after);
      if (text == null)
      {
         return null;
      }
      return before(text, before);
   }

}
