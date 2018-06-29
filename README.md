## Infinispan Remote Endpoints Interoperability demo

This repo contains a series of samples to demonstrate how to access data from Infinispan Server from multiple clients
(REST and Hot Rod),  in different formats (text, JSON, binary, POJO).


To run the demos, do a ```mvn clean install``` first, then go to the ```client/``` folder and launch the server with:

```
docker run -v $PWD/../entities/target:/opt/jboss/infinispan-server/standalone/deployments -v $PWD/config:/opt/jboss/infinispan-server/standalone/configuration/demo -it -p 8080:8080 -p 11222:11222 -e "APP_USER=dev" -e "APP_PASS=dev" jboss/infinispan-server:9.3.0.Final demo/clustered.xml
```

This instructs the server to be started with REST credentials ```dev/dev```, exposing ports ```8080``` and ```11222``` to ```localhost``` and using the configuration file ```client/config/clustered.xml```