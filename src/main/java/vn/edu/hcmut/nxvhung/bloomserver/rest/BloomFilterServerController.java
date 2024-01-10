package vn.edu.hcmut.nxvhung.bloomserver.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmut.nxvhung.bloomserver.sender.BlacklistSender;
import vn.edu.hcmut.nxvhung.bloomserver.service.BlacklistService;

@RestController
//@RequiredArgsConstructor
//@Slf4j
public class TestController {
  private BlacklistSender bloomSender;
  @Autowired
  private BlacklistService blacklistService;
  @GetMapping("/checkAndSendToTheClientsInAsyncMode")
  public String test() {
//    log.info("Trigger blacklistService.checkAndSendToTheClientsInAsyncMode()");
//    blacklistService.checkAndSendToTheClientsInAsyncMode();
    return "OK";
  }

}
