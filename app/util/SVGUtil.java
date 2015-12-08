package util;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.app.beans.SVGIcon;
import play.Logger;
import play.Play;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shelajev on 17/10/15.
 */
public class SVGUtil {

  private static Map<String, URI> icons = new HashMap<>();

  private static String[] pieces = new String[] {"bb", "bn", "bk", "bp", "bq", "br",
                                                 "wb", "wn", "wk", "wp", "wq", "wr"};

  public static void init() throws IOException {
    String path = "resources/chess/%s.svg";
    for(String piece : pieces) {
      try {
        InputStream resourceAsStream = Play.class.getClassLoader().getResourceAsStream(String.format(path, piece));
        icons.put(piece, initIcon(piece, resourceAsStream));
      }
      catch (Exception e) {
        Logger.info("Cannot initialize icon for piece: " + piece, e);
      }
    }
  }

  private static URI initIcon(String name, InputStream resourceStream) throws IOException {
    return SVGCache.getSVGUniverse().loadSVG(resourceStream, name);
  }

  public static SVGIcon getIcon(String name, int size) {
    URI uri = icons.get(name);
    if(uri == null) return null;
    SVGIcon icon = new SVGIcon();
    icon.setSvgURI(uri);
    icon.setPreferredSize(new Dimension(size, size));
    icon.setScaleToFit(true);
    icon.setAntiAlias(true);
    icon.setInterpolation(SVGIcon.INTERP_BICUBIC);
    return icon;
  }


}
