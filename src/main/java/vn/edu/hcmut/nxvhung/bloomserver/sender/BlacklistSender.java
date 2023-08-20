package vn.edu.hcmut.nxvhung.bloomserver.sender;


import jakarta.jms.JMSException;
import jakarta.jms.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;

@Component
public class BlacklistSender {


  private final JmsTemplate jmsTemplate;

  public BlacklistSender(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  public void sendMessage(String destination, final Message message) {
    jmsTemplate.convertAndSend(destination, message);
  }



}
