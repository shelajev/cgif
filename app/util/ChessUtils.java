package util;

import chesspresso.Chess;
import chesspresso.game.Game;
import chesspresso.game.GameListener;
import chesspresso.move.Move;
import chesspresso.pgn.PGNReader;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import play.Logger;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by shelajev on 14/10/15.
 */
public class ChessUtils {

  private static final MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate("play-metrics");
  private static final Timer requestTimer = metricRegistry.timer(name("gifsTimer"));

  public static byte[] gif(String pgn, String color, int size, int moveDelayInMilliseconds, int lastImageInMilliseconds) {
    final Timer.Context time = requestTimer.time();
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ImageOutputStream ios = new MemoryCacheImageOutputStream(baos)) {
      pgn = pgn.replaceAll("\\{[^\\}]*\\}", "");
      Game game = new PGNReader(new ByteArrayInputStream(pgn.getBytes()), "pgn").parseGame();
      if (game == null) {
        return null;
      }
      final int bottomPlayer = "black".equalsIgnoreCase(color) ? Chess.BLACK : Chess.WHITE;

      try (GifWriter gw = new GifWriter(ios, moveDelayInMilliseconds, game.getWhite() + " " + game.getResult() + " " + game.getBlack())) {
        game.gotoStart();

        BufferedImage[] mutableImage = new BufferedImage[] { ComponentRenderer.paintComponent(new PositionView(game.getPosition(), bottomPlayer, size)) };
        gw.addImage(mutableImage[0]);

        game.traverse(new GameListener() {
          @Override public void notifyMove(Move move, short[] shorts, String s, int plyNumber, int level) {
            try {
              mutableImage[0] = ComponentRenderer.paintComponent(new PositionView(game.getPosition(), bottomPlayer, size));
              gw.addImage(mutableImage[0]);
            }
            catch (IOException e) {
              Logger.error("Cannot initialize PositionView: " + e, e);
            }
          }

          @Override public void notifyLineStart(int i) {
          }

          @Override public void notifyLineEnd(int i) {
          }
        }, false);

        //hold last image longer, has to be a multiple of the normal delay
        for (int i = 0; i < lastImageInMilliseconds / moveDelayInMilliseconds; i++) {
          gw.addImage(mutableImage[0]);
        }
      }
      ios.flush();
      return baos.toByteArray();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    finally {
      time.stop();
    }
  }
}
