package util;

/**
 * Created by shelajev on 14/10/15.
 */

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public class ComponentRenderer {

  public static BufferedImage paintComponent(Component c) {
    c.setSize(c.getPreferredSize());
    layoutComponent(c);

    BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(),
      BufferedImage.TYPE_INT_RGB);

    CellRendererPane crp = new CellRendererPane();
    crp.add(c);
    crp.paintComponent(img.createGraphics(), c, crp, c.getBounds());
    return img;
  }

  // from the example of user489041
  public static void layoutComponent(Component c) {
    synchronized (c.getTreeLock()) {
      c.doLayout();
      if (c instanceof Container)
        for (Component child : ((Container) c).getComponents())
          layoutComponent(child);
    }
  }
}
