package com.michal.openai.functions;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class FunctionFacory implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	
	public Function getFunctionByFunctionName(String functionName)
	{
		return (Function) applicationContext.getBean(functionName);	
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException 
	{
		this.applicationContext = applicationContext;
	}
	
	
}
