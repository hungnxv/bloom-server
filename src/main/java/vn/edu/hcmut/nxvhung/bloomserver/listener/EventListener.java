package vn.edu.hcmut.nxvhung.bloomserver.listener;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.util.Map;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

  @JmsListener(destination = "companyA_request")
//  @SendTo("outbound.queue")
  public String receiveMessage(final Message jsonMessage) throws JMSException {
    String messageData = null;
    System.out.println("Tin nhắn nhận được: " + jsonMessage);
    String response = null;
    if(jsonMessage instanceof TextMessage) {
      TextMessage textMessage = (TextMessage)jsonMessage;
      messageData = textMessage.getText();
//      Map map = new Gson().fromJson(messageData, Map.class);
//      response  = "Chào " + map.get("name");
    }
    return response;
  }


}
