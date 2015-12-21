package util;

import java.util.Base64;

public class GifUtils {
  public static String getGifDataURL(byte[] bytes) {
    return "data:image/gif;base64," + Base64.getEncoder().encodeToString(bytes);
  }

}
