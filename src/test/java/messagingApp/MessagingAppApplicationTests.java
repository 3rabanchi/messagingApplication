package messagingApp;

import com.edcs.model.Config;
import com.edcs.model.Nodes;
import com.edcs.service.RabbitmqService;
import com.edcs.utils.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class MessagingAppApplicationTests {

    @Test
    void checkMethodIfUsed() {
        RabbitmqService rabbitmqService = new RabbitmqService();
        Assert.assertFalse(rabbitmqService.ifUsed("15672"));
    }

    @Test
    void checkConfigFile() throws IOException, JSONException {
        String data = FileUtils.getConfigFile();

        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(data, Config.class);
        System.out.println(config.getNodes()[1].getManagementPort());
        //	System.out.println(level);
    }

    @Test
    public void getFreeNodeTest() throws JsonProcessingException {
        String data = FileUtils.getConfigFile();
        RabbitmqService rabbitmqService = new RabbitmqService();

        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(data, Config.class);
        for (Nodes node : config.getNodes()) {
            System.out.println(node.getManagementPort() + " - checking!");
            if (rabbitmqService.ifUsed(node.getManagementPort()))
                System.out.println(node.getManagementPort() + " is not used!");
            else
                System.out.println(node.getManagementPort() + " is used!");
        }
        //;	Assert.assertTrue(true);
        //	return config.getNodes()[1].getAmqpPort();
    }
}
