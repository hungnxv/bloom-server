package vn.edu.hcmut.nxvhung.bloomserver.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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
import vn.edu.hcmut.nxvhung.bloomfilter.impl.MergeableCountingBloomFilter;
import vn.edu.hcmut.nxvhung.bloomserver.config.CompaniesSetting;
import vn.edu.hcmut.nxvhung.bloomserver.dto.CompanyData;
import vn.edu.hcmut.nxvhung.bloomserver.sender.BlacklistSender;

@Service
public class BlacklistService {

  private static final Logger logger = LoggerFactory.getLogger(BlacklistService.class);
  private final Map<String, CompanyData> blacklistData = new ConcurrentHashMap<>();
  private final BlacklistSender blacklistSender;
  private final CompaniesSetting companiesSetting;

  private final RedisService redisService;

  @Value("${mode.async}")
  private boolean async;

  @Value("${mode.epsilon}")
  private Integer epsilon;

  private static final AtomicInteger currentTimestamp = new AtomicInteger(0);
  private static final Map<String, Integer> timestampsVector = new ConcurrentHashMap<>();

  public BlacklistService(BlacklistSender blacklistSender, CompaniesSetting companiesSetting, RedisService redisService) {
    this.blacklistSender = blacklistSender;
    this.companiesSetting = companiesSetting;
    this.redisService = redisService;
  }

  @Async
  public void handleBlacklist(Message message) {
    String companyName = message.getCompanyName();
    Integer companyTimestamp = timestampsVector.getOrDefault(companyName, 0);

    Integer receivedTimestamp = message.getTimestamp();
    if (companyTimestamp > receivedTimestamp) {
      logger.info("Company {} sent outdated blacklist. Received timestamp: {}. Current timestamp: {}", companyName, receivedTimestamp,
          companyTimestamp);
      return;
    }
    currentTimestamp.set(Math.max(receivedTimestamp, currentTimestamp.get()));
    redisService.setTimestamp(currentTimestamp.get());

    timestampsVector.put(companyName, receivedTimestamp);
    redisService.saveTimestampsVector(timestampsVector);

    CompanyData companyData = blacklistData.get(companyName);
    if (Objects.isNull(companyData)) {
      companyData = new CompanyData(message.getBlacklist(), companyName, receivedTimestamp);
      blacklistData.put(companyName, companyData);
    }

    companyData.setBlacklist(message.getBlacklist());
    companyData.setCurrentTimeStamp(receivedTimestamp);
    companyData.setProcessed(false);
    redisService.saveBlacklistData(blacklistData);
    blacklistData.forEach((key, value) -> mergeAndSendBack(key));

  }

  private void mergeAndSendBack(String companyName) {
    List<String> partners = companiesSetting.getRelatedCompanies(companyName);

    boolean canSyc = canSync(companyName, partners);
    if (!canSyc) {
      return;
    }

    CompanyData companyData = blacklistData.get(companyName);
    sendBackToBFC(companyName, companyData, partners);

  }

  private void sendBackToBFC(String companyName, CompanyData companyData, List<String> partners) {
    Filterable<Key> mergedBlacklist = ((MergeableCountingBloomFilter) companyData.getBlacklist()).copySetting();
    partners.forEach(p -> mergedBlacklist.merge(blacklistData.get(p).getBlacklist()));
    Map<String, Integer> partnerTimestampVector = new HashMap<>(partners.size() + 1);
    partnerTimestampVector.put("BFS", currentTimestamp.get());
    partners.forEach(partner -> partnerTimestampVector.put(partner, timestampsVector.get(partner)));
    companyData.setProcessed(true);
    logger.info("BFS: send blacklist to company {}, timestamp: {}", companyName, partnerTimestampVector);
    blacklistSender.sendMessage(companiesSetting.getResponseQueue(companyName),
        new Message(currentTimestamp.intValue(), mergedBlacklist, partnerTimestampVector));
  }

  private boolean canSync(String companyName, List<String> partners) {
    Integer lastTimestamp = timestampsVector.getOrDefault(companyName, 0);
    if (lastTimestamp < currentTimestamp.get()) {
      return false;
    }
    return partners.stream().allMatch(p -> lastTimestamp.equals(timestampsVector.getOrDefault(p, 0)));
  }

  @Scheduled(cron = "10 0 * * * *")
  public void checkAndSendToTheClients() {

    if (!async) {
      logger.info("checkAndSendToTheClients: processing in sync mode");
      processSyncMode();
      return;
    }

    logger.info("checkAndSendToTheClientsInAsyncMode: get data from current timestamp {}", currentTimestamp.get());

    for (Entry<String, CompanyData> entry : blacklistData.entrySet()) {
      processByCompany(entry);
    }
    redisService.saveBlacklistData(blacklistData);

  }

  private void processSyncMode() {
    for (Entry<String, CompanyData> entry : blacklistData.entrySet()) {
      String companyName = entry.getKey();
      CompanyData companyData = entry.getValue();
      if (companyData.isProcessed()) {
        continue;
      }
      List<String> partners = companiesSetting.getRelatedCompanies(entry.getKey());
      if (canSync(companyName, partners)) {
        sendBackToBFC(entry.getKey(), companyData, partners);
      }

    }
    redisService.saveBlacklistData(blacklistData);

  }

  private void processByCompany(Entry<String, CompanyData> entry) {
    String companyName = entry.getKey();
    List<String> partners = companiesSetting.getRelatedCompanies(companyName);
    boolean canSync = partners.stream().allMatch(p -> Math.abs(currentTimestamp.get() - timestampsVector.getOrDefault(p, 0)) <= epsilon);
    if (!canSync) {
      return;
    }
    sendBackToBFC(companyName, entry.getValue(), partners);
  }

}
