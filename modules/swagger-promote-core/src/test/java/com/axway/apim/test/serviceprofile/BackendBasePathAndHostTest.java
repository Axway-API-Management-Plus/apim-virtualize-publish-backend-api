package com.axway.apim.test.serviceprofile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.properties.APIDefintion;
import com.axway.apim.swagger.api.state.DesiredAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class BackendBasePathAndHostTest {
	
	@BeforeClass
	private void initTestIndicator() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("replaceHostInSwagger", "true");
		new CommandParameters(params);
	}
	
	@Test
	public void backendHostAndBasePath() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("teststore.json");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore.json"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);
		
		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/api/v1/myAPI");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void backendHostAndBasePathYaml() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://myhost.customer.com:8767/api/v1/myAPI.yaml");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("petstore.yaml");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore.yaml"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);
		
		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/api/v1/myAPI.yaml");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}

	
	@Test
	public void backendHostOnly() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("http://myhost.customer.com:8767");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("teststore.json");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore.json"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/v2");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "http");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}

	@Test
	public void backendHostOnlyYaml() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("http://myhost.customer.com:8767");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("petstore.yaml");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore.yaml"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/v2.yaml");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "http");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}

	
	@Test
	public void backendHostBasisBasePath() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://myhost.customer.com/");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("teststore.json");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore.json"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com");
		Assert.assertEquals(swagger.get("basePath").asText(), "/");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void backendHostBasisBasePathYaml() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://yaml.myhost.customer.com/");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("petstore.yaml");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore.yaml"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "yaml.myhost.customer.com");
		Assert.assertEquals(swagger.get("basePath").asText(), "/");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}

	
	
	@Test
	public void swaggerWithoutSchemes() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://myhost.customer.com/");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("teststore.json");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore-without-schemes.json"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com");
		Assert.assertEquals(swagger.get("basePath").asText(), "/");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void swaggerWithoutSchemesYaml() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://yaml.myhost.customer.com/");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("petstore-without-schemes.yaml");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore-without-schemes.yaml"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "yaml.myhost.customer.com");
		Assert.assertEquals(swagger.get("basePath").asText(), "/");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void backendBasepathChangesNothing() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://petstore.swagger.io");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("teststore.json");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore-only-https-scheme.json"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "petstore.swagger.io");
		Assert.assertEquals(swagger.get("basePath").asText(), "/v2");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	

	@Test
	public void backendBasepathChangesNothingYaml() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setBackendBasepath("https://yaml.petstore.swagger.io");
		APIDefintion apiDefinition = new APIDefintion();
		apiDefinition.setAPIDefinitionFile("petstore-only-https-scheme.yaml");
		apiDefinition.setAPIDefinitionContent(getSwaggerContent("/api_definition_1/petstore-only-https-scheme.yaml"), testAPI);
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getAPIDefinitionContent());
		Assert.assertEquals(swagger.get("host").asText(), "yaml.petstore.swagger.io");
		Assert.assertEquals(swagger.get("basePath").asText(), "/v2.yaml");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}

	
	private byte[] getSwaggerContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
