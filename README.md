# ddm-form-data-storage

### Overview
The library for managing form data using specific storage.  
It supports reading and writing operations and works with json from data and file form data.

### Usage

- Declare `StorageServiceFactory.class` bean
- Declare storage configuration bean for storage factory (list of available below)
- Create storage service bean using `StorageServiceFactory.class` and storage configurations bean
- Inject storage service to your service
- Integration context services communication.

#### Available storage configurations
- Ceph (`CephStorageConfiguration.class`)  
Config fields:  
  `httpEndpoint` - http endpoint to ceph
  `accessKey` - ceph access key  
  `secretKey` - ceph secret key  
  `bucket` - ceph bucket name
  

#### Example
```java
@Bean
public StorageServiceFactory storageServiceFactory(ObjectMapper objectMapper) {
  return new StorageServiceFactory(objectMapper);
}

@Bean
@ConfigurationProperties(prefix = "<prefix-to-ceph-properties>")
public CephStorageConfiguration cephStorageConfiguration() {
  return new CephStorageConfiguration();
}

@Bean
public FormDataStorageService formDataStorageService(StorageServiceFactory factory,
        CephStorageConfiguration config) {
  return factory.formDataStorageService(config);
}
```
#### Required configuration for services context configuration.Define this properties:
```yaml
storage:
  externalContext:
    service:
      base-url: urlToService
```

#### Example for context services communication
```java
  @Bean
  public ApplicationContextService applicationContextService(
      StorageServiceFactory factory,
      ExternalContextService service) {
    return factory.mongoContextService(service);
  }
```

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.
    

### License

The ddm-form-data-storage is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).