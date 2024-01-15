package vn.edu.hcmut.nxvhung.bloomserver.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmut.nxvhung.bloomserver.service.BlacklistService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/BFS")
public class BloomFilterServerController {

  private final BlacklistService blacklistService;
  @GetMapping("/triggerJob")
  public String triggerJobManually() {
    log.info("Trigger job to check and send blacklist data to client");
    blacklistService.checkAndSendToTheClients();
    return "OK";
  }

}
