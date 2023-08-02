package vn.edu.hcmut.nxvhung.bloomserver.rest;

import com.github.vectorclock.VectorClock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmut.nxvhung.bloomserver.sender.BloomSender;

@RestController
public class TestController {
  @Autowired
  private BloomSender bloomSender;
  @GetMapping("/test")
  public String test() {
    bloomSender.sendMessage("companyA_request", "Message cua Hung");
    return "OK";
  }

}
