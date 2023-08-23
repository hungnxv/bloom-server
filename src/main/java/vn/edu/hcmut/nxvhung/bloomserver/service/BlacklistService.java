package vn.edu.hcmut.nxvhung.bloomserver.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
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

  @Value("${mode.epsilon}")
  private Integer epsilon;

  private static final AtomicInteger currentTimestamp = new AtomicInteger(0);
  private static final Map<String, Integer> maxTimestampsMap = new ConcurrentHashMap<>();

  public BlacklistService(BlacklistSender blacklistSender, CompaniesSetting companiesSetting) {
    this.blacklistSender = blacklistSender;
    this.companiesSetting = companiesSetting;
  }

  @Async
  public void handleBlacklist(Message message) {
    String companyName = message.getCompanyName();
    currentTimestamp.set(Math.max(message.getTimestamp(), currentTimestamp.get()));
    maxTimestampsMap.put(companyName, message.getTimestamp());
    Map<String, CompanyData> blacklistData = blacklistCompanyByTimestamp.get(message.getTimestamp());
    if (Objects.isNull(blacklistData)) {
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
    boolean canSyc = canSync(partners);

    if (canSyc) {
      Map<String, CompanyData> blacklistMap = blacklistCompanyByTimestamp.get(currentTimestamp.get());
      Filterable<Key> mergedBlacklist = (Filterable<Key>) ((Filterable<Key>) blacklistMap.get(companyName).getBlacklist()).clone();//get a copy
      partners.forEach(p -> mergedBlacklist.merge(blacklistMap.get(p).getBlacklist()));
      blacklistSender.sendMessage(companiesSetting.getResponseQueue(companyName), new Message(currentTimestamp.intValue(), mergedBlacklist));
    }
  }

  private boolean canSync(List<String> partners) {
    Map<String, CompanyData> blacklistMap = blacklistCompanyByTimestamp.get(currentTimestamp.get());
    return partners.stream().allMatch(blacklistMap::containsKey);
  }

  @Scheduled(cron = "10 * * * *")
  public void checkAndSendToTheClientsInAsyncMode() {
    if (!async) {
      return;
    }
    Map<String, CompanyData> currentBlacklist = blacklistCompanyByTimestamp.get(currentTimestamp.get());

    for (Entry<String, CompanyData> entry : currentBlacklist.entrySet()) {
      processByCompany(entry);
    }
  }

  private void processByCompany(Entry<String, CompanyData> entry)  {
    String companyName = entry.getKey();
    List<String> partners = companiesSetting.getRelatedCompanies(companyName);
    boolean canSync = partners.stream().allMatch(p -> Math.abs(currentTimestamp.get() - maxTimestampsMap.getOrDefault(p, 0)) > epsilon);
    if(!canSync) {
      return;
    }

    Map<String, CompanyData> blacklistMap = blacklistCompanyByTimestamp.get(currentTimestamp.get());
    try {
      Filterable<Key> mergedBlacklist = (Filterable<Key>) ((Filterable<Key>) blacklistMap.get(companyName).getBlacklist()).clone();
      partners.forEach(p -> {
        Optional<Filterable<Key>> blacklist = Optional.ofNullable(blacklistCompanyByTimestamp.get(maxTimestampsMap.getOrDefault(p, 0))).map(CompanyData::getBlacklist);
        blacklist.ifPresent(mergedBlacklist::merge);
      });
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }

  }


}
