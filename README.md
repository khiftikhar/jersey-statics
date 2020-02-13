# jersey-statics
If you really, really, really want to serve the your static content through jersey resource, so here it is.
This library have a [ResourceService](src/main/java/info/hassan/jersey/statics/services/ResourceService.java) that has two implementations,
one for production, and another for development. 

1. [ResourceServiceImpl](src/main/java/info/hassan/jersey/statics/services/ResourceServiceImpl.java)
2. [ReloadableResourceServiceImpl](src/main/java/info/hassan/jersey/statics/services/ReloadableResourceServiceImpl.java)

There also a resource [StaticResource](src/main/java/info/hassan/jersey/statics/resources/StaticsResource.java) 
that you can use and configure in your ResourceConfig, e.g. 

```java
final Resource resource = Resource.builder(StaticsResource.class).path("/static/").build();
registerResources(resource);
```
The StaticResource have only one constructor with ``@Inject`` 

```java
@Inject
public StaticsResource(ResourceService resourceService) {
  this.resourceService = resourceService;
}
```
So your CDI framework should be able to provide the right implementation.

This resource have two endpoints one that serves, index.html and other that serve all resource in 
all sub-directories. For example calling 
``http://localhost:8080/`` or ``http://localhost:8080/index.html`` will return the ``index.html`` so 
this implies that you must have index.html at the base directory. Lets look at an example base direcotry

```
- html
- html/index.html
- html/css/main.css
- html/js/main.js
- html/favicon.ico
- html/img/logo.svg
```
Here html is the base directory and it must contain index.html, rest is all up to you.

### How to use ``ResourceService``

For example, if you I will use this service in my spring boot project then in my ``@Configuration``
class, I will do something like 

```java
@Configuration
public class AppConfig {

  @Profile("dev")
  @Bean
  public ResourceService reloadableResourceService(
      @Value("${baseDir}") final String baseDir,
      @Value("${pollForChangeInMillis}") final Long pollForChangeInMillis)
      throws IOException {
    return new ReloadableResourceServiceImpl(Paths.get(baseDir), pollForChangeInMillis);
  }

  @Profile("prod")
  @Bean
  public ResourceService mainResourceService(@Value("${baseDir}") final String baseDir)
      throws IOException {
    return new ResourceServiceImpl(Paths.get(baseDir));
  }
}
``` 

