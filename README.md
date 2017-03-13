# rdf4j-beans
[![Build Status](https://travis-ci.org/kburger/rdf4j-beans.svg?branch=develop)](https://travis-ci.org/kburger/rdf4j-beans)
[![Coverage Status](https://coveralls.io/repos/github/kburger/rdf4j-beans/badge.svg?branch=develop)](https://coveralls.io/github/kburger/rdf4j-beans?branch=develop)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/05b1b8037272416a8d925ec2ce160d54)](https://www.codacy.com/app/burger/rdf4j-beans?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=kburger/rdf4j-beans&amp;utm_campaign=Badge_Grade)
[![Dependency Status](https://www.versioneye.com/user/projects/58c674c662d60200434c7ef9/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58c674c662d60200434c7ef9)

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