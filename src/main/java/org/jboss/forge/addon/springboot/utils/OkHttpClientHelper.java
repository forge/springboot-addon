/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.utils;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * To work around: https://github.com/square/okhttp/issues/2323
 * version 3.0.1 onwards fails (maybe 3.3.0 is fixed)
 */
public class OkHttpClientHelper
{

   public static OkHttpClient createOkHttpClient()
   {
      X509TrustManager tm = provideX509TrustManager();
      SSLSocketFactory ssf = provideSSLSocketFactory(tm);

      return new OkHttpClient.Builder()
               //                .sslSocketFactory(ssf)
               .build();
   }

   private static X509TrustManager provideX509TrustManager()
   {
      try
      {
         TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         factory.init((KeyStore) null);
         TrustManager[] trustManagers = factory.getTrustManagers();
         return (X509TrustManager) trustManagers[0];
      }
      catch (NoSuchAlgorithmException | KeyStoreException exception)
      {
         // ignore
      }

      return null;
   }

   private static SSLSocketFactory provideSSLSocketFactory(X509TrustManager trustManager)
   {
      try
      {
         SSLContext sslContext = SSLContext.getInstance("TLS");
         sslContext.init(null, new TrustManager[] { trustManager }, null);
         return sslContext.getSocketFactory();
      }
      catch (NoSuchAlgorithmException | KeyManagementException exception)
      {
         // ignore
      }

      return (SSLSocketFactory) SSLSocketFactory.getDefault();
   }
}
