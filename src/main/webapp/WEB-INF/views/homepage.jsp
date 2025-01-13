<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
	<head>
	   <meta charset="UTF-8">
	   <title>ChatGPT by upat</title>
	   
	</head>
		  <body>
		  
			  <div class = "input_form" >
			  
				  <form action="/chatgpt" method="POST">
				  
					    <span class="Query_text_span">
							Your query:  
						</span>
				 		<input class="input" type="text" name="query">
				 		<span class=input_span" data-placeholder="query"> </span>
						<button class="login100-form-btn">
							Send
						</button>				  
				</form>
				
				<c:if test="${response != null}">
				<div class="response_div" >
					<div class="reponse_div_text">
						Reponse:
					</div>
					<div class="response_div_field" >
						<input class="response_input" type="text" disabled="disabled">
					</div>
				
				</div>
				
				</c:if>
				
		  </div>
		  
  		 </body>
</html>