/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package SCG.beyeah1211.service.impl;

import SCG.beyeah1211.common.beyeahCategoryLevelEnum;
import SCG.beyeah1211.common.beyeahException;
import SCG.beyeah1211.common.ServiceResultEnum;
import SCG.beyeah1211.controller.vo.beyeahSearchGoodsVO;
import SCG.beyeah1211.dao.GoodsCategoryMapper;
import SCG.beyeah1211.dao.beyeahGoodsMapper;
import SCG.beyeah1211.entity.GoodsCategory;
import SCG.beyeah1211.entity.beyeahGoods;
import SCG.beyeah1211.service.beyeahGoodsService;
import SCG.beyeah1211.util.BeanUtil;
import SCG.beyeah1211.util.beyeahUtils;
import SCG.beyeah1211.util.PageQueryUtil;
import SCG.beyeah1211.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class beyeahGoodsServiceImpl implements beyeahGoodsService {

    @Autowired
    private beyeahGoodsMapper goodsMapper;
    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public PageResult getbeyeahGoodsPage(PageQueryUtil pageUtil) {
        List<beyeahGoods> goodsList = goodsMapper.findbeyeahGoodsList(pageUtil);
        int total = goodsMapper.getTotalbeyeahGoods(pageUtil);
        PageResult pageResult = new PageResult(goodsList, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String savebeyeahGoods(beyeahGoods goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != beyeahCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        if (goodsMapper.selectByCategoryIdAndName(goods.getGoodsName(), goods.getGoodsCategoryId()) != null) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        goods.setGoodsName(beyeahUtils.cleanString(goods.getGoodsName()));
        goods.setGoodsIntro(beyeahUtils.cleanString(goods.getGoodsIntro()));
        goods.setTag(beyeahUtils.cleanString(goods.getTag()));
        if (goodsMapper.insertSelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public void batchSavebeyeahGoods(List<beyeahGoods> beyeahGoodsList) {
        if (!CollectionUtils.isEmpty(beyeahGoodsList)) {
            goodsMapper.batchInsert(beyeahGoodsList);
        }
    }

    @Override
    public String updatebeyeahGoods(beyeahGoods goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != beyeahCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        beyeahGoods temp = goodsMapper.selectByPrimaryKey(goods.getGoodsId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        beyeahGoods temp2 = goodsMapper.selectByCategoryIdAndName(goods.getGoodsName(), goods.getGoodsCategoryId());
        if (temp2 != null && !temp2.getGoodsId().equals(goods.getGoodsId())) {
            //name和分类id相同且不同id 不能继续修改
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        goods.setGoodsName(beyeahUtils.cleanString(goods.getGoodsName()));
        goods.setGoodsIntro(beyeahUtils.cleanString(goods.getGoodsIntro()));
        goods.setTag(beyeahUtils.cleanString(goods.getTag()));
        goods.setUpdateTime(new Date());
        if (goodsMapper.updateByPrimaryKeySelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public beyeahGoods getbeyeahGoodsById(Long id) {
        beyeahGoods beyeahGoods = goodsMapper.selectByPrimaryKey(id);
        if (beyeahGoods == null) {
            beyeahException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        return beyeahGoods;
    }

    @Override
    public Boolean batchUpdateSellStatus(Long[] ids, int sellStatus) {
        return goodsMapper.batchUpdateSellStatus(ids, sellStatus) > 0;
    }

    @Override
    public PageResult searchbeyeahGoods(PageQueryUtil pageUtil) {
        List<beyeahGoods> goodsList = goodsMapper.findbeyeahGoodsListBySearch(pageUtil);
        int total = goodsMapper.getTotalbeyeahGoodsBySearch(pageUtil);
        List<beyeahSearchGoodsVO> beyeahSearchGoodsVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            beyeahSearchGoodsVOS = BeanUtil.copyList(goodsList, beyeahSearchGoodsVO.class);
            for (beyeahSearchGoodsVO beyeahSearchGoodsVO : beyeahSearchGoodsVOS) {
                String goodsName = beyeahSearchGoodsVO.getGoodsName();
                String goodsIntro = beyeahSearchGoodsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                    beyeahSearchGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 30) {
                    goodsIntro = goodsIntro.substring(0, 30) + "...";
                    beyeahSearchGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        PageResult pageResult = new PageResult(beyeahSearchGoodsVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }
}
