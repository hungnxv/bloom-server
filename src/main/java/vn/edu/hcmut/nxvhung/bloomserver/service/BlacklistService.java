package vn.edu.hcmut.nxvhung.bloomserver.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import vn.edu.hcmut.nxvhung.bloomfilter.dto.Message;
import vn.edu.hcmut.nxvhung.bloomfilter.hash.Hash;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.MergeableCountingBloomFilter;
import vn.edu.hcmut.nxvhung.bloomserver.dto.CompanyMetaData;
import vn.edu.hcmut.nxvhung.bloomserver.sender.BlacklistSender;

@Service
public class BlacklistService {

  private final Map<String, CompanyMetaData> blacklistMap = new HashMap<>();
  private final BlacklistSender blacklistSender;

  public BlacklistService(BlacklistSender blacklistSender) {
    this.blacklistSender = blacklistSender;
  }

  public void addBlacklist(String queue, Message message) {
    blacklistMap.put(queue, new CompanyMetaData(message.getBlacklist(), queue, message.getTimestamp()));
  }

  public void mergeAndSendBack() {
    blacklistSender.sendMessage("company_A_response" , new Message(2, new MergeableCountingBloomFilter(10, 1, Hash.MURMUR_HASH, 4)));
  }


}
