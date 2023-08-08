package vn.edu.hcmut.nxvhung.bloomserver.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.edu.hcmut.nxvhung.bloomfilter.Filterable;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;
import vn.edu.hcmut.nxvhung.bloomfilter.hash.Hash;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.MergeableCountingBloomFilter;
import vn.edu.hcmut.nxvhung.bloomserver.config.CompaniesSetting;
import vn.edu.hcmut.nxvhung.bloomserver.dto.CompanyData;
import vn.edu.hcmut.nxvhung.bloomserver.sender.BlacklistSender;

@Service

public class BlacklistService {

  private static final Logger logger = LoggerFactory.getLogger(BlacklistService.class);
  private final Map<String, CompanyData> blacklistMap = new HashMap<>();
  private final BlacklistSender blacklistSender;
  private final CompaniesSetting companiesSetting;

  public BlacklistService(BlacklistSender blacklistSender, CompaniesSetting companiesSetting) {
    this.blacklistSender = blacklistSender;
    this.companiesSetting = companiesSetting;
  }

  public void addBlacklist(String companyName, Message message) {
    companiesSetting.getResponseQueue(companyName);
    blacklistMap.put(companyName, new CompanyData(message.getBlacklist(), companyName, message.getTimestamp()));
  }

  public CompanyData getBlacklist(String companyName) {
    return blacklistMap.get(companyName);
  }

  @Async
  public void handleBlacklist(Message message) {
    String companyName = message.getCompanyName();
    addBlacklist(companyName, message);
    List<String> partners = companiesSetting.getRelatedCompanies(companyName);
    for(String partner : partners) {
      CompanyData companyData = blacklistMap.get(partner);
      if (Objects.isNull(companyData)) {
        logger.warn("{} not send data ", partner);
      } else if(companyData.getCurrentTimeStamp().intValue() == message.getTimestamp().intValue()){

      }
    }
    boolean canSynched  = true;
    if(canSynched) {
      Filterable<Key> mergedBlacklist = message.getBlacklist();//get a copy
      partners.forEach(p -> mergedBlacklist.merge(blacklistMap.get(p).getBlacklist()));
      blacklistSender.sendMessage(companiesSetting.getResponseQueue(companyName), new Message(message.getTimestamp(),mergedBlacklist));
    }


  }

  public void mergeAndSendBack() {

    blacklistSender.sendMessage("company_A_response", new Message(2, new MergeableCountingBloomFilter(10, 1, Hash.MURMUR_HASH, 4)));
  }


}
