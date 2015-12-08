package controllers;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import chesspresso.Chess;
import model.Input;
import play.cache.Cache;
import play.cache.Cached;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Akka;
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

  public F.Promise<Result> pgn() {
    DynamicForm requestData = Form.form().bindFromRequest();
    String pgn = requestData.get("pgn");
    String color = requestData.get("color");
    int size = Integer.valueOf(requestData.get("size"));
    int plyStart = 0; //Integer.valueOf(requestData.get("plyStart"));
    int plyEnd = Integer.MAX_VALUE; //Integer.valueOf(requestData.get("plyEnd"));

    String key = pgn + "-" + color + "-" + size + "-" + plyStart + "-" + plyEnd;
    return Cache.getOrElse(key, new Callable<F.Promise<Result>>() {
      @Override public F.Promise<Result> call() throws Exception {
        return promise(() -> {
          return ok(ChessUtils.gif(pgn, color, size, plyStart, plyEnd)).as("image/gif");
        });
    }}, 0);
  }
}
