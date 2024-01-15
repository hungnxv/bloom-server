package vn.edu.hcmut.nxvhung.bloomserver.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Synchronized;
import vn.edu.hcmut.nxvhung.bloomfilter.Filterable;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;

@Getter
public class CompanyData implements Serializable {
  private static final long serialVersionUID = 2843843883L;

  private Filterable<Key> blacklist;

  private Integer currentTimeStamp;

  private String companyId;

  private boolean processed;


  public CompanyData(Filterable<Key> blacklist, String companyId, Integer currentTimeStamp) {
    this.blacklist = blacklist;
    this.currentTimeStamp = currentTimeStamp;
    this.companyId = companyId;
  }

  @Synchronized
  public void setBlacklist(Filterable<Key> blacklist) {
    this.blacklist = blacklist;
  }

  @Synchronized
  public void setCurrentTimeStamp(Integer currentTimeStamp) {
    this.currentTimeStamp = currentTimeStamp;
  }

  @Synchronized
  public void setCompanyId(String companyId) {
    this.companyId = companyId;
  }

  @Synchronized
  public void setProcessed(boolean processed) {
    this.processed = processed;
  }
}
