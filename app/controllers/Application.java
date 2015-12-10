package controllers;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import chesspresso.Chess;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.hash.Hashing;
import model.Input;
import play.cache.Cache;
import play.cache.Cached;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Akka;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import util.ChessUtils;
import util.GifWriter;
import util.Lichess;
import play.libs.F;
import play.mvc.*;

import views.html.*;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import play.Logger;

import util.*;

import static play.libs.F.Promise.promise;

public class Application extends Controller {

  public Result index() {
    Form form = Form.form();
    return ok(index.render(form));
  }

  public Result about() {
    return ok(about.render());
  }

  public Result result(String key) {
    if (Cache.get(key) == null) {
      return notFound();
    }
    return ok(result.render(key));
  }

  public F.Promise<Result> gif(String key) {
    F.Promise<byte[]> result = (F.Promise<byte[]>) Cache.get(key);
    if (result != null) {
      return result.map(r -> ok(r).as("image/gif"));
    }

    return promise(() -> notFound());
  }

  public F.Promise<Result> download(String key) {
    F.Promise<byte[]> result = (F.Promise<byte[]>) Cache.get(key);
    if (result != null) {
      response().setHeader("Content-Disposition", "attachement");
      response().setHeader("filename", "chess-gif.gif");
      return result.map(r -> ok(r).as("image/gif"));
    }

    return promise(() -> notFound());
  }

  public F.Promise<Result> gfyfy(String key) {
    String url = "http://upload.gfycat.com/transcode?fetchUrl=" + routes.Application.gif(key).absoluteURL(request());

    Logger.debug("Gfycatting a gif: " + url);
    F.Promise<WSResponse> responsePromise = WS.url(url).get();
    return responsePromise.map(r -> {
      Logger.debug("Gfycatting a gif: response = " + r.getBody());
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
    int size = sizeParam != null ? Integer.valueOf(sizeParam) : 320;
    int plyStart = 0; //Integer.valueOf(requestData.get("plyStart"));
    int plyEnd = Integer.MAX_VALUE; //Integer.valueOf(requestData.get("plyEnd"));

    String key = Hashing.murmur3_32().hashUnencodedChars(pgn + "-" + color + "-" + size + "-" + plyStart + "-" + plyEnd).toString();

    F.Promise<byte[]> result = Cache.getOrElse(key, new Callable<F.Promise<byte[]>>() {
      @Override public F.Promise<byte[]> call() throws Exception {
        return promise(() -> ChessUtils.gif(pgn, color, size, plyStart, plyEnd));
      }
    }, 0);

    return result.map((r) -> temporaryRedirect(controllers.routes.Application.result(key)));
  }
}
