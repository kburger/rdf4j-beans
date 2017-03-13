# rdf4j-beans
Java beans IO library for RDF4J.

# usage
Add the `rdf4j-beans` dependency in your POM:
```xml
<dependency>
    <groupId>com.github.kburger</groupId>
    <artifactId>rdf4j-beans</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Annotate your beans:
```java
@Type("http://example.com/Type")
public class MyBean {
    @Predicate(value = "http://purl.org/dc/terms/title", isLiteral = true)
    private String title;
    
    public MyBean(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
}
```

Invoke the mapper:
```java
BeanMapper mapper = new BeanMapper();
mapper.write(new OutputStreamWriter(System.out), new MyBean("example"), "http://example.com/subject", RDFFormat.TURTLE);
```