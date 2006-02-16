<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h1>Petclinic Tutorial</h1>

<p>
The petclinic sample is the Spring petclinic sample powered by Compass. For a complete overview of the sample, please read the Petclinic Sample chapter in the Compass documentation.
<p>
The <a href="<c:url value="/spring-tutorial.htm"/>">Spring Tutorial</a> explains how the system is built using Spring and the different frameworks. The following section will explain the different configuration of the sample.

<h2>Hibernate</h2>

<p>
In order to run the sample with Hibernate, the contextConfigLocation entry in the web.xml should have the /WEB-INF/applicationContext-compass.xml and /WEB-INF/applicationContext-hibernate.xml. The searchView.url and the searchResultsView.url in the WEB-INF/classes/views.properties file should point to the search.jsp file.

<h2>Apache OJB</h2>

<p>
In order to run the sample with OJB, the contextConfigLocation entry in the web.xml should have the /WEB-INF/applicationContext-compass.xml and /WEB-INF/applicationContext-ojb.xml. The searchView.url and the searchResultsView.url in the WEB-INF/classes/views.properties file should point to the search.jsp file.

<h2>JDBC</h2>

<p>
In order to run the sample with Jdbc, the contextConfigLocation entry in the web.xml should have the /WEB-INF/applicationContext-jdbc.xml. The searchView.url and the searchResultsView.url in the WEB-INF/classes/views.properties file should point to the searchResource.jsp file.

<p>
Note that the Jdbc sample currently runs only the Hsql database that comes with the sample.

<%@ include file="/WEB-INF/jsp/footer.jsp" %>

