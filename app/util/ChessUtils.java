package util;

import chesspresso.Chess;
import chesspresso.game.Game;
import chesspresso.game.GameListener;
import chesspresso.game.view.GameBrowser;
import chesspresso.game.view.GameTextViewer;
import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.pgn.PGNReader;
import chesspresso.pgn.PGNSyntaxError;
import play.Logger;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by shelajev on 14/10/15.
 */
public class ChessUtils {

  public static final int GIF_FRAME_DELAY_MS = 800;

  public static byte[] gif(String pgn, String color, int size, int plyStart, int plyEnd) {
    try {
      pgn = pgn.replaceAll("\\{[^\\}]*\\}", "");
      Game game = new PGNReader(new ByteArrayInputStream(pgn.getBytes()), "pgn").parseGame();
      if(game == null) {
        return null;
      }
      final int bottomPlayer = "black".equalsIgnoreCase(color) ? Chess.BLACK : Chess.WHITE;

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageOutputStream ios = new MemoryCacheImageOutputStream(baos);
      GifWriter gw = new GifWriter(ios, GIF_FRAME_DELAY_MS, game.getWhite() + " " + game.getResult() + " " + game.getBlack());

      if(plyStart == 0) {
        game.gotoStart();
        gw.addImage(ComponentRenderer.paintComponent(new PositionView(game.getPosition(), bottomPlayer, size)));
      }

      int ply = 0;
      game.traverse(new GameListener() {
        @Override public void notifyMove(Move move, short[] shorts, String s, int plyNumber, int level) {
          if(plyNumber < plyStart && plyNumber > plyEnd) {
            return;
          }
          try {
            gw.addImage(ComponentRenderer.paintComponent(new PositionView(game.getPosition(), bottomPlayer, size)));
          }
          catch (IOException e) {
            Logger.error("Cannot initialize PositionView: " + e, e);
          }
        }
        @Override public void notifyLineStart(int i) {}
        @Override public void notifyLineEnd(int i) {}
      }, false);

      gw.close();
      ios.flush();
      return baos.toByteArray();
    } catch(Exception e) {
      Logger.error("Got an exception while getting images of the game:" + e, e);
      throw new RuntimeException(e);
    }
  }
}
