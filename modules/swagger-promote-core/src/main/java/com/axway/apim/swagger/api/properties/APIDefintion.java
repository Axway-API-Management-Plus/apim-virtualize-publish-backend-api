package com.axway.apim.swagger.api.properties;

import java.net.URL;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.Utils;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.state.DesiredAPI;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class APIDefintion {
	
	static Logger LOG = LoggerFactory.getLogger(APIDefintion.class);
	
	private String apiDefinitionFile = null;
	
	private byte[] apiDefinitionContent = null;

	private ObjectMapper objectMapper = null;
	
	public APIDefintion() {
		
	}
	
	private static YAMLFactory yamlFactory = null;
	
	private static YAMLFactory  getYamlFactory() {
		if (APIDefintion.yamlFactory == null) {
			APIDefintion.yamlFactory = new YAMLFactory();
		}
		return APIDefintion.yamlFactory;
	}

	public APIDefintion(byte[] apiDefinitionContent) {
		this.apiDefinitionContent = apiDefinitionContent;
	}

	public String getAPIDefinitionFile() {
		return apiDefinitionFile;
	}

	public void setAPIDefinitionFile(String apiDefinitionFile) {
		this.apiDefinitionFile = apiDefinitionFile;
	}

	
	private JsonNode parseContent(byte[] apiDefinitionContent) throws Exception {
		
		JsonNode swagger = null;
		ObjectMapper jsonObjectMapper = null;
		ObjectMapper yamlObjectMapper = null;
		
		try {
			jsonObjectMapper = new ObjectMapper();
			swagger = jsonObjectMapper.readTree(apiDefinitionContent);
		}
		catch (JsonParseException jsonException) {
			try {
				yamlObjectMapper = new ObjectMapper(APIDefintion.getYamlFactory());
				swagger = yamlObjectMapper.readTree(apiDefinitionContent);
			}
			catch (JsonParseException yamlException) {
				LOG.error("API definition file "  + (this.apiDefinitionFile  != null ? this.apiDefinitionFile : "<no name>" ) + 
						" could not be parsed. File is not a valid Json or Yaml file - see details below.");
				LOG.error("Json parse error: ", jsonException);
				LOG.error("Yaml parse error: ", yamlException);
				throw new Exception("Could not parse API definition file - see error messages for details");
			}
		}
		// Only use Yaml object mapper for writing changed API definition if we are at version 7.7 or higher.
		if (yamlObjectMapper != null && APIManagerAdapter.hasAPIManagerVersion("7.7")) {
			objectMapper = yamlObjectMapper;
		}
		else {
			objectMapper = jsonObjectMapper;
		}
		return swagger;
	}
	
	
	public void setAPIDefinitionContent(byte[] apiDefinitionContent, DesiredAPI importAPI) {
		this.apiDefinitionContent = apiDefinitionContent;
		try {
			if(CommandParameters.getInstance().replaceHostInSwagger() && getAPIDefinitionType()==IAPI.SWAGGGER_API) {
				if(importAPI.getBackendBasepath()!=null) {
					boolean backendBasepathAdjusted = false;
					URL url = new URL(importAPI.getBackendBasepath());
					String port = url.getPort()==-1 ? ":"+String.valueOf(url.getDefaultPort()) : ":"+String.valueOf(url.getPort());
					if(port.equals(":443") || port.equals(":80")) port = "";
					
					// parse content an initialize objectMapper variable
					JsonNode swagger = parseContent(apiDefinitionContent);
				  
					if(swagger.get("host")==null) {
						LOG.debug("Adding new host '"+url.getHost()+port+"' to Swagger-File based on configured backendBasepath: '"+importAPI.getBackendBasepath()+"'");
						backendBasepathAdjusted = true;
						((ObjectNode)swagger).put("host", url.getHost()+port);
					} else {
						if(swagger.get("host").asText().equals(url.getHost()+port)) {
							LOG.debug("Swagger Host: '"+swagger.get("host").asText()+"' already matches configured backendBasepath: '"+importAPI.getBackendBasepath()+"'. Nothing to do.");
						} else {
							LOG.debug("Replacing existing host: '"+swagger.get("host").asText()+"' in Swagger-File to '"+url.getHost()+port+"' based on configured backendBasepath: '"+importAPI.getBackendBasepath()+"'");
							backendBasepathAdjusted = true;
							((ObjectNode)swagger).put("host", url.getHost()+port);
						}
					}
					if(url.getPath()!=null && !url.getPath().equals("")) {
						if(swagger.get("basePath").asText().equals(url.getPath())) {
							LOG.debug("Swagger basePath: '"+swagger.get("basePath").asText()+"' already matches configured backendBasepath: '"+url.getPath()+"'. Nothing to do.");
						} else {
							LOG.debug("Replacing existing basePath: '"+swagger.get("basePath").asText()+"' in Swagger-File to '"+url.getPath()+"' based on configured backendBasepath: '"+importAPI.getBackendBasepath()+"'");
							backendBasepathAdjusted = true;
							((ObjectNode)swagger).put("basePath", url.getPath());
						}
					}
					 ArrayNode newSchemes = objectMapper.createArrayNode();
					 newSchemes.add(url.getProtocol());
					if(swagger.get("schemes")==null) {
						LOG.debug("Adding protocol: '"+url.getProtocol()+"' to Swagger-Definition");
						backendBasepathAdjusted = true;
						((ObjectNode)swagger).set("schemes", newSchemes);
					} else {
						if(swagger.get("schemes").size()!=1 || !swagger.get("schemes").get(0).asText().equals(url.getProtocol())) {
							LOG.debug("Replacing existing protocol(s): '"+swagger.get("schemes")+"' with '"+url.getProtocol()+"' according to configured backendBasePath: '"+importAPI.getBackendBasepath()+"'.");
							backendBasepathAdjusted = true;
							((ObjectNode)swagger).replace("schemes", newSchemes);
						}
					}
					if(backendBasepathAdjusted) {
						LOG.info("Used the configured backendBasepath: '"+importAPI.getBackendBasepath()+"' to adjust the Swagger definition.");
						this.apiDefinitionContent = objectMapper.writeValueAsBytes(swagger);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
		}
	}

	public byte[] getAPIDefinitionContent() {
		return apiDefinitionContent;
	}
	
	public int getAPIDefinitionType() throws AppException {
		String apiDefinitionSource = null;
		if(this.apiDefinitionFile.toLowerCase().endsWith(".url")) {
			apiDefinitionSource = Utils.getAPIDefinitionUriFromFile(this.apiDefinitionFile);
		} else {
			apiDefinitionSource = this.apiDefinitionFile;
		}
		if(apiDefinitionSource.toLowerCase().endsWith("?wsdl")) {
			return IAPI.WSDL_API;
		} else {
			return IAPI.SWAGGGER_API;
		}
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APIDefintion) {
			APIDefintion otherSwagger = (APIDefintion)other;
			boolean rc = (Arrays.hashCode(this.apiDefinitionContent)) == Arrays.hashCode(otherSwagger.getAPIDefinitionContent()); 
			if(!rc) {
				LOG.info("Detected API-Definition-Filesizes: API-Manager: " + this.apiDefinitionContent.length + " vs. Import: " + otherSwagger.getAPIDefinitionContent().length);
			}
			return rc;
		} else {
			return false;
		}
	}

}
