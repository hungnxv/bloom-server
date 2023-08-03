package vn.edu.hcmut.nxvhung.bloomserver.dto;

import java.io.Serializable;
import vn.edu.hcmut.nxvhung.bloomfilter.Filterable;
import vn.edu.hcmut.nxvhung.bloomfilter.impl.Key;

public class CompanyMetaData implements Serializable {
  private static final long serialVersionUID = 2843843883L;

  private Filterable<Key> blacklist;

  private Integer currentTimeStamp;

  private String companyId;

  public CompanyMetaData(Filterable<Key> blacklist, String companyId, Integer currentTimeStamp) {
    this.blacklist = blacklist;
    this.currentTimeStamp = currentTimeStamp;
    this.companyId = companyId;
  }

  public Filterable<Key> getBlacklist() {
    return blacklist;
  }

  public void setBlacklist(Filterable<Key> blacklist) {
    this.blacklist = blacklist;
  }

  public Integer getCurrentTimeStamp() {
    return currentTimeStamp;
  }

  public void setCurrentTimeStamp(Integer currentTimeStamp) {
    this.currentTimeStamp = currentTimeStamp;
  }

  public String getCompanyId() {
    return companyId;
  }

  public void setCompanyId(String companyId) {
    this.companyId = companyId;
  }
}
