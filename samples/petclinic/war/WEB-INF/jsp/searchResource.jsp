<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<P>
<H2>Compass Search:</H2>

<FORM method="GET">
	<spring:bind path="command.query">
	 <INPUT type="text" size="20" name="query" value="<c:out value="${status.value}"/>" />
	</spring:bind>
  <INPUT type = "submit" value="Search"/>
</FORM>

<c:if test="${! empty searchResults}">

  Search took <c:out value="${searchResults.searchTime}" />ms

  <c:forEach var="hit" items="${searchResults.hits}">
   <c:choose>
     <c:when test="${hit.alias == 'pet'}">
       <P>
         <a href="<c:url value="/editPet.htm?petId=${hit.resource.ID[0].stringValue}"/>">
           <c:out value="${hit.resource.name[0].stringValue}" /> (Pet)
         </a>
         <BR>Birthdate: <fmt:formatDate value="${hit.resource.birthDate[0].stringValue}" pattern="yyyy-MM-dd"/>, 
         Type: <c:out value="${hit.data.petType[0].stringValue}" />
     </c:when>

     <c:when test="${hit.alias == 'owner'}">
       <P>
         <a href="<c:url value="/editOwner.htm?ownerId=${hit.resource.ID[0].stringValue}"/>">
           <c:out value="${hit.resource.firstName[0].stringValue}" /> <c:out value="${hit.resource.lastName[0].stringValue}" /> (Owner)
         </a>
         <BR>Name: <c:out value="${hit.resource.firstName[0].stringValue}" /> <c:out value="${hit.resource.lastName[0].stringValue}" />,
         Address: <c:out value="${hit.resource.address[0].stringValue}" />, City: <c:out value="${hit.resource.city[0].stringValue}" />,
         Telephone: <c:out value="${hit.resource.telephone[0].stringValue}" />
     </c:when>

     <c:when test="${hit.alias == 'vet'}">
       <P>
         <a href="#">
           <c:out value="${hit.resource.firstName[0].stringValue}" /> <c:out value="${hit.resource.lastName[0].stringValue}" /> (Vet)
         </a>
         <BR>Name: <c:out value="${hit.resource.firstName[0].stringValue}" /> <c:out value="${hit.resource.lastName[0].stringValue}" />
     </c:when>

     <c:when test="${hit.alias == 'visit'}">
       <P>
         <a href="#">
           <c:out value="${hit.resource.date[0].stringValue}" /> (Visit)
         </a>
         <BR>Description: <c:out value="${hit.resource.description[0].stringValue}" />
         <BR>Pet: <a href="<c:url value="/editPet.htm?petId=${hit.resource.pet_id[0].stringValue}"/>">
           <c:out value="${hit.resource.name[0].stringValue}" />
         </a>
     </c:when>

   </c:choose>

  </c:forEach>

  <c:if test="${! empty searchResults.pages}">

    <BR><BR><BR>
    <table><tr>
    <c:forEach var="page" items="${searchResults.pages}" varStatus="pagesStatus">
      <td>
      <c:choose>
        <c:when test="${page.selected}">
          <c:out value="${page.from}" />-<c:out value="${page.to}" />
        </c:when>
        <c:otherwise>
          <FORM method="GET">
            <spring:bind path="command.query">
               <INPUT type="hidden" name="query" value="<c:out value="${status.value}"/>" />
            </spring:bind>
            <spring:bind path="command.page">
               <INPUT type="hidden" name="page" value="<c:out value="${pagesStatus.index}"/>" />
            </spring:bind>
            <INPUT type = "submit" value="<c:out value="${page.from}" />-<c:out value="${page.to}" />"/>
          </FORM>
        </c:otherwise>
      </c:choose>
      </td>
    </c:forEach>
    </tr></table>

  </c:if>

</c:if>

<P>
<BR>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>