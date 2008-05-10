<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>
<P>
<H2>Compass Index</H2>
<P>
Use the Index button to index the database using Compass::Gps. The operation will
delete the current index and reindex the database based on the mappings and devices
defined in the Compass::Gps configuration context.
<FORM method="POST" action="<c:url value="/index.htm"/>">
	<spring:bind path="command.doIndex">
		<INPUT type="hidden" name="doIndex" value="true" />
	</spring:bind>
    <INPUT type="submit" value="Index"/>
</FORM>
<c:if test="${! empty indexResults}">
	<P>Indexing took: <c:out value="${indexResults.indexTime}" />ms.
</c:if>
<P>
<BR>
<%@ include file="/WEB-INF/jsp/footer.jsp" %>