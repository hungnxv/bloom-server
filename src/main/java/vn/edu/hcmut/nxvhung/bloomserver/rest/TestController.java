package vn.edu.hcmut.nxvhung.bloomserver.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmut.nxvhung.bloomserver.sender.BlacklistSender;

@RestController
@RequiredArgsConstructor

public class TestController {
  private final BlacklistSender bloomSender;
  @GetMapping("/test")
  public String test() {
    return "OK";
  }

}
