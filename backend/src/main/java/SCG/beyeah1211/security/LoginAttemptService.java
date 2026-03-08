package SCG.beyeah1211.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private final ConcurrentHashMap<String, AttemptState> attempts = new ConcurrentHashMap<>();

    @Value("${app.auth.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.auth.login.window-seconds:600}")
    private long windowSeconds;

    @Value("${app.auth.login.lock-seconds:900}")
    private long lockSeconds;

    public boolean isBlocked(String ip, String username) {
        String key = key(ip, username);
        AttemptState state = attempts.get(key);
        if (state == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (state.lockUntilMs > now) {
            return true;
        }
        if (now - state.windowStartMs > windowSeconds * 1000) {
            attempts.remove(key);
        }
        return false;
    }

    public void onFailure(String ip, String username) {
        String key = key(ip, username);
        long now = System.currentTimeMillis();
        attempts.compute(key, (k, old) -> {
            AttemptState state = old == null ? new AttemptState(now) : old;
            if (now - state.windowStartMs > windowSeconds * 1000) {
                state.windowStartMs = now;
                state.failCount = 0;
                state.lockUntilMs = 0;
            }
            if (state.lockUntilMs > now) {
                return state;
            }
            state.failCount += 1;
            if (state.failCount >= maxAttempts) {
                state.lockUntilMs = now + lockSeconds * 1000;
                state.failCount = 0;
                state.windowStartMs = now;
            }
            return state;
        });
    }

    public void onSuccess(String ip, String username) {
        attempts.remove(key(ip, username));
    }

    private String key(String ip, String username) {
        String safeIp = ip == null ? "unknown" : ip.trim();
        String safeUsername = username == null ? "unknown" : username.trim();
        return safeIp + "|" + safeUsername;
    }

    private static final class AttemptState {
        private long windowStartMs;
        private int failCount;
        private long lockUntilMs;

        private AttemptState(long now) {
            this.windowStartMs = now;
            this.failCount = 0;
            this.lockUntilMs = 0;
        }
    }
}
