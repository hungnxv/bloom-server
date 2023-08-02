package vn.edu.hcmut.nxvhung.bloomserver.dto;

import java.io.Serializable;
import org.apache.logging.log4j.core.filter.Filterable;

public class Message implements Serializable {

  private static final long serialVersionUID = -3287941672064071940L;

  private Integer logicalTimestamp;
  private Filterable blacklist;

  public Integer getLogicalTimestamp() {
    return logicalTimestamp;
  }

  public void setLogicalTimestamp(Integer logicalTimestamp) {
    this.logicalTimestamp = logicalTimestamp;
  }

  public Filterable getBlacklist() {
    return blacklist;
  }

  public void setBlacklist(Filterable blacklist) {
    this.blacklist = blacklist;
  }
}
