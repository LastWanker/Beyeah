package SCG.beyeah1211.controller.api;

import SCG.beyeah1211.common.Constants;
import SCG.beyeah1211.entity.beyeahGoods;
import SCG.beyeah1211.service.beyeahGoodsService;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/goods")
public class GoodsApiController {
    @Resource
    private beyeahGoodsService beyeahGoodsService;

    @GetMapping
    public Result listGoods(@RequestParam(value = "categoryId", required = false) Long categoryId,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "sortField", required = false) String sortField,
                            @RequestParam(value = "page", defaultValue = "1") Integer page,
                            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", pageSize);
        params.put("goodsSellStatus", Constants.SELL_STATUS_UP);
        if (categoryId != null && categoryId > 0) {
            params.put("goodsCategoryId", categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            params.put("keyword", keyword.trim());
        }
        if ("createTime".equals(sortField) || "publishTime".equals(sortField)) {
            params.put("orderBy", "new");
        } else if ("sellingPrice".equals(sortField) || "price".equals(sortField)) {
            params.put("orderBy", "price");
        }
        PageQueryUtil pageQueryUtil = new PageQueryUtil(params);
        PageResult pageResult = beyeahGoodsService.searchbeyeahGoods(pageQueryUtil);
        return ResultGenerator.genSuccessResult(pageResult);
    }

    @GetMapping("/{goodsId}")
    public Result goodsDetail(@PathVariable("goodsId") Long goodsId) {
        beyeahGoods goods = beyeahGoodsService.getbeyeahGoodsById(goodsId);
        return ResultGenerator.genSuccessResult(goods);
    }
}

