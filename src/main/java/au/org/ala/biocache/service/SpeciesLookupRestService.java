/**************************************************************************
 *  Copyright (C) 2013 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package au.org.ala.biocache.service;

import com.mockrunner.util.common.StringUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.web.client.RestOperations;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of SpeciesLookupService.java that calls the bie-service application
 * via JSON REST web services.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class SpeciesLookupRestService implements SpeciesLookupService {

    private final static Logger logger = Logger.getLogger(SpeciesLookupRestService.class);

    private RestOperations restTemplate; // NB MappingJacksonHttpMessageConverter() injected by Spring

    protected String bieUriPrefix;

    protected Boolean enabled;

    @Inject
    private AbstractMessageSource messageSource; // use for i18n of the headers

    private String[] baseHeader;
    private String[] countBaseHeader;
    private String[] synonymHeader;
    private String[] countSynonymHeader;

    /**
     * @see SpeciesLookupService#getGuidForName(String)
     *
     * @param name
     * @return guid
     */
    @Override
    public String getGuidForName(String name) {
        String guid = null;
        if(enabled){

            try {
                final String jsonUri = bieUriPrefix + "/guid/" + name;
                logger.info("Requesting: " + jsonUri);
                List<Object> jsonList = restTemplate.getForObject(jsonUri, List.class);

                if (!jsonList.isEmpty()) {
                    Map<String, String> jsonMap = (Map<String, String>) jsonList.get(0);
                    if (jsonMap.containsKey("acceptedIdentifier")) {
                        guid = jsonMap.get("acceptedIdentifier");
                    }
                }
            } catch (Exception ex) {
                logger.error("RestTemplate error: " + ex.getMessage(), ex);
            }
        }

        return guid;
    }

    /**
     * Lookup the accepted name for a GUID
     *
     * @return
     */
    @Override
    public String getAcceptedNameForGuid(String guid) {
        String acceptedName = "";
        if(enabled){
            try {
                final String jsonUri = bieUriPrefix + "/species/shortProfile/" + guid + ".json";
                logger.info("Requesting: " + jsonUri);
                Map<String, String> jsonMap = restTemplate.getForObject(jsonUri, Map.class);

                if (jsonMap.containsKey("scientificName")) {
                    acceptedName = jsonMap.get("scientificName");
                }

            } catch (Exception ex) {
                logger.error("RestTemplate error: " + ex.getMessage(), ex);
            }
        }

        return acceptedName;
    }


    /**
     *
     * @param guids
     * @return
     */
    @Override
    public List<String> getNamesForGuids(List<String> guids) {
        List<String> names = null;
        if(enabled){
            try {
                final String jsonUri = bieUriPrefix + "/species/namesFromGuids.json";
                String params = "?guid=" + StringUtils.join(guids, "&guid=");
                names = restTemplate.postForObject(jsonUri + params, null, List.class);
            } catch (Exception ex) {
                logger.error("Requested URI: " + bieUriPrefix + "/species/namesFromGuids.json");
                logger.error("With POST body: guid=" + StringUtils.join(guids, "&guid="));
                logger.error("RestTemplate error: " + ex.getMessage(), ex);
            }
        }

        return names;
    }

    private List<Map<String, String>> getNameDetailsForGuids(List<String> guids) {
        List<Map<String,String>> results =null;
        if(enabled){
            final String url = bieUriPrefix + "/species/guids/bulklookup.json";
            try{
                //String jsonString="";
                Map searchDTOList = restTemplate.postForObject(url, guids, Map.class);
                //System.out.println(test);
                results = (List<Map<String,String>>)searchDTOList.get("searchDTOList");
            } catch (Exception ex) {
                logger.error("Requested URI: " + url);
                logger.error("With POST body: guid=" + StringUtils.join(guids, "&guid="));
                logger.error("RestTemplate error: " + ex.getMessage(), ex);
            }
        }
        return results;
    }

    private Map<String, List<Map<String, String>>> getSynonymDetailsForGuids(
            List<String> guids) {
        Map<String,List<Map<String, String>>> results = null;
        if(enabled){
            final String url = bieUriPrefix + "/species/bulklookup/namesFromGuids.json";
            try{
                results = restTemplate.postForObject(url, guids, Map.class);
            } catch(Exception ex){
                logger.error("Requested URI: " + url);
                logger.error("With POST body: guid=" + StringUtils.join(guids, "&guid="));
                logger.error("RestTemplate error: " + ex.getMessage(), ex);
            }
        }
        return results;
    }

    @Override
    public List<String[]> getSpeciesDetails(List<String> guids,List<Long> counts, boolean includeCounts, boolean includeSynonyms){
        List<String[]> details= new  java.util.ArrayList<String[]>(guids.size());
        List<Map<String,String>> values = getNameDetailsForGuids(guids);
        Map<String,List<Map<String, String>>> synonyms = includeSynonyms? getSynonymDetailsForGuids(guids):new HashMap<String,List<Map<String,String>>>();
        int size = includeSynonyms && includeCounts ? 13 : ((includeCounts && !includeSynonyms) || (includeSynonyms && !includeCounts)) ? 12: 11;

        //case names_and_lsid: sciName + "|" + taxonConceptId + "|" + vernacularName + "|" + kingdom + "|" + family
        //rebuild values using taxonConceptIds
        if (values != null && values.get(0) == null
                && guids.size() > 0 && StringUtil.countMatches(guids.get(0), "|") == 4) {
            List<String> taxonConceptIds = new ArrayList(guids.size());
            for(String s : guids) {
                if (s != null) {
                    if (s.startsWith("\"") && s.endsWith("\"") && s.length() > 2) s = s.substring(1, s.length() - 1);
                    String[] split = s.split("\\|", 6);
                    if (split.length == 5) {
                        taxonConceptIds.add(split[1]);
                    } else {
                        taxonConceptIds.add("");
                    }
                }
            }
            values = getNameDetailsForGuids(taxonConceptIds);
        }

        for(int i =0 ; i<guids.size();i++){
            int countIdx = 11;
            String[] row = new String[size];
            //guid
            String guid = guids.get(i);
            row[0]=guid;
            if(values!= null && synonyms != null){
                Map<String,String> map = values.get(i);
                if(map!=null){
                    //scientific name
                    row[1]=map.get("nameComplete");
                    row[2]=map.get("author");
                    row[3]=map.get("rank");
                    row[4]=map.get("kingdom");
                    row[5]=map.get("phylum");
                    row[6]=map.get("classs");
                    row[7]=map.get("order");
                    row[8]=map.get("family");
                    row[9]=map.get("genus");
                    row[10]=map.get("commonNameSingle");
                } else if (StringUtil.countMatches(guid, "|") == 4){
                    //not matched and is like names_and_lsid: sciName + "|" + taxonConceptId + "|" + vernacularName + "|" + kingdom + "|" + family
                    if (guid.startsWith("\"") && guid.endsWith("\"") && guid.length() > 2) guid = guid.substring(1, guid.length() - 1);
                    String [] split = guid.split("\\|", 6);
                    row[0] = guid;
                    row[1] = split[0];
                    row[2] = "";
                    row[3] = "";
                    row[4] = split[3];
                    row[5] = "";
                    row[6] = "";
                    row[7] = "";
                    row[8] = split[4];
                    row[9] = "";
                    row[10] = split[2];
                }

                if(includeSynonyms){
                    //retrieve a list of the synonyms
                    List<Map<String,String>> names =synonyms.get(row[0]);
                    StringBuilder sb =new StringBuilder();
                    for(Map<String,String> n :names){
                        if(!guid.equals(n.get("guid"))){
                            if(sb.length()>0){
                                sb.append(",");
                            }
                            sb.append(n.get("name"));
                        }
                    }
                    row[11] = sb.toString();
                    countIdx = 12;
                }
            }
            if(includeCounts){
                row[countIdx] = counts.get(i).toString();
            }
            details.add(row);
        }

        return details;
    }

    @Override
    public String[] getHeaderDetails(String field,boolean includeCounts, boolean includeSynonyms){
        if(baseHeader == null){
            //initialise all the headers
            initHeaders();
        }
        String[] startArray = baseHeader;
        if(includeCounts){
            if(includeSynonyms){
                startArray = countSynonymHeader;
            } else{
                startArray = countBaseHeader;
            }
        } else if(includeSynonyms){
            startArray = synonymHeader;
        }
        return (String[])ArrayUtils.add(startArray, 0, messageSource.getMessage("facet."+field, null, field,null));
    }

    /**
     * initialise the common header components that will be added to the the supplied header field.
     */
    private void initHeaders(){
        baseHeader = new String[]{messageSource.getMessage("species.name", null,"Species Name", null),
                messageSource.getMessage("species.author", null,"Scientific Name Author", null),
                messageSource.getMessage("species.rank", null,"Taxon Rank", null),
                messageSource.getMessage("species.kingdom", null,"Kingdom", null),
                messageSource.getMessage("species.phylum", null,"Phylum", null),
                messageSource.getMessage("species.class", null,"Class", null),
                messageSource.getMessage("species.order", null,"Order", null),
                messageSource.getMessage("species.family", null,"Family", null),
                messageSource.getMessage("species.genus", null,"Genus", null),
                messageSource.getMessage("species.common", null,"Vernacular Name", null)};
        countBaseHeader = (String[]) ArrayUtils.add(baseHeader,messageSource.getMessage("species.count", null,"Number of Records", null));
        synonymHeader = (String[]) ArrayUtils.add(baseHeader,messageSource.getMessage("species.synonyms", null,"Synonyms", null));
        countSynonymHeader = (String[]) ArrayUtils.add(synonymHeader,messageSource.getMessage("species.count", null,"Number of Records", null));
    }

    public void setBieUriPrefix(String bieUriPrefix) {
        this.bieUriPrefix = bieUriPrefix;
    }

    public void setRestTemplate(RestOperations restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setMessageSource(AbstractMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public List<String> getGuidsForTaxa(List<String> taxaQueries) {
        List guids = new ArrayList<String>();

        StringBuilder encodedQueries = new StringBuilder();

        if (taxaQueries.size() == 1) {
            String taxaQ = !taxaQueries.get(0).isEmpty() ? taxaQueries.get(0) : "*:*"; // empty taxa search returns all records
            for (String tq : taxaQ.split(" OR ")) {
                try {
                    if (encodedQueries.length() > 0) encodedQueries.append("&q=");
                    encodedQueries.append(URLEncoder.encode(tq, "UTF-8"));
                    taxaQueries.add(tq);
                } catch (UnsupportedEncodingException e) {
                    logger.error("failed encoding " + tq, e);
                }
            }
            taxaQueries.remove(0); // remove first entry
        }

        String url = bieUriPrefix + "/guid/batch?q=" + encodedQueries.toString();
        logger.info("Requesting: " + url);
        Map<String, Object> jsonMap = restTemplate.getForObject(url, Map.class);

        for (Object o : jsonMap.values()) {
            if (((List) o).size() > 0) {
                Map m = (Map) ((List) o).get(0);
                if (m.containsKey("acceptedIdentifier")) {
                    guids.add(m.get("acceptedIdentifier"));
                } else {
                    guids.add(null);
                }
            } else {
                guids.add(null);
            }
        }

        return guids;
    }

    public Map search(String query, String [] filterQuery, int max, boolean includeSynonyms, boolean includeAll, boolean counts) {
        String url = bieUriPrefix + "/ws/search.json?q=" + query + "&pageSize=" + max;
        logger.info("Requesting: " + url);
        Map<String, Object> jsonMap = restTemplate.getForObject(url, Map.class);

        return jsonMap;
    }
}
