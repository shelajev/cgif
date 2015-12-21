package controllers;

import models.ChessGif;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.history;

public class History extends Controller {

  public Result recentEntries() {
    return ok(history.render(ChessGif.Cache.get()));
  }

}
