package vn.edu.hcmut.nxvhung.bloomserver.listener;

import jakarta.jms.JMSException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;
import vn.edu.hcmut.nxvhung.bloomserver.config.CompaniesSetting;
import vn.edu.hcmut.nxvhung.bloomserver.service.BlacklistService;


@Component
@Slf4j
@RequiredArgsConstructor
public class EventListener {

  private final BlacklistService blacklistService;

  @JmsListener(destination = "company_request")
  public void receiveMessage(final Message message) throws JMSException {
    log.info("BFS received message {}", message);
    blacklistService.handleBlacklist(message);
  }

}
