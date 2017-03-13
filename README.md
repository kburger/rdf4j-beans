# rdf4j-beans
[![Build Status](https://travis-ci.org/kburger/rdf4j-beans.svg?branch=master)](https://travis-ci.org/kburger/rdf4j-beans)
[![Coverage Status](https://coveralls.io/repos/github/kburger/rdf4j-beans/badge.svg?branch=master)](https://coveralls.io/github/kburger/rdf4j-beans?branch=master)

Java beans IO library for RDF4J.

# usage
Add the `rdf4j-beans` dependency in your POM:
```xml
<dependency>
    <groupId>com.github.kburger</groupId>
    <artifactId>rdf4j-beans</artifactId>
    <version>0.1.0</version>
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

# changelog
0.1.0
- Initial release.
- Support for bean serialization.