package vn.edu.hcmut.nxvhung.bloomserver.service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import vn.edu.hcmut.nxvhung.bloomserver.dto.CompanyData;

@Service
@RequiredArgsConstructor
public class RedisService {
  private static final String HASH_KEY = "BLACKLIST";

  private static final String BLOOM_SERVER_TIMESTAMP_VECTOR_MAP = "BLOOM_SERVER_TIMESTAMP_VECTOR_MAP";

  private static final String COMPANIES_BLACKLIST_HASH_KEY = "COMPANIES_MAP_BLACKLIST";

  private static final String TIMESTAMP_HASH_KEY = "BLOOM_SERVER_CURRENT_TIMESTAMP";


  private final RedisTemplate<Object, Object> redisTemplate;

  private HashOperations<Object, String, Map> hashOperations;

  private ValueOperations<Object, Object> timestampOperation;

  @PostConstruct
  public void init() {
    hashOperations = redisTemplate.opsForHash();
    timestampOperation = redisTemplate.opsForValue();
  }


  public void setTimestamp(Integer timestamp) {
      timestampOperation.set(TIMESTAMP_HASH_KEY, timestamp) ;
  }

  public Integer getTimestamp() {
    return (Integer) timestampOperation.get(TIMESTAMP_HASH_KEY) ;
  }

  public void saveTimestampsVector(Map<String, Integer> maxTimestampsMap) {
    hashOperations.put(HASH_KEY, BLOOM_SERVER_TIMESTAMP_VECTOR_MAP, maxTimestampsMap);
  }

  public void saveBlacklistData(Map<String, CompanyData> blacklistData) {
    hashOperations.put(HASH_KEY, COMPANIES_BLACKLIST_HASH_KEY, blacklistData);
  }
}
