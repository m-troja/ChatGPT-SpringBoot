package controller;

import com.michal.openai.Controllers.ChatGptApiController;
import com.michal.openai.entity.GptFunction;
import com.michal.openai.gpt.GptService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ChatGptApiControllerTest {

    @InjectMocks ChatGptApiController controller;
    @Mock private List<GptFunction> functions;
    @Mock private GptService gptService;
}
