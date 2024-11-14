package auth.ms.token_store;

import java.time.Duration;
import java.time.Instant;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class ExpiredTokensScheduler {

    private static final Logger LOG = Logger.getLogger("ExpiredTokensScheduler");

    private static final long WARN_LOG_DURATION_THRESHOLD_SECONDS = 10;

    private final TokenStoreService tokenStoreService;

    @Inject
    public ExpiredTokensScheduler(TokenStoreService tokenStoreService) {
        this.tokenStoreService = tokenStoreService;
    }

    @Scheduled(every = "3m", delayed = "1m")
    public void deleteExpiredTokens() {
        var startInstant = Instant.now();
        var count = tokenStoreService.deleteExpired();
        var duration = Duration.between(startInstant, Instant.now());
        var tookMillis = duration.toMillis();

        var message = String.format("Deleted %d expired tokens (took %d ms)", count, tookMillis);

        if (duration.toSeconds() > WARN_LOG_DURATION_THRESHOLD_SECONDS) {
            LOG.warn(message);
        } else if (count > 0) {
            LOG.info(message);
        } else {
            LOG.debug(message);
        }
    }
}
