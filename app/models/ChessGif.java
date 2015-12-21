package models;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by shelajev on 21/12/15.
 */
public class ChessGif {

  public static class Cache {
    private static final LinkedHashMap<String, ChessGif> map = new LinkedHashMap<String, ChessGif>() {
      protected boolean removeEldestEntry(Map.Entry<String, ChessGif> eldest) {
        return size() > 16;
      }
    };

    public static Collection<ChessGif> get(){
     return map.values();
    }
  }

  private String name;
  private String desc;

  private final byte[] bytes;

  public ChessGif(byte[] bytes) {
    this.bytes = bytes;
  }

  public String getName() {
    if("null - null".equals(name)) {
      return "";
    }
    return name;
  }

  public ChessGif setName(String name) {
    this.name = name;
    return this;
  }

  public String getDesc() {
    return desc;
  }

  public ChessGif setDesc(String desc) {
    this.desc = desc;
    return this;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public ChessGif cache() {
    Cache.map.put(UUID.randomUUID().toString(), this);
    return this;
  }

}
