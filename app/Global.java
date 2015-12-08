import play.Application;
import play.GlobalSettings;
import play.Logger;
import util.SVGUtil;

/**
 * Created by shelajev on 20/10/15.
 */
public class Global extends GlobalSettings {

  @Override
  public void onStart(Application app) {
    try {
      SVGUtil.init();
    }
    catch (Exception e) {
      Logger.info("SVGUtil threw " + e, e);
    }
  }
}
