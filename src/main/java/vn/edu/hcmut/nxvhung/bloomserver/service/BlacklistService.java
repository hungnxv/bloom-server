package vn.edu.hcmut.nxvhung.bloomserver.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.edu.hcmut.nxvhung.bloomfilter.Filterable;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;
import vn.edu.hcmut.nxvhung.bloomserver.config.CompaniesSetting;
import vn.edu.hcmut.nxvhung.bloomserver.dto.CompanyData;
import vn.edu.hcmut.nxvhung.bloomserver.sender.BlacklistSender;

@Service
public class BlacklistService {

  private static final Logger logger = LoggerFactory.getLogger(BlacklistService.class);
  private final Map<Integer, Map<String, CompanyData>> blacklistCompanyByTimestamp = new HashMap<>();
  private final BlacklistSender blacklistSender;
  private final CompaniesSetting companiesSetting;

  @Value("${mode.async}")
  private boolean async;

  private AtomicInteger currentTimestamp = new AtomicInteger(0);

  public BlacklistService(BlacklistSender blacklistSender, CompaniesSetting companiesSetting) {
    this.blacklistSender = blacklistSender;
    this.companiesSetting = companiesSetting;
  }

  @Async
  public void handleBlacklist(Message message) {
    String companyName = message.getCompanyName();
    currentTimestamp.set(Math.max(message.getTimestamp(), currentTimestamp.get()));
    Map<String, CompanyData> blacklistData = blacklistCompanyByTimestamp.get(message.getTimestamp());
    if(Objects.isNull(blacklistData)) {
      blacklistData = new HashMap<>();
      blacklistCompanyByTimestamp.put(message.getTimestamp(), blacklistData);
    }

    blacklistData.put(companyName, new CompanyData(message.getBlacklist(), companyName, message.getTimestamp()));
    blacklistData.forEach((key, value) -> {
      try {
        mergeAndSendBack(key);
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    });

  }

  private void mergeAndSendBack(String companyName) throws CloneNotSupportedException {
    List<String> partners = companiesSetting.getRelatedCompanies(companyName);
    boolean canSych  = canSynch(partners);

    if(canSych) {
      Map<String, CompanyData> blacklistMap = blacklistCompanyByTimestamp.get(currentTimestamp.get());
      Filterable<Key> mergedBlacklist = (Filterable<Key>) ((Filterable<Key>) blacklistMap.get(companyName).getBlacklist()).clone();//get a copy
      partners.forEach(p -> mergedBlacklist.merge(blacklistMap.get(p).getBlacklist()));
      blacklistSender.sendMessage(companiesSetting.getResponseQueue(companyName), new Message(currentTimestamp.intValue(), mergedBlacklist));
    }
  }

  private boolean canSynch(List<String> partners) {
    Map<String, CompanyData> blacklistMap = blacklistCompanyByTimestamp.get(currentTimestamp.get());

    if(!async) {
      return partners.stream().allMatch(blacklistMap::containsKey);
    }


    return false;
  }


}
