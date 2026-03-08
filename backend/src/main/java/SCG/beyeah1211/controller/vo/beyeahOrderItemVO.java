/**
 * 涓ヨ們澹版槑锛? * 寮€婧愮増鏈鍔″繀淇濈暀姝ゆ敞閲婂ご淇℃伅锛岃嫢鍒犻櫎鎴戞柟灏嗕繚鐣欐墍鏈夋硶寰嬭矗浠昏拷绌讹紒
 * 鏈郴缁熷凡鐢宠杞欢钁椾綔鏉冿紝鍙楀浗瀹剁増鏉冨眬鐭ヨ瘑浜ф潈浠ュ強鍥藉璁＄畻鏈鸿蒋浠惰憲浣滄潈淇濇姢锛? * 鍙甯稿垎浜拰瀛︿範婧愮爜锛屼笉寰楃敤浜庤繚娉曠姱缃椿鍔紝杩濊€呭繀绌讹紒
 * Copyright (c) 2019-2020 鍗佷笁 all rights reserved.
 * 鐗堟潈鎵€鏈夛紝渚垫潈蹇呯┒锛? */
package SCG.beyeah1211.controller.vo;

import java.io.Serializable;

/**
 * 璁㈠崟璇︽儏椤甸〉闈㈣鍗曢」VO
 */
public class beyeahOrderItemVO implements Serializable {

    private Long orderItemId;

    private Long goodsId;

    private Integer goodsCount;

    private String goodsName;

    private String goodsCoverImg;

    private Integer sellingPrice;

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsCoverImg() {
        return goodsCoverImg;
    }

    public void setGoodsCoverImg(String goodsCoverImg) {
        this.goodsCoverImg = goodsCoverImg;
    }

    public Integer getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Integer sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getGoodsCount() {
        return goodsCount;
    }

    public void setGoodsCount(Integer goodsCount) {
        this.goodsCount = goodsCount;
    }
}

