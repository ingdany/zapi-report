package pacer

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
import zephyr.EndPoints

import net.java.textilej.parser.MarkupParser
import net.java.textilej.parser.builder.HtmlDocumentBuilder
import net.java.textilej.parser.markup.mediawiki.MediaWikiDialect
import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.parser.ParserDelegator
import java.io.StringReader
import java.io.StringWriter


public class Summary {

	@Keyword
	public void getReport(String sourcePath, String fileName, String cycleName) {
		String htm_file = sourcePath + fileName + ".htm";
		String title = "ZFJ - Html Report for Executions"
		FileWriter writer = null;
		String doctype = "<!DOCTYPE HTML>";
		String html_start = "\n<HTML>";
		String header = addHeader();
		String body_start = "<body><table class='altrowstable' id='alternatecolor' border='1' cellspacing='0' cellpadding='0' style='padding-left : 50 px ; overflow-y = scroll ;'>";
		String thead = "<thead><th>Issue Key</th><th>Test Summary</th><th>Execution Status</th><th>Execution Defect(s)</th><th>Assigned To</th><th>OrderId</th><th>Step</th><th>Test Data</th><th>Expected Result</th><th>Step Result</th><th>Comment</th><th>Test Step Custom Field</th></thead>";
		String tbody = "<tbody>" + addExecutions(cycleName) + "</tbody>";
		String body_end = "</body>";
		String html_end = "</HTML>";
		writer = new FileWriter(htm_file, false);
		writer.write(doctype);
		writer.write(html_start);
		writer.write(header);
		writer.write(body_start);
		writer.write(thead);
		writer.write(tbody);
		writer.write(body_end);
		writer.write(html_end);
		writer.close();
	}

	private String addHeader() {
		String header = """
<head>
        <meta charset='UTF-8'>
        <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>
        <title>ZFJ - Html Report for Executions</title>
        <script type='text/javascript'>
        function altRows ( id ) {
            if ( document.getElementsByTagName ) {
                var table = document.getElementById ( id ) ;
                var rows = table.getElementsByTagName ( 'tr' ) ;
                for ( i = 0 ; i < rows.length ; i ++ ) {
                    if ( i % 2 == 0 ) {
                        rows[ i ].className = 'evenrowcolor' ;
                    } else {
                        rows[ i ].className = 'oddrowcolor' ;
                    }
                }
            }
        }
        window.onload = function ( ) {
            altRows ( 'alternatecolor' ) ;
        }
        </script>
        <style type='text/css'>
        table.altrowstable {
            font-family : verdana, arial, sans-serif ;
            font-size : 9px ;
            padding-left : 50px ;
            color : #333333 ;
            border-width : 1px ;
            border-color : #a9c6c9 ;
            border-collapse : collapse ;
        }
        table.altrowstable th {
            border-width : 1px ;
            padding : 8px ;
            border-style : solid ;
            border-color : #a9c6c9 ;
        }
        table.altrowstable thead {
            font-weight : bold ;
        }
        table.altrowstable td {
            border-width : 1px ;
            padding : 8px ;
            border-style : solid ;
            border-color : #a9c6c9 ;
        }
        .oddrowcolor {
            background-color : #d4e3e5 ;
        }
        .evenrowcolor {
            background-color : none ;
        }
        </style>
    </head>
"""
	}

	private String addExecutions(String cycleName) {
		EndPoints zapiConnection = new EndPoints()
		def executions = zapiConnection.getExecutionByCycle(cycleName);
		def execution_row = ""
		def execution_rows = ""
		def executionId, issueId, project, total_count, execution_details, test_step_details, step_result_details, test_step_row, test_step_rows, step_result_row, step_result_rows,step_result,comment_result,issueKey,
		test_summary,execution_status, execution_defects, assigned_to, assigned_names, order_id, step, test_data, expected_result, defects, step_markup, expected_result_markup, comment_result_markup;



		for (def execution : executions) {
			executionId = execution.key
			issueId = execution.value[0]
			project = execution.value[2]
			total_count = zapiConnection.getStepCount(issueId, project)
			execution_details = zapiConnection.getExecution(executionId, issueId, project)
			test_step_details = zapiConnection.getTestStep(issueId, project)
			step_result_details = zapiConnection.getStepResult(executionId, issueId)
			for (def execution_detail : execution_details) {
				test_step_rows = ""
				test_step_row = ""
				step_result_rows = ""
				step_result_row = ""
				issueKey = execution_detail.value[0].toString().toUpperCase()
				test_summary = execution_detail.value[1]
				execution_status = execution_detail.value[2]
				execution_defects = execution_detail.value[3]
				println(execution_defects.getClass())
				defects = (execution_defects.size == 0) ? "" : execution_defects				
				assigned_to = execution_detail.value[4]
				assigned_names = zapiConnection.getAssigneeName(assigned_to)
				execution_row += "<tr><td rowspan=\"" + total_count + "\">" + issueKey + "</td><td rowspan=\"" + total_count  + "\">" + test_summary + "</td><td rowspan=\"" + total_count + "\">" +
						execution_status + "</td><td rowspan=\"" + total_count + "\">" +  defects + "</td><td rowspan=\"" + total_count + "\">" + assigned_names + "</td>";
			}
			for (def test_step_detail : test_step_details) {
				order_id = test_step_detail.value[0]
				step_markup = test_step_detail.value[1]
				step = WikiToHTML(step_markup)
				test_data = test_step_detail.value[2]
				expected_result_markup = test_step_detail.value[3]
				expected_result = WikiToHTML(expected_result_markup)
				test_step_row = test_step_row == "" ? "<td>" + order_id + "</td><td>" + step + "</td><td>" + test_data + "</td><td>" + expected_result +
						"</td>" : "<tr><td>" + order_id + "</td><td>" +  step + "</td><td>" + test_data + "</td><td>" + expected_result + "</td>";
				step_result = step_result_details.find{ it.key == test_step_detail.key }?.value[1]
				comment_result_markup = step_result_details.find{ it.key == test_step_detail.key }?.value[0]
				comment_result = comment_result_markup == null ? " " : WikiToHTML(comment_result_markup)
				step_result_row = "<td>"+step_result+"</td><td>"+comment_result+"</td><td> </td></tr>"
				test_step_rows = test_step_rows + test_step_row + step_result_row
			}
			execution_row = execution_row + test_step_rows
		}
		return execution_row;
	}

	private String WikiToHTML(String markup_text) {
		def html_text, html_text2
		StringWriter writer = new StringWriter()
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer)
		builder.setEmitAsDocument(false)
		MarkupParser parser = new MarkupParser(new MediaWikiDialect())
		parser.setBuilder(builder)
		parser.parse(markup_text)
		html_text = writer.toString()
		html_text = html_text.toString().replaceAll('\\*(.+?)\\*','\\<b>$1<\\/b>')
		html_text = html_text.toString().replaceAll('(\r|\n)','<br>$1')
		return html_text
	}
}
