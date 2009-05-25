COMPASS, Release 2.3.0 beta 1
-----------------------------
http://www.compass-project.org


1. INTRODUCTION

Compass is a powerful, transactional Object to Search Engine Mapping (OSEM) Java framework. 
Compass allows you to declaratively map your Object domain model to the underlying Search Engine, 
synchronizing data changes between Index and different datasources. Compass provides a high level 
abstraction on top of the Lucene low level API. Compass also implements fast index operations and 
optimization and introduces transaction capabilities to the Search Engine.

2. RELEASE INFO

Compass requires J2SE 1.4 and (optionally) J2EE 1.3 (Servlet 2.3, JSP 1.2, JTA 1.0, EJB 2.0). 
JDK 1.5 is required for building Compass.

Integration is provided with Log4J 1.2, Hibernate 2.1/3.0/3.1, JDO 1.0/2.0, JPA 1.0, Apache OJB 1.0, 
iBATIS SQL Maps 2.0/2.1, Spring 1.2/2.0 .

Release contents:
* "src" contains the Java source and test files for Compass
* "dist" contains various Compass distribution jar files
* "lib" contains all third-party libraries needed for running the samples and/or building compass
* "docs" contains general documentation and API javadocs
* "samples" contains demo applications and skeletons

The "lib" directory is just included in the "-with-dependencies" download. Make sure to download this full
distribution ZIP file if you want to run the sample applications and/or build compass yourself.
Ant build scripts for compass and the samples are provided. The standard samples can be built with
the included Ant runtime by invoking the corresponding "build.bat" files (see samples subdirectories).

Latest info is available at the public website: http://www.opensymphony.com/compass

Compass is released under the terms of the Apache Software License (see license.txt).
All libraries included in the "-with-dependencies" download are subject to their respective licenses.
This product includes software developed by the Apache Software Foundation (http://www.apache.org).
This product includes software developed by Clinton Begin (http://www.ibatis.com).


3. DISTRIBUTION JAR FILES

The "dist" directory contains the following distinct jar files for use in applications. The "compass.jar"
is the full Java 1.5 compass jar. The "compass14.jar" is a retro translated jar file supporting Java 1.4. 

4. WHERE TO START?

Documentation can be found in the "docs" directory:
* the Compass reference documentation

Documented sample applications and skeletons can be found in "samples":
* library
* petclinic
