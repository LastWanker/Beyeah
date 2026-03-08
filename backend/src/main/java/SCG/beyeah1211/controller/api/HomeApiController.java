package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.Constants;
import SCG.beyeah1211.common.IndexConfigTypeEnum;
import SCG.beyeah1211.service.beyeahIndexConfigService;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/home")
public class HomeApiController {
    @Resource
    private beyeahIndexConfigService beyeahIndexConfigService;

    @GetMapping("/new")
    public Result newGoods() {
        return ResultGenerator.genSuccessResult(
                beyeahIndexConfigService.getConfigGoodsesForIndex(
                        IndexConfigTypeEnum.INDEX_GOODS_NEW.getType(),
                        Constants.INDEX_GOODS_NEW_NUMBER
                )
        );
    }

    @GetMapping("/hot")
    public Result hotGoods() {
        return ResultGenerator.genSuccessResult(
                beyeahIndexConfigService.getConfigGoodsesForIndex(
                        IndexConfigTypeEnum.INDEX_GOODS_HOT.getType(),
                        Constants.INDEX_GOODS_HOT_NUMBER
                )
        );
    }
}

