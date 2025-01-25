<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="/css/style.css">
<link rel="stylesheet" href="/public/css/style.css">

<!DOCTYPE html>
<html lang="en">
	<head>
	   <meta charset="UTF-8">
	   <title>ChatGPT by Trojan</title>
	   <link rel="icon" href="/images/openai-16-16.ico">
	   
	</head>
		  <body>
		  <h4>ChatGPT by Trojan</h1>
		  
			  <div class = "input_form" >
			  
				  <form action="/chatgpt" method="POST">
				  
					    <span class="Query_text_span">
							ChatGPT query:  <br>
						</span>
				 		<input class="gpt_input" type="text" name="query" contenteditable="true">
				 		<span class="input_span" data-placeholder="query"> </span>
						<button class="login100-form-btn">
							Send
						</button>				  
				</form>
			<!--  <c:if test="${response != null}">  -->	
				<div class="response_div" >
					<div class="reponse_div_text">
						Reponse from ChatGPT:
					</div>
					<div class="response_div_field" >
						<input class="response_input" type="text" disabled="disabled" value="${response}">
					</div>
				
				</div>
							<!--  </c:if>   -->	
				
				
				
		  </div>
		  
  		 </body>
</html>