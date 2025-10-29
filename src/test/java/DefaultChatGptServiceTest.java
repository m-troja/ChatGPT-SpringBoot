import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.GptTool;
import com.michal.openai.functions.FunctionFacory;
import com.michal.openai.gpt.impl.DefaultGptService;
import com.michal.openai.persistence.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.michal.openai.slack.SlackService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class DefaultChatGptServiceTest {

    @Mock private static final String ROLE_USER = "user";
    @Mock private static final String ROLE_SYSTEM = "system";
    @Mock private static final String ROLE_ASSISTANT = "assistant";
    @Mock private String model;
    @Mock private Double temperature;
    @Mock private Double presencePenalty;
    @Mock private Integer maxTokens;
    @Mock private Integer retryAttempts;
    @Mock private Integer waitSeconds;
    @Mock private Integer qtyOfContextMessages;
    @Mock private String systemInitialMessage;
    @Mock private String jsonDir;
    @Mock private List<GptTool> tools;
    @Mock private JpaGptRequestRepo jpaGptRequestRepo;
    @Mock private RequestJdbcTemplateRepo requestTemplateRepo;
    @Mock private RestClient restClient;
    @Mock private FunctionFacory functionFactory;
    @Mock private JpaGptResponseRepo jpaGptResponseRepo;
    @Mock private JpaGptMessageRepo messageRepo;
    @Mock private ResponseJdbcTemplateRepo responseJdbc;
    @Mock private JpaSlackRepo jpaSlackrepo;
    @Mock private ObjectMapper objectMapper;
    @InjectMocks private DefaultGptService defaultGptService;



}
