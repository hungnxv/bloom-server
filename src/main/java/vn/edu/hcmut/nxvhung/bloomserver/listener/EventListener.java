package vn.edu.hcmut.nxvhung.bloomserver.listener;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;
//import jakarta.jms.Message;


@Component
public class EventListener {

  @JmsListener(destination = "company_A_request")
  public String receiveMessage(final Message jsonMessage) throws JMSException {
    String messageData = null;
//    System.out.println("Tin nhắn nhận được: " + jsonMessage.getLocalTimestamp() + jsonMessage.getFilterable());
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
