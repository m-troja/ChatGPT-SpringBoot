package com.michal.openai.config;

import com.michal.openai.functions.entity.GptTool;
import com.michal.openai.github.entity.GetReposParameterProperties;
import com.michal.openai.functions.entity.GptFunction;
import com.michal.openai.jira.entity.JiraCreateIssueParameterProperties;
import com.michal.openai.tasksystem.entity.function.TaskSystemAssignIssueParameterProperties;
import com.michal.openai.tasksystem.entity.function.TaskSystemCreateIssueParameterProperties;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class BeansConfiguration {

	@Value("${slack.bot.oauth.token}")
	private String slackSecurityTokenBot ;

    @Bean("slackBotClient")
    public MethodsClient slackMethodClientBot() {
        return Slack.getInstance().methods(slackSecurityTokenBot);
    }

    @Value("${CHAT_ALLOW_FUNCTION_TASKSTORM_ASSIGN_ISSUE}")
    private String allowFunctionTaskStormAssignIssue;

    @Value("${CHAT_ALLOW_FUNCTION_TASKSTORM_CREATE_ISSUE}")
    private String allowFunctionTaskStormCreateIssue;

    @Value("${CHAT_ALLOW_FUNCTION_TASKSTORM_GET_ALL_ISSUES}")
    private String allowFunctionTaskStormGetAllIssues;

    @Value("${CHAT_ALLOW_FUNCTION_JIRA_GET_ALL_ISSUES}")
    private String allowFunctionJiraGetAllIssues;

    @Value("${CHAT_ALLOW_FUNCTION_JIRA_CREATE_ISSUE}")
    private String allowFunctionJiraCreateIssue;

    @Value("${CHAT_ALLOW_FUNCTION_GITHUB_GET_REPOS}")
    private String allowFunctionGithubGetRepos;

    @Value("${gpt.function.tasksystem.assign.issue.name}") private String taskStormAssignIssueFunctionName;
    @Value("${gpt.function.tasksystem.create.issue.name}") private String taskStormCreateIssueFunctionName;
    private final String taskStormGetAllIssuesFunctionName = "getAllTaskSystemIssuesFunction";
    @Value("${gpt.function.jira.create.issue.name}") private String jiraCreateIssueFunctionName;
    private final String jiraGetAllIssuesFunctionName = "getAllJiraIssuesFunction";
    @Value("${github.function.name}") private String githubGetReposFunctionName;

    private final ApplicationContext context;

    public BeansConfiguration(ApplicationContext context) {
        this.context = context;
    }

    /* ==============================
     *          Github
     * ==============================
     */
	@Bean("defineGetReposFunction")
	public GptFunction getRepos(
			@Value("${github.function.desc}") String functionDescription,
			@Value("${github.function.attr.login}") String attrLogin,
			@Value("${github.function.attr.login.desc}") String attrLoginDesc
    ) {
		var getReposFunction = new GptFunction();
		getReposFunction.setName(githubGetReposFunctionName);
		getReposFunction.setDescription(functionDescription);

		GptFunction.Parameters gptFunctionParameters = getReposFunction.new Parameters();
		gptFunctionParameters.setType("object");
		gptFunctionParameters.setRequired(new String[] {attrLogin});

		var properties = new GetReposParameterProperties();
		var login = new GetReposParameterProperties.LoginAttr("string", attrLoginDesc);

		properties.setLogin(login);
		gptFunctionParameters.setProperties(properties);
		getReposFunction.setParameters(gptFunctionParameters);

		return getReposFunction;
	}
    /* ==============================
     *          Jira
     * ==============================
     */


    @Bean("defineCreateJiraIssueFunction")
    public GptFunction defineCreateJiraIssueFunction(
            @Value("${gpt.function.jira.create.issue.description}") String description,
            @Value("${gpt.function.jira.create.issue.attr.description.desc}") String descrAttrDescription,
            @Value("${gpt.function.jira.create.issue.attr.issuetype.desc}") String issueTypeAttrDescription,
            @Value("${gpt.function.jira.create.issue.attr.issuetype.epic}") String epicIssueType,
            @Value("${gpt.function.jira.create.issue.attr.issuetype.story}") String storyIssueType,
            @Value("${gpt.function.jira.create.issue.attr.issuetype.task}") String taskIssueType,
            @Value("${gpt.function.jira.create.issue.attr.issuetype.bug}") String bugIssueType,
            @Value("${gpt.function.jira.create.issue.attr.duedate.desc}") String dueDateAttrDescription,
            @Value("${gpt.function.jira.create.issue.attr.summary.format}") String dueDateFormat,
            @Value("${gpt.function.jira.create.issue.attr.summary.desc}") String summaryAttrDescription,
            @Value("${gpt.function.jira.create.issue.attr.project.desc}") String projectTypeAttrDescription
    )
    {
        var gptFunction = new GptFunction();
        gptFunction.setName(jiraCreateIssueFunctionName);
        gptFunction.setDescription(description);

        // Predefine function parameters
        JiraCreateIssueParameterProperties properties = new JiraCreateIssueParameterProperties();
        properties.setDescription(new JiraCreateIssueParameterProperties.Description("string",descrAttrDescription));
        properties.setSummary(new JiraCreateIssueParameterProperties.Summary("string",summaryAttrDescription));
        properties.setDuedate(new JiraCreateIssueParameterProperties.DueDate("string",dueDateAttrDescription, dueDateFormat));
        properties.setIssueType(new JiraCreateIssueParameterProperties.IssueType("string",issueTypeAttrDescription, new String[] {epicIssueType, storyIssueType, taskIssueType, bugIssueType} ));
        properties.setProject(new JiraCreateIssueParameterProperties.Project("string", projectTypeAttrDescription));

        // Define function's parameters
        GptFunction.Parameters parameters = gptFunction.new Parameters();
        parameters.setType("object");
        parameters.setProperties(properties);
        parameters.setRequired(new String[] {"summary", "description", "issuetype", "project"});

        gptFunction.setParameters(parameters);

        return gptFunction;
    }

	@Bean("defineAllJiraIssuesFunction")
	public GptFunction defineGetAllJiraIssuesFunction() {
		var gptFunction = new GptFunction();
		gptFunction.setName(jiraGetAllIssuesFunctionName);
		gptFunction.setDescription("Get basic data about all issues in my Jira: key, assignee, description, summary, due date.");

		return gptFunction;
	}

    /* ==============================
     *        Task-System
     * ==============================
     */

    @Bean("defineAllTaskSystemIssuesFunction")
    public GptFunction defineGetAllTaskSystemIssuesFunction() {
        var gptFunction = new GptFunction();
        gptFunction.setName(taskStormGetAllIssuesFunctionName);
        gptFunction.setDescription("Get all issues in Task-System: key, assignee, description, summary, due date etc...");
        return gptFunction;
    }

    @Bean("defineAssignTaskSystemIssueFunction")
    public GptFunction assignTaskSystemIssue(
            @Value("${gpt.function.tasksystem.assign.issue.desc}") String functionDescription,
            @Value("${gpt.function.tasksystem.assign.issue.attr.key.desc}") String attrKey,
            @Value("${gpt.function.tasksystem.assign.issue.attr.slackUserId.desc}") String attrSlackUserId

    ) {
        var assignTaskSystemIssueFn = new GptFunction();
        assignTaskSystemIssueFn.setName(taskStormAssignIssueFunctionName);
        assignTaskSystemIssueFn.setDescription(functionDescription);

        GptFunction.Parameters gptFunctionParameters = assignTaskSystemIssueFn.new Parameters();
        gptFunctionParameters.setType("object");
        gptFunctionParameters.setRequired(new String[] {"key", "userSlackId"});

        var properties = new TaskSystemAssignIssueParameterProperties();
        var key = properties.new Key("string", attrKey);
        var slackUserId = properties.new SlackUserId("string", attrSlackUserId);

        properties.setKey(key);
        properties.setSlackUserId(slackUserId);
        gptFunctionParameters.setProperties(properties);
        assignTaskSystemIssueFn.setParameters(gptFunctionParameters);

        return assignTaskSystemIssueFn;
    }

    @Bean("defineCreateTaskSystemIssueFunction")
    public GptFunction defineCreateTaskSystemIssueFunction(
            @Value("${gpt.function.tasksystem.create.issue.description}") String description,
            @Value("${gpt.function.tasksystem.create.issue.attr.title.desc}") String titleAttrDescription,
            @Value("${gpt.function.tasksystem.create.issue.attr.description.desc}") String descrAttrDescription,
            @Value("${gpt.function.tasksystem.create.issue.attr.priority.desc}") String priorityAttrDescription,
            @Value("${gpt.function.tasksystem.create.issue.attr.authorslackid.desc}") String authorSlackIdAttrDescription,
            @Value("${gpt.function.tasksystem.create.issue.attr.assigneeslackid.desc}") String assigneeSlackIdAttrDescription,
            @Value("${gpt.function.tasksystem.create.issue.attr.duedate.desc}") String dueDateAttrDescription,
            @Value("${gpt.function.tasksystem.create.issue.attr.projectid.desc}") String projectIdAttrDescription,
            @Value("${gpt.function.tasksystem.create.issue.attr.duedate.format}") String dueDateFormat
    )
    {
        var gptFunction = new GptFunction();
        gptFunction.setName(taskStormCreateIssueFunctionName);
        gptFunction.setDescription(description);

        // Predefine function parameters
        var properties = new TaskSystemCreateIssueParameterProperties();
        properties.setTitle(properties.new Title("string",titleAttrDescription));
        properties.setDescription(properties.new Description("string",descrAttrDescription));
        properties.setDescription(properties.new Description("string",descrAttrDescription));
        properties.setPriority(properties.new Priority("string",priorityAttrDescription, new String[]{"LOW", "NORMAL", "HIGH", "CRITICAL"}));
        properties.setAuthorSlackId(properties.new AuthorSlackId("string",authorSlackIdAttrDescription));
        properties.setAssigneeSlackId(properties.new AssigneeSlackId("string",assigneeSlackIdAttrDescription));
        properties.setDueDate(properties.new DueDate("string",dueDateAttrDescription, dueDateFormat));
        properties.setProjectId(properties.new ProjectId("string",projectIdAttrDescription));

        // Define function's parameters
        GptFunction.Parameters parameters = gptFunction.new Parameters();
        parameters.setType("object");
        parameters.setProperties(properties);
        parameters.setRequired(new String[] {"title", "description", "authorSlackId"});

        gptFunction.setParameters(parameters);

        return gptFunction;
    }



    /* ==============================
     *          Mail - Disabled mailing feature due to security
     * ==============================


	@Bean("sendEmailFunction")
	public Function sendEmailCall() {
		return new SendEmailFunction();
	}

	@Bean("gptSendEmailFunction")
	public GptFunction gptSendEmailFunction(@Value("${gpt.function.gmail.send.email.name}") String functionName,
			@Value("${gpt.function.gmail.send.email.name}") String description,
			@Value("${gpt.function.gmail.send.email.attr.addressee.email.desc}") String addresseeEmailAttrDescription,
			@Value("${gpt.function.gmail.send.email.attr.addressee.name.desc}") String addresseeNameAttrDescription,
			@Value("${gpt.function.gmail.send.email.attr.content.desc}") String contentAttrDescription,
			@Value("${gpt.function.gmail.send.email.attr.subject.desc}") String subjectAttrDescription) {
		var function = new GptFunction();
		function.setName(functionName);
		function.setDescription(description);
		GptFunction.Parameters parameters = function.new Parameters();
		parameters.setType("object");b

		SendEmailParameterProperties properties = new SendEmailParameterProperties();
		properties.setAddresseeEmail(properties.new AddresseeEmail("string", addresseeEmailAttrDescription));
		properties.setAddresseeName(properties.new AddresseeName("string", addresseeNameAttrDescription));
		properties.setContent(properties.new Content("string", contentAttrDescription));
		properties.setSubject(properties.new Subject("string", subjectAttrDescription));

		parameters.setProperties(properties);
		parameters.setRequired(new String[] {"content", "subject"});
		function.setParameters(parameters);
		return function;
	}
	 */

    public List<GptTool> getAllowedToolCalls(
    ) {
        Map<String, GptFunction> functionMap =
                context.getBeansOfType(GptFunction.class)
                        .values().stream()
                        .collect(Collectors.toMap(GptFunction::getName, f -> f));

        var allowedFunctions = new ArrayList<GptFunction>();

        if (allowFunctionTaskStormAssignIssue.equals("1"))
        {
            allowedFunctions.add(functionMap.get(taskStormAssignIssueFunctionName));

        }
        if (allowFunctionTaskStormCreateIssue.equals("1"))
        {
            allowedFunctions.add(functionMap.get(taskStormCreateIssueFunctionName));

        }
        if (allowFunctionTaskStormGetAllIssues.equals("1")) {
            allowedFunctions.add(functionMap.get(taskStormGetAllIssuesFunctionName));
        }

        if (allowFunctionJiraGetAllIssues.equals("1"))
        {
            allowedFunctions.add(functionMap.get(jiraGetAllIssuesFunctionName));

        }
        if (allowFunctionJiraCreateIssue.equals("1"))
        {
            allowedFunctions.add(functionMap.get(jiraCreateIssueFunctionName));

        }
        if (allowFunctionGithubGetRepos.equals("1")) {
            allowedFunctions.add(functionMap.get(githubGetReposFunctionName));
        }

        System.getenv().forEach((key, value) ->
                {
                    if ( key.contains("CHAT"))
                    {
                        log.debug("{}={}", key, value);

                    }
                });

        var extractedTools = allowedFunctions.stream().map(f -> new GptTool("function", f)).toList();
        log.debug("Allowed GPT Tools:");
        for (GptTool t : extractedTools)
        {
            log.debug("{}", t.function().getName());
        }
        return extractedTools;
    }
}