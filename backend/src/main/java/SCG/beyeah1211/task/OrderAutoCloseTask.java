package SCG.beyeah1211.task;

import SCG.beyeah1211.service.beyeahOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OrderAutoCloseTask {
    private static final Logger logger = LoggerFactory.getLogger(OrderAutoCloseTask.class);

    @Resource
    private beyeahOrderService beyeahOrderService;

    @Value("${app.order.auto-close-seconds:1800}")
    private int autoCloseSeconds;

    @Value("${app.order.auto-close-batch-size:100}")
    private int autoCloseBatchSize;

    @Scheduled(
            initialDelayString = "${app.order.auto-close-initial-delay-ms:30000}",
            fixedDelayString = "${app.order.auto-close-fixed-delay-ms:60000}"
    )
    public void autoCloseExpiredUnpaidOrders() {
        try {
            int closed = beyeahOrderService.autoCloseExpiredOrders(autoCloseSeconds, autoCloseBatchSize);
            if (closed > 0) {
                logger.info("auto-closed {} expired unpaid orders", closed);
            }
        } catch (Exception exception) {
            logger.warn("auto-close task skipped this round due to runtime error");
        }
    }
}
