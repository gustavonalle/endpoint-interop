package org.infinispan.interop;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.infinispan.commons.dataconversion.MediaType.APPLICATION_JSON_TYPE;

import java.io.IOException;

import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

/**
 * Interop when storing java objects in the server.
 */
public class ObjectStorageInterop {

   public static void main(String[] args) throws IOException {
      Configuration configuration = new ConfigurationBuilder().build();
      RemoteCacheManager remoteCacheManager = new RemoteCacheManager(configuration);

      // The pojo-cache is configured with 'application/x-java-object' for keys and values
      String cacheName = "pojo-cache";

      RemoteCache<String, CryptoCurrency> remoteCache = remoteCacheManager.getCache(cacheName);

      // Write from Hot Rod
      remoteCache.put("BTC", new CryptoCurrency("Bitcoin", 1));
      remoteCache.put("LTC", new CryptoCurrency("Litecoin", 2));
      remoteCache.put("DOGE", new CryptoCurrency("Dogecoin", 100));

      System.out.println("Cache size after insertion = " + remoteCache.size());

      Executor requestExecutor = Executor.newInstance().auth("dev", "dev");

      // Read from REST as JSON
      String response = requestExecutor
            .execute(Request.Get(String.format("http://%s:%d/rest/%s/%s", "localhost", 8080, cacheName, "BTC"))
                  .addHeader(ACCEPT, APPLICATION_JSON_TYPE))
            .returnContent().asString();

      System.out.println("/GET BTC as JSON: " + response);

      // Write from REST in JSON format
      String json = "{\"_type\":\"org.infinispan.interop.CryptoCurrency\",\"description\":\"Monero\",\"rank\":12}";
      StatusLine status = requestExecutor.execute(
            Request.Post(String.format("http://%s:%d/rest/%s/%s", "localhost", 8080, cacheName, "ETH"))
                  .bodyString(json, ContentType.APPLICATION_JSON))
            .returnResponse().getStatusLine();

      System.out.println("Inserted new entry in JSON format:" + json + ", status code: " + status);

      // Read from Hot Rod the data inserted via REST
      CryptoCurrency eth = remoteCache.get("ETH");
      System.out.println("Reading as Java Object: " + eth);
   }


}



