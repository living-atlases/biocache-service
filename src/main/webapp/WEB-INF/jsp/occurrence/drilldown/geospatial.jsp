<%@ include file="/common/taglibs.jsp"%>
<fieldset>
<p>	
	<label for="country"><spring:message code="occurrence.record.country"/>:</label> 
	<c:choose>
	<c:when test="${not empty occurrenceRecord.isoCountryCode}">
		<a href="${pageContext.request.contextPath}/countries/${occurrenceRecord.isoCountryCode}"><gbif:capitalize><spring:message code="country.${occurrenceRecord.isoCountryCode}"/></gbif:capitalize></a>
		<gbiftag:isCountryInferred issuesBit="${occurrenceRecord.otherIssue}"/>
		<c:if test="${countryInferred}">
			<spring:message code="occurrence.record.inferred.from.coordinates"/>
		</c:if>
	</c:when>
	<c:otherwise>
		${rawOccurrenceRecord.country}
	</c:otherwise>
	</c:choose>	
</p>

<c:if test="${not empty rawOccurrenceRecord.county}">
<p>	
	<label for="county"><spring:message code="occurrence.record.county"/>:</label> 
	${rawOccurrenceRecord.county}
</p>	
</c:if>

<p>
	<label for="stateOrProvince"><spring:message code="occurrence.record.state.or.province"/>:</label> 
	${rawOccurrenceRecord.stateOrProvince}
	
	<c:if test="${not empty state}">
	   (inferred from coordinates as <a href="${pageContext.request.contextPath}/regions/${state.id}">${state.name}</a>)
	</c:if>
</p>	
<p>
	<label for="locality"><spring:message code="occurrence.record.locality"/>:</label> 
	${rawOccurrenceRecord.locality}
	
	<c:if test="${not empty town || not empty city}">
       (inferred from coordinates as 
        <c:if test="${not empty town}"><a href="${pageContext.request.contextPath}/regions/${town.id}">${town.name}</a></c:if> 
        <c:if test="${not empty city}"><a href="${pageContext.request.contextPath}/regions/${city.id}">${city.name}</a></c:if>)
    </c:if>
</p>

<c:if test="${not empty shire}">
<p>
    <label for="ibra">Shire:</label> 
    <a href="${pageContext.request.contextPath}/regions/${shire.id}">${shire.name}</a>
</p>    
</c:if>

<c:if test="${not empty ibra}">
<p>
    <label for="ibra">Biogeographic region:</label> 
    <a href="${pageContext.request.contextPath}/regions/${bioregion.id}">${ibra.name}</a>
</p>    
</c:if>

<c:if test="${not empty imcra}">
<p>
    <label for="imcra">Marine region:</label> 
    <a href="${pageContext.request.contextPath}/regions/${imcra.id}">${imcra.name}</a>
</p>    
</c:if>

<c:if test="${not empty riverbasin}">
<p>
    <label for="imcra">River basin:</label> 
    <a href="${pageContext.request.contextPath}/regions/${riverbasin.id}"><gbif:capitalize>${riverbasin.name}</gbif:capitalize></a> 
</p>    
</c:if>

<p>
	<label for="latitude"><spring:message code="occurrence.record.geospatial.latitude"/>:</label> 
	${rawOccurrenceRecord.latitude}
</p>	
<p>
	<label for="longitude"><spring:message code="occurrence.record.geospatial.longitude"/>:</label> 
	${rawOccurrenceRecord.longitude}
</p>	
<p>
	<label for="latLongPrecision"><spring:message code="occurrence.record.geospatial.latLongPrecision"/>:</label> 
	${rawOccurrenceRecord.latLongPrecision}
</p>

<c:if test="${not empty rawOccurrenceRecord.minAltitude || not empty rawOccurrenceRecord.maxAltitude}">
<p>
	<label for="altitude"><spring:message code="occurrence.record.geospatial.altitude"/>:</label> 
	<c:if test="${not empty rawOccurrenceRecord.minAltitude}"><spring:message code="minimum"/> ${rawOccurrenceRecord.minAltitude}</c:if><c:if test="${not empty rawOccurrenceRecord.minAltitude && not empty rawOccurrenceRecord.maxAltitude}">,</c:if> 
	<c:if test="${not empty rawOccurrenceRecord.maxAltitude}"><spring:message code="maximum"/> ${rawOccurrenceRecord.maxAltitude}</c:if>
	<c:if test="${not empty rawOccurrenceRecord.altitudePrecision}"><spring:message code="occurrence.record.with.precision"/> ${rawOccurrenceRecord.altitudePrecision}</c:if>
	<c:if test="${not empty occurrenceRecord.altitudeInMetres}">
		<spring:message code="occurrence.record.interpreted.as" arguments="${occurrenceRecord.altitudeInMetres} metres" argumentSeparator="$$$$$"/>
	</c:if>
</p>
</c:if>	

<c:if test="${not empty rawOccurrenceRecord.minDepth || not empty rawOccurrenceRecord.maxDepth}">
<p>
	<label for="depth"><spring:message code="occurrence.record.geospatial.depth"/>:</label> 
	<c:if test="${not empty rawOccurrenceRecord.minDepth}"><spring:message code="minimum"/> ${rawOccurrenceRecord.minDepth}</c:if><c:if test="${not empty rawOccurrenceRecord.minDepth && not empty rawOccurrenceRecord.maxDepth}">,</c:if> 
	<c:if test="${not empty rawOccurrenceRecord.maxDepth}"><spring:message code="maximum"/> ${rawOccurrenceRecord.maxDepth}</c:if> 
	<c:if test="${not empty rawOccurrenceRecord.depthPrecision}"><spring:message code="occurrence.record.with.precision"/> ${rawOccurrenceRecord.depthPrecision}</c:if>
	<c:if test="${not empty occurrenceRecord.depthInMetres}">
		<spring:message code="occurrence.record.interpreted.as" arguments="${occurrenceRecord.depthInMetres} metres" argumentSeparator="$$$$$"/>
	</c:if>
</p>	
</c:if>

</fieldset>

<c:if test="${occurrenceRecord.latitude!=null && occurrenceRecord.longitude!=null && (occurrenceRecord.longitude!=0 && occurrenceRecord.latitude!=0) }">
	<c:set var="mapDivName" value="map" scope="request"/>
	<c:set var="pointsClickable" value="false" scope="request"/>
	<tiles:insert page="/WEB-INF/jsp/geography/googleMap.jsp"/>
	<a href="${pageContext.request.contextPath}/occurrences/${occurrenceRecord.key}/largeMap"><spring:message code="occurrence.record.view.large.map"/></a>
</c:if>