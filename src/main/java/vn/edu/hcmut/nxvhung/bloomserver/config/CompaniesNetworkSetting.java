package vn.edu.hcmut.nxvhung.bloomserver.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "company")
public class CompaniesNetworkSetting {
  private Map<String, List<String>> network;

  public Map<String, List<String>> getNetwork() {
    return network;
  }

  public void setNetwork(Map<String, List<String>> network) {
    this.network = network;
  }

  public List<String> getRelatedCompanies(String companyName) {
    return network.getOrDefault(companyName, Collections.emptyList());
  }
}
