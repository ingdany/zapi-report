package zephyr

import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows

import internal.GlobalVariable

import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URISyntaxException

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.ParseException
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.StringEntity
//import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

import com.thed.zephyr.cloud.rest.ZFJCloudRestClient
import com.thed.zephyr.cloud.rest.client.JwtGenerator

public class TokenGenerator {

	@Keyword
	public String generate(String apiUrl, String apiType) {
		try {
			def zephyrBaseUrl = "https://prod-api.zephyr4jiracloud.com/connect"
			def zapiAccessKey = "amlyYTpmMjhjNTAxNy0yZjJkLTQ0NGItYmFlZS0xMDBiYmI2YmZjYzQgNWYzZjFhNTFkMzc5NmUwMDQ2MzQxNmU1IFVTRVJfREVGQVVMVF9OQU1F"
			def secretKey = "9EvrJ6bgB6G-R2FOl1sHWPwFA_RagRDHGqcsA0kTyR8"
			def userName = "dperez@biosoftinc.com"

			ZFJCloudRestClient client = ZFJCloudRestClient.restBuilder(zephyrBaseUrl, zapiAccessKey, secretKey, userName).build();
			JwtGenerator jwtGenerator = client.getJwtGenerator();

			String createCycleUri = zephyrBaseUrl + apiUrl;

			URI uri = new URI(createCycleUri);
			//log.info(uri.toString())

			int expirationInSec = 3600;
			String jwt = jwtGenerator.generateJWT(apiType, uri, expirationInSec);

			return jwt;
		}
		catch (Exception ex) {
			println("We are not able to generate token, please review API Url or API Type")
		}
	}

}
