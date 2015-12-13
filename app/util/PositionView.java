package util;

import chesspresso.Chess;
import chesspresso.position.AbstractMutablePosition;
import com.kitfox.svg.app.beans.SVGIcon;
import play.api.Play;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;


/**
 * Position view.
 *
 *
 * This class is adapted from the chesspresso's PositionView that used ascii to represent the pieces.
 * The logic is the same, I just made it draw the icons on top of the background.
 *
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public class PositionView extends java.awt.Component
{

  private final int cellSize;
  private final int size;
  private final Image background;
  private final int bottomPlayer;
  private final AbstractMutablePosition position;

  /**
   * Create a new position view.
   *
   *@param position the position to display
   */
  public PositionView(AbstractMutablePosition position, int size) throws IOException {
    this(position, Chess.WHITE, size);
  }

  /**
   * Create a new position view.
   *
   *@param position the position to display
   *@param bottomPlayer the player at the lower edge
   */
  public PositionView(AbstractMutablePosition position, int bottomPlayer, int size) throws IOException {
    this.position = position;
    this.bottomPlayer = bottomPlayer;
    this.background = ImageIO.read(Play.class.getClassLoader().getResourceAsStream("resources/chess/maple.jpg"));
    this.size = size;
    this.cellSize = size / 8;

  }

  public Dimension getPreferredSize() {return new Dimension(size, size);}
  public Dimension getMinimumSize() {return new Dimension(size, size);}
  public Dimension getMaximumSize() {return new Dimension(size, size);}

  public void paint(Graphics graphics)
  {
    super.paint(graphics);
    graphics.drawImage(background.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
    for (int y = 0; y < Chess.NUM_OF_ROWS; y++) {
      for (int x = 0; x < Chess.NUM_OF_COLS; x++) {
        int sqi = (bottomPlayer == Chess.WHITE
          ? Chess.coorToSqi(x, Chess.NUM_OF_ROWS - y - 1)
          : Chess.coorToSqi(Chess.NUM_OF_COLS - x - 1, y));
        int stone = position.getStone(sqi);
        char piece = Chess.stoneToChar(stone);
        if(piece != ' ') {
          String name = Chess.stoneToColor(stone) == Chess.WHITE ? "w" : "b";
          name += piece;

          SVGIcon icon = SVGUtil.getIcon(name.toLowerCase(), cellSize);
          icon.paintIcon(this, graphics, x * cellSize, y * cellSize);
        }
      }
    }
  }

}