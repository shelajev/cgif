package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.hash.Hashing;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import util.ChessUtils;
import views.html.about;
import views.html.index;
import views.html.result;

import java.util.concurrent.Callable;

import static play.libs.F.Promise.promise;

public class About extends Controller {

  public Result about() {
    return ok(about.render());
  }

}
