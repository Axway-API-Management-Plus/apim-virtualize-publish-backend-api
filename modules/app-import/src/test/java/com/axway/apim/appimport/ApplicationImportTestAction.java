package com.axway.apim.appimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;

public class ApplicationImportTestAction extends AbstractTestAction {
	
	private static Logger LOG = LoggerFactory.getLogger(ApplicationImportTestAction.class);
	
	public static String CONFIG = "config";
	
	File testDir = null;
	
	@Override
	public void doExecute(TestContext context) {
		String origConfigFile 		= context.getVariable(CONFIG);
		String stage				= null;
		boolean useEnvironmentOnly	= false;
		testDir = createTestDirectory(context);
		try {
			stage 				= context.getVariable("stage");
		} catch (CitrusRuntimeException ignore) {};

		String configFile = replaceDynamicContentInFile(origConfigFile, context, createTempFilename(origConfigFile));
		LOG.info("Using Replaced configFile-File: " + configFile);
		//LOG.info("API-Manager import is using user: '"+context.replaceDynamicContentInString("${oadminUsername1}")+"'");
		int expectedReturnCode = 0;
		try {
			expectedReturnCode 	= Integer.parseInt(context.getVariable("expectedReturnCode"));
		} catch (Exception ignore) {};
		
		try {
			useEnvironmentOnly 	= Boolean.parseBoolean(context.getVariable("useEnvironmentOnly"));
		} catch (Exception ignore) {};
		
		String enforce = "false";
		
		try {
			enforce = context.getVariable("enforce");
		} catch (Exception ignore) {};
		
		
		if(stage==null) {
			stage = "NOT_SET";
		} else {
			// We need to prepare the dynamic staging file used during the test.
			String stageConfigFile = origConfigFile.substring(0, origConfigFile.lastIndexOf(".")+1) + stage + origConfigFile.substring(origConfigFile.lastIndexOf("."));
			String replacedStagedConfig = configFile.substring(0, configFile.lastIndexOf("."))+"."+stage+".json";
			// This creates the dynamic staging config file! (For testing, we also support reading out of a file directly)
			replaceDynamicContentInFile(stageConfigFile, context, replacedStagedConfig);
		}
		
		String[] args;
		if(useEnvironmentOnly) {
			args = new String[] {  
					"-c", configFile, "-s", stage};
		} else {
			args = new String[] { 
					"-c", configFile, 
					"-h", context.replaceDynamicContentInString("${apiManagerHost}"), 
					"-u", context.replaceDynamicContentInString("${oadminUsername1}"), 
					"-p", context.replaceDynamicContentInString("${oadminPassword1}"),
					"-s", stage,
					"-f", enforce
			};
		}
		//LOG.info("Ignoring admin account: '"+ignoreAdminAccount+"' | Enforce breaking change: " + enforce + " | useEnvironmentOnly: " + useEnvironmentOnly);
		int rc = ClientApplicationImportApp.importApp(args);
		if(expectedReturnCode!=rc) {
			throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
		}
	}
	
	/**
	 * To make testing easier we allow reading test-files from classpath as well
	 */
	private String replaceDynamicContentInFile(String pathToFile, TestContext context, String replacedFilename) {
		
		File inputFile = new File(pathToFile);
		InputStream is = null;
		OutputStream os = null;
		try {
			if(inputFile.exists()) { 
				is = new FileInputStream(pathToFile);
			} else {
				is = this.getClass().getResourceAsStream(pathToFile);
			}
			if(is == null) {
				throw new IOException("Unable to read swagger file from: " + pathToFile);
			}
			String jsonData = IOUtils.toString(is, StandardCharsets.UTF_8);
			//String filename = pathToFile.substring(pathToFile.lastIndexOf("/")+1); // e.g.: petstore.json, no-change-xyz-config.<stage>.json, 

			String jsonReplaced = context.replaceDynamicContentInString(jsonData);

			os = new FileOutputStream(new File(replacedFilename));
			IOUtils.write(jsonReplaced, os, StandardCharsets.UTF_8);
			
			return replacedFilename;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(os!=null)
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return null;
	}
	
	private String createTempFilename(String origFilename) {
		String prefix = origFilename.substring(0, origFilename.indexOf(".")+1);
		String suffix = origFilename.substring(origFilename.indexOf("."));
		try {
			File tempFile = File.createTempFile(prefix, suffix, testDir);
			tempFile.deleteOnExit();
			return tempFile.getAbsolutePath();
		} catch (IOException e) {
			LOG.error("Cant create temp file", e);
			throw new RuntimeException(e);
		}
	}
	
	private File createTestDirectory(TestContext context) {
		int randomNum = ThreadLocalRandom.current().nextInt(1, 9999 + 1);
		String appName = context.getVariable("appName");
		String testDirName = "AppImportActionTest-" + appName.replace(" ", "") + "-" + randomNum;
		String tmpDir = System.getProperty("java.io.tmpdir");
		File testDir = new File(tmpDir + File.separator + testDirName);
		if(!testDir.mkdir()) {
			randomNum = ThreadLocalRandom.current().nextInt(1, 9999 + 1);
			testDirName = "AppImportActionTest-" + appName.replace(" ", "") + "-" + randomNum;
			testDir = new File(tmpDir + File.separator + testDirName);
			if(!testDir.mkdir()) {
				throw new RuntimeException("Failed to create Test-Directory: " + tmpDir + File.separator + testDirName);
			}
		}
		LOG.info("Successfully created Test-Directory: "+tmpDir + File.separator + testDirName);
		return testDir;
	}
}