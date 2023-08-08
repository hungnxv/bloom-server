package vn.edu.hcmut.nxvhung.bloomserver.listener;

import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;
import vn.edu.hcmut.nxvhung.bloomserver.config.CompaniesSetting;
import vn.edu.hcmut.nxvhung.bloomserver.service.BlacklistService;


@Component
public class EventListener {

  private final BlacklistService blacklistService;

  public EventListener(BlacklistService blacklistService) {
    this.blacklistService = blacklistService;
  }

  @Autowired
  private CompaniesSetting companiesSetting;

  @JmsListener(destination = "company_request")
  public void receiveMessage(final Message message) throws JMSException {
    blacklistService.handleBlacklist(message);
  }

}
