The following libraries are included in Compass distribution because they are
required either for building compass or for running the sample apps. Note that each
of these libraries is subject to the respective license; check the respective project
distribution/website before using any of them in your own applications.

* ant/ant.jar
- Ant 1.6.5 (http://ant.apache.org)
- used to build compass and the sample apps

* antlr/antlr-2.7.6.jar
- ANTLR 2.7.6 (http://www.antlr.org)
- required for running PetClinic (by Hibernate3)

* c3p0/c3p0-0.9.0.2.jar
- C3P0 0.9.0.2 connection pool (http://sourceforge.net/projects/c3p0)
- required for building compass
- required at runtime when using Jdbc Index location with C3P0 pool

* cglib/cglib-nodep-2.2.jar
- CGLIB 2.2 (http://cglib.sourceforge.net)
- required for building compass
- required for running PetClinic (by Hibernate)

* dom4j/dom4j-1.6.1
- DOM4J 1.6.1 XML parser (http://www.dom4j.org)
- required for building compass

* ehcache/ehcache-1.2.3.jar
- EHCache 1.2.3 (http://ehcache.sourceforge.net)
- required for building compass
- required for running PetClinic (by Hibernate)

* hibernate/hibernate3.jar
- Hibernate 3.2.6 GA (http://www.hibernate.org)
- required for building compass
- required at runtime when using Compass's Hibernate 3.x support

* hibernate/hibernate-annotations.jar
- Hibernate Annotations 3.2.0 GA (http://www.hibernate.org) (taken form EntityManager 3.3.1 GA)
- required for building compass

* hibernate/javassist.jar
- Java Assist 3.3 (Required by Hibernate EntityManager) (taken form Core 3.2.4)
- required for building compass

* hibernate/hibernate-entitymanager.jar
- Hibernate EntityManager 3.3.1 GA (http://www.hibernate.org)
- required for building compass

* hibernate/jboss-archive-browsing.jar
- jboss-common-core 2.0.2.Alpha (taken form EntityManager 3.3.1 GA)
- required for building compass

* hsqldb/hsqldb.jar
- HSQLDB 1.8.0.1 (http://hsqldb.sourceforge.net)
- required for running PetClinic

* ibatis/ibatis-2.3.4.726.jar
- iBATIS SQL Maps 2.3.4.726 (http://www.ibatis.com)
- required for building compass
- required at runtime when using Compass's iBATIS SQL Maps 2.0 support

* j2ee/activation.jar
- JavaBeans Activation Framework 1.0.2 (http://java.sun.com/products/javabeans/glasgow/jaf.html)
- required for building compass

* j2ee/connector.jar
- J2EE Connector Architecture 1.5 (http://java.sun.com/j2ee/connector)
- required at runtime when using Hibernate's JCA Connector

* j2ee/ejb.jar
- Enterprise JavaBeans API 2.0 (http://java.sun.com/products/ejb)
- required for building compass

* j2ee/jaxrpc.jar
- JAX-RPC API 1.1 (http://java.sun.com/xml/jaxrpc)
- required for building compass

* j2ee/jdbc2_0-stdext.jar
- JDBC 2.0 Standard Extensions (http://java.sun.com/products/jdbc)
- required at runtime when using Compass's JDBC support on J2SE 1.3

* j2ee/jms.jar
- Java Message Service API 1.1 (java.sun.com/products/jms)
- required for building compass

* j2ee/jsp-api.jar
- JSP API 2.0 (http://java.sun.com/products/jsp)
- required for building compass

* j2ee/jstl.jar
- JSP Standard Tag Library API 1.0 (http://java.sun.com/products/jstl)
- required for building compass

* j2ee/jta.jar
- Java Transaction API 1.0.1b (http://java.sun.com/products/jta)
- required for building compass
- required at runtime when using Compass's JTA transaction support

* j2ee/mail.jar
- JavaMail 1.3.2 (http://java.sun.com/products/javamail)
- required for building compass

* j2ee/rowset.jar
- JDBC RowSet Implementations 1.0.1 (http://java.sun.com/products/jdbc)
- required for building compass on JDK < 1.5

* j2ee/servlet-api.jar
- Servlet API 2.4 (http://java.sun.com/products/servlet)
- required for building compass

* j2ee/xml-apis.jar
- JAXP, DOM and SAX APIs (taken from Xerces 2.6 distribution; http://xml.apache.org/xerces2-j)
- required at runtime when using Compass on JDK < 1.4

* jakarta-commons/commons-collections.jar
- Commons Collections 3.1 (http://jakarta.apache.org/commons/collections)
- required for running PetClinic (by Commons DBCP, Hibernate, OJB)

* jakarta-commons/commons-dbcp.jar
- Commons DBCP 1.2.1 (http://jakarta.apache.org/commons/dbcp)
- required for building compass
- required at runtime when using Compass's Jdbc with commons pool

* jakarta-commons/commons-lang.jar
- Commons Lang 2.1 (http://jakarta.apache.org/commons/lang)
- required for building compass (by OJB)
- required at runtime when using Compass's OJB support (by OJB)

* jakarta-commons/commons-logging.jar
- Commons Logging 1.0.4 (http://jakarta.apache.org/commons/logging)
- required for building compass
- required at runtime, as Compass uses it for all logging

* jakarta-commons/commons-pool.jar
- Commons Pool 1.2 (http://jakarta.apache.org/commons/pool)
- required for running (by Commons DBCP)

* jakarta-commons/commons-jexl-1.0.jar
- Commons JEXL (http://jakarta.apache.org/commons/jexl)
- required building Compass
- required at runtime when using JEXL based dynamic meta data

* jakarta-taglibs/standard.jar
- Jakarta's JSTL implementation 1.0.6 (http://jakarta.apache.org/taglibs)
- required for running PetClinic

* jaxen/jaxen-1.1.1.jar
- Jaxen XPath engine (http://jaxen.codehaus.org/)
- required when using dom4j/... for xpath processing

* jotm/jotm.jar
- JOTM 1.5.3 (http://jotm.objectweb.org)
- required for building compass

* jpa/persistence.jar
- Java Persistence API (Publid Draft Version)
- required for building compass

* jotm/jotm-carol.jar
- JOTM Carol 1.5.3 (http://jotm.objectweb.org)
- required for building compass

* jotm/jrmp-stubs.jar
- JOTM jrmp 1.5.3 (http://jotm.objectweb.org)
- required for building compass

* jpox/jpox.jar
- JPox 1.1.0 JDO implementation (http://www.jpox.org/)
- required for building compass

* jpox/jpox-enhancer.jar
- JPox 1.1.0 JDO implementation (http://www.jpox.org/)
- required for building compass

* junit/junit.jar
- JUnit 3.8.1 (http://www.junit.org)
- required for building the test suite

* log4j/log4j-1.2.13.jar
- Log4J 1.2.13 (http://logging.apache.org/log4j)
- required for building compass

* lucene/lucene-analyzers.jar
- Lucene 2.4.1 (http://lucene.apache.org)
- required for building compass
- required at runtime if using extended analyzers

* lucene/lucene-core.jar
- Lucene 2.4.1 (http://lucene.apache.org)
- required for building compass
- required at runtime

* lucene/lucene-highlighter.jar
- Lucene 2.4.1 (http://lucene.apache.org)
- required for building compass
- required at runtime if using highlighter features

* lucene/lucene-snowball.jar
- Lucene 2.4.1 (http://lucene.apache.org)
- required for building compass
- required at runtime if using the snowball analyzer features

* ognl/ognl-2.6.5.jar
- OpenSymphony Ognl (http://www.opensymphony.com/ognl)
- required for building compass
- required at runtime when using dynamic meta data with ognl

* spring/spring.jar (and spring-*.jar)
- Spring Framework 2.5.6 (http://www.springframework.org)
- required for building compass
- required at runtime when using Compass's Spring support

* velocity/velocity-1.4.jar
- Apache Jakarta Velocity (http://jakarta.apache.org/velocity)
- required for building compass
- required at runtime when using velocity for dynamic meta-data

* oracle/toplink-essentials
- Oracle Toplink Essential Reference implementation of JPA (part of glass fish) V2UR1_build_b09d
- required for building compass
- required at runtime when using Compass Gps JPA support with Toplink Essentials

* eclipselink/eclipselink
- Eclipselink 1.1.0.r3634
- required for building compass
- required at runtime when using Compass Gps JPA support with EclipseLink

* xpp/pull-parser-2.1.10.jar
- XPP2 (http://www.extreme.indiana.edu/xgws/xsoap/xpp/)
- required at runtime when using XPP2 based xml parsing with XSEM

* xpp/xpp3-1.1.3_8.jar
- XPP3 (http://www.extreme.indiana.edu/xgws/xsoap/xpp/)
- required at runtime when using XPP3 based xml parsing with XSEM

* gigaspaces/*

- GigaSpaces Community edition (http://www.gigaspaces.com) version 6.6.2
- required for building compass