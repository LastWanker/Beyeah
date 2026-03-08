package SCG.beyeah1211.service;

import SCG.beyeah1211.controller.vo.AfterSaleVO;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;

public interface beyeahAfterSaleService {
    AfterSaleVO createAfterSale(Long userId,
                                String orderNo,
                                Long orderItemId,
                                Integer refundAmount,
                                String reason,
                                String description);

    PageResult getMyAfterSales(PageQueryUtil pageUtil);

    AfterSaleVO getMyAfterSaleDetail(Long userId, String afterSaleNo);

    PageResult getAdminAfterSales(PageQueryUtil pageUtil);

    AfterSaleVO approveAfterSale(Long afterSaleId);

    AfterSaleVO rejectAfterSale(Long afterSaleId, String rejectReason);
}
