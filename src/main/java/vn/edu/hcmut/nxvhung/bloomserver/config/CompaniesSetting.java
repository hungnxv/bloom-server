package vn.edu.hcmut.nxvhung.bloomserver.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "companies")
public class CompaniesSetting {
  private Map<String, List<String>> network;

  private Map<String, String> responseQueueConfig;


  public Map<String, List<String>> getNetwork() {
    return network;
  }

  public String getResponseQueue(String companyName) {
    return responseQueueConfig.get(companyName);
  }

  public void setNetwork(Map<String, List<String>> network) {
    this.network = network;
  }

  public Map<String, String> getResponseQueueConfig() {
    return responseQueueConfig;
  }

  public void setResponseQueueConfig(Map<String, String> responseQueueConfig) {
    this.responseQueueConfig = responseQueueConfig;
  }

  public List<String> getRelatedCompanies(String companyName) {
    return network.getOrDefault(companyName, Collections.emptyList());
  }
}
