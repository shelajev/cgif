package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.hash.Hashing;
import models.ChessGif;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import util.ChessUtils;
import util.Constants;
import views.html.index;
import views.html.result;

import java.util.concurrent.Callable;

import static play.libs.F.Promise.promise;

public class Application extends Controller implements Constants {

  public Result index() {
    Form form = Form.form();
    return ok(index.render(form));
  }

  public Result result(String key) {
    if (Cache.get(key) == null) {
      return notFound();
    }
    return ok(result.render(key));
  }

  public F.Promise<Result> gif(String key) {
    F.Promise<ChessGif> result = (F.Promise<ChessGif>) Cache.get(key);
    if (result != null) {
      return result.map(r -> ok(r.getBytes()).as("image/gif"));
    }

    return promise(() -> notFound());
  }

  public F.Promise<Result> download(String key) {
    F.Promise<ChessGif> result = (F.Promise<ChessGif>) Cache.get(key);
    if (result != null) {
      response().setHeader("Content-Disposition", "attachement");
      response().setHeader("filename", "chess-gif.gif");
      return result.map(r -> ok(r.getBytes()).as("image/gif"));
    }

    return promise(() -> notFound());
  }

  public F.Promise<Result> gfyfy(String key) {
    String url = "http://upload.gfycat.com/transcode?fetchUrl=" + routes.Application.gif(key).absoluteURL(request());
    F.Promise<WSResponse> responsePromise = WS.url(url).get();
    return responsePromise.map(r -> {
      JsonNode gfyName = r.asJson().get("gfyName");
      return gfyName.textValue();
    })
      .map(name -> temporaryRedirect("http://gfycat.com/" + name));
  }

  public F.Promise<Result> pgn() {
    DynamicForm requestData = Form.form().bindFromRequest();
    String pgn = requestData.get("pgn");
    String color = requestData.get("color");

    String sizeParam = requestData.get("size");
    int size = sizeParam != null ? Integer.valueOf(sizeParam) : DEFAULT_BOARD_SIZE_PX;

    int moveDelay = requestData.get("delay") == null ? DEFAULT_MOVE_DELAY_MS : (int) (Float.parseFloat(requestData.get("delay")) * 1000);
    int lastDelay = requestData.get("lastDelay") == null ? DEFAULT_LAST_MOVE_DELAY_MS : (int) (Float.parseFloat(requestData.get("lastDelay")) * 1000);

    final String key = Hashing.murmur3_32().hashUnencodedChars(pgn + "-" + color + "-" + size + "-" + moveDelay + "-" + lastDelay).toString();

    F.Promise<ChessGif> result = Cache.getOrElse(key, new Callable<F.Promise<ChessGif>>() {
      @Override public F.Promise<ChessGif> call() throws Exception {
        return promise(() -> ChessUtils.gif(pgn, color, size, moveDelay, lastDelay).cache());
      }
    }, 0);

    return result.map((r) -> temporaryRedirect(controllers.routes.Application.result(key)));
  }
}
