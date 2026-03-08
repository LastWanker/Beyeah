/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package SCG.beyeah1211.service.impl;

import SCG.beyeah1211.common.Constants;
import SCG.beyeah1211.common.ServiceResultEnum;
import SCG.beyeah1211.controller.vo.beyeahShoppingCartItemVO;
import SCG.beyeah1211.dao.beyeahGoodsMapper;
import SCG.beyeah1211.dao.beyeahShoppingCartItemMapper;
import SCG.beyeah1211.entity.beyeahGoods;
import SCG.beyeah1211.entity.beyeahShoppingCartItem;
import SCG.beyeah1211.service.beyeahShoppingCartService;
import SCG.beyeah1211.util.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class beyeahShoppingCartServiceImpl implements beyeahShoppingCartService {

    @Autowired
    private beyeahShoppingCartItemMapper beyeahShoppingCartItemMapper;

    @Autowired
    private beyeahGoodsMapper beyeahGoodsMapper;

    @Override
    public String savebeyeahCartItem(beyeahShoppingCartItem beyeahShoppingCartItem) {
        beyeahShoppingCartItem temp = beyeahShoppingCartItemMapper.selectByUserIdAndGoodsId(beyeahShoppingCartItem.getUserId(), beyeahShoppingCartItem.getGoodsId());
        if (temp != null) {
            //已存在则修改该记录
            temp.setGoodsCount(beyeahShoppingCartItem.getGoodsCount());
            return updatebeyeahCartItem(temp);
        }
        beyeahGoods beyeahGoods = beyeahGoodsMapper.selectByPrimaryKey(beyeahShoppingCartItem.getGoodsId());
        //商品为空
        if (beyeahGoods == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        int totalItem = beyeahShoppingCartItemMapper.selectCountByUserId(beyeahShoppingCartItem.getUserId()) + 1;
        //超出单个商品的最大数量
        if (beyeahShoppingCartItem.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        //超出最大数量
        if (totalItem > Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_TOTAL_NUMBER_ERROR.getResult();
        }
        //保存记录
        if (beyeahShoppingCartItemMapper.insertSelective(beyeahShoppingCartItem) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updatebeyeahCartItem(beyeahShoppingCartItem beyeahShoppingCartItem) {
        beyeahShoppingCartItem beyeahShoppingCartItemUpdate = beyeahShoppingCartItemMapper.selectByPrimaryKey(beyeahShoppingCartItem.getCartItemId());
        if (beyeahShoppingCartItemUpdate == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        //超出单个商品的最大数量
        if (beyeahShoppingCartItem.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        //当前登录账号的userId与待修改的cartItem中userId不同，返回错误
        if (!beyeahShoppingCartItemUpdate.getUserId().equals(beyeahShoppingCartItem.getUserId())) {
            return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
        }
        //数值相同，则不执行数据操作
        if (beyeahShoppingCartItem.getGoodsCount().equals(beyeahShoppingCartItemUpdate.getGoodsCount())) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        beyeahShoppingCartItemUpdate.setGoodsCount(beyeahShoppingCartItem.getGoodsCount());
        beyeahShoppingCartItemUpdate.setUpdateTime(new Date());
        //修改记录
        if (beyeahShoppingCartItemMapper.updateByPrimaryKeySelective(beyeahShoppingCartItemUpdate) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public beyeahShoppingCartItem getbeyeahCartItemById(Long beyeahShoppingCartItemId) {
        return beyeahShoppingCartItemMapper.selectByPrimaryKey(beyeahShoppingCartItemId);
    }

    @Override
    public Boolean deleteById(Long shoppingCartItemId, Long userId) {
        beyeahShoppingCartItem beyeahShoppingCartItem = beyeahShoppingCartItemMapper.selectByPrimaryKey(shoppingCartItemId);
        if (beyeahShoppingCartItem == null) {
            return false;
        }
        //userId不同不能删除
        if (!userId.equals(beyeahShoppingCartItem.getUserId())) {
            return false;
        }
        return beyeahShoppingCartItemMapper.deleteByPrimaryKey(shoppingCartItemId) > 0;
    }

    @Override
    public List<beyeahShoppingCartItemVO> getMyShoppingCartItems(Long beyeahUserId) {
        List<beyeahShoppingCartItemVO> beyeahShoppingCartItemVOS = new ArrayList<>();
        List<beyeahShoppingCartItem> beyeahShoppingCartItems = beyeahShoppingCartItemMapper.selectByUserId(beyeahUserId, Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER);
        if (!CollectionUtils.isEmpty(beyeahShoppingCartItems)) {
            //查询商品信息并做数据转换
            List<Long> beyeahGoodsIds = beyeahShoppingCartItems.stream().map(beyeahShoppingCartItem::getGoodsId).collect(Collectors.toList());
            List<beyeahGoods> beyeahGoods = beyeahGoodsMapper.selectByPrimaryKeys(beyeahGoodsIds);
            Map<Long, beyeahGoods> beyeahGoodsMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(beyeahGoods)) {
                beyeahGoodsMap = beyeahGoods.stream().collect(Collectors.toMap(SCG.beyeah1211.entity.beyeahGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
            }
            for (beyeahShoppingCartItem beyeahShoppingCartItem : beyeahShoppingCartItems) {
                beyeahShoppingCartItemVO beyeahShoppingCartItemVO = new beyeahShoppingCartItemVO();
                BeanUtil.copyProperties(beyeahShoppingCartItem, beyeahShoppingCartItemVO);
                if (beyeahGoodsMap.containsKey(beyeahShoppingCartItem.getGoodsId())) {
                    beyeahGoods beyeahGoodsTemp = beyeahGoodsMap.get(beyeahShoppingCartItem.getGoodsId());
                    beyeahShoppingCartItemVO.setGoodsCoverImg(beyeahGoodsTemp.getGoodsCoverImg());
                    String goodsName = beyeahGoodsTemp.getGoodsName();
                    // 字符串过长导致文字超出的问题
                    if (goodsName.length() > 28) {
                        goodsName = goodsName.substring(0, 28) + "...";
                    }
                    beyeahShoppingCartItemVO.setGoodsName(goodsName);
                    beyeahShoppingCartItemVO.setSellingPrice(beyeahGoodsTemp.getSellingPrice());
                    beyeahShoppingCartItemVOS.add(beyeahShoppingCartItemVO);
                }
            }
        }
        return beyeahShoppingCartItemVOS;
    }
}
