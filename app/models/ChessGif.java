package models;

/**
 * Created by shelajev on 21/12/15.
 */
public class ChessGif {

  private String name;
  private String desc;

  private final byte[] bytes;

  public ChessGif(byte[] bytes) {
    this.bytes = bytes;
  }

  public String getName() {
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

}
