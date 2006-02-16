<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h2>Welcome</h2>

<p>
Welcome to the Compass Petclinic Sample. The sample shows how simple it is to add search capabilites to an application using Compass.

<h2>Working with Petclinic</h2>

<p>
If it is the first time that the sample is run, Go to the index page and index the application data (from the database). After indexing, the Search page should yield results for queries such as: vet, j*, and so on.

<p>
An interesting part of the sample is the fact that Petclinc will mirror any data changes back to the index. So changing a Pet or Vet will be reflected in the index (sometimes immediately - Hibernate and OJB, and sometimes after several seconds - Jdbc). Try and change a value, and see that a search for the new value will return the recently changed object.

<%@ include file="/WEB-INF/jsp/footer.jsp" %>