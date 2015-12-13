package util;

/**
 * Created by shelajev on 14/10/15.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

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

  public static void layoutComponent(Component c) {
    synchronized (c.getTreeLock()) {
      c.doLayout();
      if (c instanceof Container)
        for (Component child : ((Container) c).getComponents())
          layoutComponent(child);
    }
  }
}
