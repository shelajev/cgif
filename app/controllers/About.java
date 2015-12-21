package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.about;

public class About extends Controller {

  public Result about() {
    return ok(about.render());
  }

}
