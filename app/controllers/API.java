package controllers;

import com.google.common.hash.Hashing;
import models.ChessGif;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import util.ChessUtils;
import util.Constants;

import java.util.concurrent.Callable;

import static play.libs.F.Promise.promise;

public class API extends Controller  implements Constants{

  public F.Promise<Result> pgn() {
    DynamicForm requestData = Form.form().bindFromRequest();
    String pgn = requestData.get("pgn");
    String color = requestData.get("color");

    String sizeParam = requestData.get("size");
    int size = sizeParam != null ? Integer.valueOf(sizeParam) : DEFAULT_BOARD_SIZE_PX;

    int moveDelay = requestData.get("delay") == null ? DEFAULT_MOVE_DELAY_MS : (int) (Float.parseFloat(requestData.get("delay")) * 1000);
    int lastDelay = requestData.get("lastDelay") == null ? DEFAULT_LAST_MOVE_DELAY_MS : (int) (Float.parseFloat(requestData.get("lastDelay")) * 1000);

    String key = Hashing.murmur3_32().hashUnencodedChars(pgn + "-" + color + "-" + size + "-" + moveDelay + "-" + lastDelay).toString();

    F.Promise<ChessGif> result = Cache.getOrElse(key, new Callable<F.Promise<ChessGif>>() {
      @Override public F.Promise<ChessGif> call() throws Exception {
        return promise(() -> ChessUtils.gif(pgn, color, size, moveDelay, lastDelay).cache());
      }
    }, 0);

    return result.map((r) -> ok(r.getBytes()).as("image/gif"));
  }

}
