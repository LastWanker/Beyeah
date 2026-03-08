/**
 * 涓ヨ們澹版槑锛? * 寮€婧愮増鏈鍔″繀淇濈暀姝ゆ敞閲婂ご淇℃伅锛岃嫢鍒犻櫎鎴戞柟灏嗕繚鐣欐墍鏈夋硶寰嬭矗浠昏拷绌讹紒
 * 鏈郴缁熷凡鐢宠杞欢钁椾綔鏉冿紝鍙楀浗瀹剁増鏉冨眬鐭ヨ瘑浜ф潈浠ュ強鍥藉璁＄畻鏈鸿蒋浠惰憲浣滄潈淇濇姢锛? * 鍙甯稿垎浜拰瀛︿範婧愮爜锛屼笉寰楃敤浜庤繚娉曠姱缃椿鍔紝杩濊€呭繀绌讹紒
 * Copyright (c) 2019-2020 鍗佷笁 all rights reserved.
 * 鐗堟潈鎵€鏈夛紝渚垫潈蹇呯┒锛? */
package SCG.beyeah1211.controller.vo;

import SCG.beyeah1211.entity.GoodsCategory;

import java.io.Serializable;
import java.util.List;

/**
 * 鎼滅储椤甸潰鍒嗙被鏁版嵁VO
 */
public class SearchPageCategoryVO implements Serializable {

    private String firstLevelCategoryName;

    private List<GoodsCategory> secondLevelCategoryList;

    private String secondLevelCategoryName;

    private List<GoodsCategory> thirdLevelCategoryList;

    private String currentCategoryName;

    public String getFirstLevelCategoryName() {
        return firstLevelCategoryName;
    }

    public void setFirstLevelCategoryName(String firstLevelCategoryName) {
        this.firstLevelCategoryName = firstLevelCategoryName;
    }

    public List<GoodsCategory> getSecondLevelCategoryList() {
        return secondLevelCategoryList;
    }

    public void setSecondLevelCategoryList(List<GoodsCategory> secondLevelCategoryList) {
        this.secondLevelCategoryList = secondLevelCategoryList;
    }

    public String getSecondLevelCategoryName() {
        return secondLevelCategoryName;
    }

    public void setSecondLevelCategoryName(String secondLevelCategoryName) {
        this.secondLevelCategoryName = secondLevelCategoryName;
    }

    public List<GoodsCategory> getThirdLevelCategoryList() {
        return thirdLevelCategoryList;
    }

    public void setThirdLevelCategoryList(List<GoodsCategory> thirdLevelCategoryList) {
        this.thirdLevelCategoryList = thirdLevelCategoryList;
    }

    public String getCurrentCategoryName() {
        return currentCategoryName;
    }

    public void setCurrentCategoryName(String currentCategoryName) {
        this.currentCategoryName = currentCategoryName;
    }
}

