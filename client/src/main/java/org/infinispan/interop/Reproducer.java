package org.infinispan.interop;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.util.CloseableIteratorCollection;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;

public class Reproducer {

   public static void main(String[] args) throws IOException {
      String mode = args.length > 0 ? args[0] : null;

      String cacheName = "ACCESSORIES";
      RemoteCacheManager remoteCacheManager = new RemoteCacheManager(new ConfigurationBuilder()
            .marshaller(new ProtoStreamMarshaller())
            .build());

      //initialize client-side serialization context
      SerializationContext serializationContext = ProtoStreamMarshaller.getSerializationContext(remoteCacheManager);
      ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
      String protoFile = protoSchemaBuilder.fileName("crypto.proto")
            .addClass(CryptoCurrency.class)
            .build(serializationContext);

      //initialize server-side serialization context via rest endpoint
      RemoteCache<Object, Object> protobuf = remoteCacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);
      protobuf.put("crypto.proto", protoFile);

      RemoteCache<Integer, CryptoCurrency> remoteCache = remoteCacheManager.getCache(cacheName);
      // Write from Hot Rod
      int size = 1_000_000;


      if ("read".equals(mode)) {
         AtomicInteger atomicInteger = new AtomicInteger(0);
         CloseableIteratorCollection<CryptoCurrency> values = remoteCache.values();
         values.forEach(c -> {
            if (atomicInteger.incrementAndGet() % 1000 == 0) {
               System.out.println("Read " + atomicInteger.get());
            }
         });
      } else {
         for (int i = 0; i < size; i++) {
            remoteCache.put(i, new CryptoCurrency("Bitcoin_" + i, i));
            if (i % 10_000 == 0) System.out.println("Written " + i);
         }
      }
      System.out.println("Cache size after insertion = " + remoteCache.size());

   }
}
