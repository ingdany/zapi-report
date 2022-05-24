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
import com.kms.katalon.core.testobject.ConditionType as ConditionType
import com.kms.katalon.core.testobject.TestObjectProperty as TestObjectProperty
import com.kms.katalon.core.testobject.RequestObject as RequestObject
import com.kms.katalon.core.testobject.ResponseObject as ResponseObject
import groovy.json.JsonSlurper
import com.kms.katalon.core.testobject.HttpBodyContent as HttpBodyContent
import com.kms.katalon.core.testobject.impl.HttpTextBodyContent as HttpTextBodyContent
import org.assertj.core.api.Assertions as Assertions
import groovy.json.JsonOutput
import groovy.json.*

public class EndPoints {

	public Map connectUrl(String restUrl) {
		try {
			def site = "https://prod-api.zephyr4jiracloud.com/connect"
			def content = "text/plain"
			def contentType = new TestObjectProperty("Content-Type", ConditionType.EQUALS,content)
			def acceptHeader = new TestObjectProperty("Accept", ConditionType.EQUALS, "application/json")
			def accessKey = "amlyYTpmMjhjNTAxNy0yZjJkLTQ0NGItYmFlZS0xMDBiYmI2YmZjYzQgNWYzZjFhNTFkMzc5NmUwMDQ2MzQxNmU1IFVTRVJfREVGQVVMVF9OQU1F"
			def zapiAccessKey = new TestObjectProperty("zapiAccessKey", ConditionType.EQUALS, accessKey)
			def url = site + restUrl
			TokenGenerator tokenClass = new TokenGenerator()
			def apiType = "GET"
			def token = tokenClass.generate(restUrl, apiType)
			def authorization = new TestObjectProperty("Authorization", ConditionType.EQUALS, token)
			def zapiRequestObject = new RequestObject("zapi")
			zapiRequestObject.setHttpHeaderProperties(Arrays.asList(contentType, acceptHeader, authorization, zapiAccessKey))
			zapiRequestObject.setRestUrl(url)
			zapiRequestObject.setRestRequestMethod(apiType)
			def zapiResponseObject = WS.sendRequest(zapiRequestObject)
			JsonSlurper slurper = new JsonSlurper()
			Map result = slurper.parseText(zapiResponseObject.getResponseBodyContent())
			return result
		}
		catch (Exception ex) {
			println("Can't connect to the Server")
		}
	}

	@Keyword
	public Map getExecutionByCycle(String CycleName) {
		// Get ProjectId and VersionId
		def versionId, projectId, cycleId
		//TokenGenerator tokenClass = new TokenGenerator()
		Map result1 = connectUrl("/public/rest/api/1.0/zql/fields/values")
		def fields = result1.get("fields")
		def cycles = fields.get("cycleName")
		for (def cycleLine : cycles) {
			if (cycleLine.name == CycleName) {
				cycleId = cycleLine.id
				versionId = cycleLine.versionId
				projectId = cycleLine.projectId
			}
		}
		// Get List of Executions by Cycle
		def result2 = connectUrl("/public/rest/api/2.0/executions/search/cycle/"+cycleId+"?versionId="+versionId+"&projectId="+projectId)
		def result3 = result2.get("searchResult")
		def result4 = result3.get("searchObjectList")
		Map<String, List<String>> executionsMap = new LinkedHashMap<>()
		List<String> executionsList
		for (def line : result4) {
			executionsList = executionsMap.get(line.execution.id)
			executionsList = new LinkedList<>()
			executionsList.add(line.execution.issueId)
			executionsList.add(line.issueKey)
			executionsList.add(projectId)
			executionsMap.put(line.execution.id, executionsList)
		}
		return executionsMap
	}

	@Keyword
	public Map getExecution(String executionId, int issueId, int projectId) {
		def result = connectUrl("/public/rest/api/1.0/execution/"+executionId+"?issueId="+issueId+"&projectId="+projectId)
		def result2 = result.get("execution")
		def result3 = result2.find{ it.key == "execution"}.value
		def result4 = result2.find{ it.key == "issueSummary"}.value
		def result5 = result2.find{ it.key == "issueKey"}.value
		Map<String, List<String>> executionMap = new LinkedHashMap<>()
		List<String> executionList
		executionList = executionMap.get(result3.id)
		executionList = new LinkedList<>()
		executionList.add(result5)
		executionList.add(result4)
		executionList.add(result3.status.name)
		executionList.add(result3.defects.key)
		executionList.add(result3.executedBy)
		executionMap.put(result3.id, executionList)
		return executionMap
	}

	@Keyword
	public String getAssigneeName(String userId) {
		Map result1 = connectUrl("/public/rest/api/1.0/zql/fields/values")
		def fields = result1.get("fields")
		def users  = fields.get("executedByAccountId")
		def userName = ""
		for (def user : users) {
			if (user.id == userId) {
				userName = user.fullName

			}
		}
		return userName
	}

	@Keyword
	public Map getTestStep(int issueId, int projectId) {
		def result = connectUrl("/public/rest/api/2.0/teststep/"+issueId+"?projectId="+projectId)
		def result2 = result.get("testSteps")
		Map<String, List<String>> testStepMap = new LinkedHashMap<>()
		List<String> testStepList
		for (def line : result2) {
			testStepList = testStepMap.get(line.id)
			testStepList = new LinkedList<>()
			testStepList.add(line.orderId)
			testStepList.add(line.step)
			testStepList.add(line.data)
			testStepList.add(line.result)
			testStepMap.put(line.id, testStepList)
		}
		return testStepMap
	}

	@Keyword
	public int getStepCount(int issueId, int projectId) {
		def result = connectUrl("/public/rest/api/2.0/teststep/"+issueId+"?projectId="+projectId)
		def result2 = result.find{ it.key == "totalCount"}.value
		return result2
	}

	@Keyword
	public Map getStepResult(String executionId,int issueId) {
		Map result = connectUrl("/public/rest/api/1.0/stepresult/search?executionId="+executionId+"&issueId="+issueId+"&isOrdered=1")
		def stepResults = result.get("stepResults")
		Map <String, List<String>> stepResultsMap = new LinkedHashMap<>()
		List<String> stepResultList
		for (def line : stepResults) {
			stepResultList = stepResultsMap.get(line.stepId)
			stepResultList = new LinkedList<>()
			stepResultList.add(line.comment)
			stepResultList.add(line.status.name)
			stepResultsMap.put(line.stepId, stepResultList)
		}
		return stepResultsMap
	}
}
