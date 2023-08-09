package vn.edu.hcmut.nxvhung.bloomserver.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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
  private final List<String> waitingList = new LinkedList<>();
  private final BlacklistSender blacklistSender;
  private final CompaniesSetting companiesSetting;

  private AtomicInteger currentTimestamp;

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
    currentTimestamp.set(Math.max(message.getTimestamp(), currentTimestamp.get()));
    addBlacklist(companyName, message);

    blacklistMap.forEach((key, value) -> mergeAndSendBack(key));

  }

  private void mergeAndSendBack(String companyName) {
    List<String> partners = companiesSetting.getRelatedCompanies(companyName);
    boolean canSynched  = partners.stream().allMatch(partner-> currentTimestamp.intValue() == Optional.ofNullable(blacklistMap.get(partner)).map(CompanyData::getCurrentTimeStamp).orElse(0));

    if(canSynched) {
      Filterable<Key> mergedBlacklist = blacklistMap.get(companyName).getBlacklist();//get a copy
      partners.forEach(p -> mergedBlacklist.merge(blacklistMap.get(p).getBlacklist()));
      blacklistSender.sendMessage(companiesSetting.getResponseQueue(companyName), new Message(currentTimestamp.intValue(), mergedBlacklist));
    }
  }



}
