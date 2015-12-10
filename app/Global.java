import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.logback.InstrumentedAppender;
import filters.MetricsFilter;
import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.EssentialFilter;
import util.SVGUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.SharedMetricRegistries.getOrCreate;

/**
 * Created by shelajev on 20/10/15.
 */
public class Global extends GlobalSettings {

  MetricRegistry metricRegistry = getOrCreate("play-metrics");
  GraphiteReporter graphiteReporter;

  @Override
  public void onStart(Application app) {
    super.onStart(app);
    setupMetrics(app.configuration());
    setupGraphiteReporter(app.configuration());
    try {
      SVGUtil.init();
    }
    catch (Exception e) {
      Logger.info("SVGUtil threw " + e, e);
    }
  }

  @Override
  public void onStop(Application application) {
    if (graphiteReporter != null) {
      graphiteReporter.stop();
    }

    super.onStop(application);
  }

  private void setupMetrics(Configuration configuration) {
    boolean metricsJvm     = configuration.getBoolean("metrics.jvm", false);
    boolean metricsLogback = configuration.getBoolean("metrics.logback", false);
    boolean metricsConsole = configuration.getBoolean("metrics.console", false);

    if(metricsJvm) {
      metricRegistry.registerAll(new GarbageCollectorMetricSet());
      metricRegistry.registerAll(new MemoryUsageGaugeSet());
      metricRegistry.registerAll(new ThreadStatesGaugeSet());
    }

    if (metricsLogback) {
      InstrumentedAppender appender = new InstrumentedAppender(metricRegistry);

      ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)Logger.underlying();
      appender.setContext(logger.getLoggerContext());
      appender.start();
      logger.addAppender(appender);
    }

    if (metricsConsole) {
      ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
      consoleReporter.start(1, TimeUnit.SECONDS);
    }

  }

  private void setupGraphiteReporter(Configuration configuration) {
    boolean graphiteEnabled = configuration.getBoolean("graphite.enabled", false);

    if (graphiteEnabled) {
      String   host        = configuration.getString("graphite.host", "localhost");
      int      port        = configuration.getInt("graphite.port", 80);
      String   prefix      = configuration.getString("graphite.prefix", "");
      long     period      = configuration.getLong("graphite.period", 1l);
      TimeUnit periodUnit  = TimeUnit.valueOf(configuration.getString("graphite.periodUnit", "MINUTES"));

      final Graphite graphite = new Graphite(new InetSocketAddress(host, port));
      GraphiteReporter.Builder reportBuilder = GraphiteReporter.forRegistry(metricRegistry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .filter(MetricFilter.ALL);

      if (prefix != null && !prefix.isEmpty()) {
        reportBuilder.prefixedWith(prefix);
      }

      graphiteReporter = reportBuilder.build(graphite);

      graphiteReporter.start(period, periodUnit);
    }
  }

  @Override
  public <T extends EssentialFilter> Class<T>[] filters() {
    return new Class[]{ MetricsFilter.class};
  }


}
