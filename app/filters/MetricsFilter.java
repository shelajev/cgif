package filters;

import com.codahale.metrics.*;
import play.api.libs.iteratee.Execution;
import play.api.libs.iteratee.Iteratee;
import play.api.mvc.EssentialAction;
import play.api.mvc.EssentialFilter;
import play.api.mvc.RequestHeader;
import play.api.mvc.Result;
import scala.Function1;
import scala.runtime.AbstractFunction1;

import java.util.HashMap;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by shelajev on 10/12/15.
 */
public class MetricsFilter implements EssentialFilter {

  private final MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate("play-metrics");

  private final Counter activeRequests = metricRegistry.counter(name("activeRequests"));
  private final Timer requestTimer = metricRegistry.timer(name("requestsTimer"));

  private final Map<String, Meter> statusMeters = new HashMap<String, Meter>() {{
    put("1", metricRegistry.meter(name("1xx-responses")));
    put("2", metricRegistry.meter(name("2xx-responses")));
    put("3", metricRegistry.meter(name("3xx-responses")));
    put("4", metricRegistry.meter(name("4xx-responses")));
    put("5", metricRegistry.meter(name("5xx-responses")));
  }};

  public EssentialAction apply(final EssentialAction next) {

    return new MetricsAction() {

      @Override
      public EssentialAction apply() {
        return next.apply();
      }

      @Override
      public Iteratee<byte[], Result> apply(final RequestHeader requestHeader) {
        activeRequests.inc();
        final Timer.Context requestTimerContext = requestTimer.time();

        return next.apply(requestHeader).map(new AbstractFunction1<Result, Result>() {

          @Override
          public Result apply(Result result) {
            activeRequests.dec();
            requestTimerContext.stop();
            String statusFirstCharacter = String.valueOf(result.header().status()).substring(0, 1);
            if (statusMeters.containsKey(statusFirstCharacter)) {
              statusMeters.get(statusFirstCharacter).mark();
            }
            return result;
          }

          @Override
          public <A> Function1<Result, A> andThen(Function1<Result, A> result) {
            return result;
          }

          @Override
          public <A> Function1<A, Result> compose(Function1<A, Result> result) {
            return result;
          }

        }, Execution.defaultExecutionContext());
      }

    };
  }

  public abstract class MetricsAction extends
    AbstractFunction1<RequestHeader, Iteratee<byte[], Result>>
    implements EssentialAction {
  }
}