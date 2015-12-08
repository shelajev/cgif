package util;

import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSRequest;
import play.libs.ws.WSRequestHolder;

import java.util.Optional;

/**
 * Created by shelajev on 13/10/15.
 */
public class Lichess {

  public static F.Promise<Optional<String>> getPGN(String id) {
    WSRequest request = WS.url("http://lichess.org/game/export/"+ id + ".pgn");
    return request.get().map(response -> Optional.of(response.getBody()));
  }
}
