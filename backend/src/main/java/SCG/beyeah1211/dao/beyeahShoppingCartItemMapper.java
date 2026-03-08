/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package SCG.beyeah1211.dao;

import SCG.beyeah1211.entity.beyeahShoppingCartItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface beyeahShoppingCartItemMapper {
    int deleteByPrimaryKey(Long cartItemId);

    int insert(beyeahShoppingCartItem record);

    int insertSelective(beyeahShoppingCartItem record);

    beyeahShoppingCartItem selectByPrimaryKey(Long cartItemId);

    beyeahShoppingCartItem selectByUserIdAndGoodsId(@Param("beyeahUserId") Long beyeahUserId, @Param("goodsId") Long goodsId);

    List<beyeahShoppingCartItem> selectByUserId(@Param("beyeahUserId") Long beyeahUserId, @Param("number") int number);

    int selectCountByUserId(Long beyeahUserId);

    int updateByPrimaryKeySelective(beyeahShoppingCartItem record);

    int updateByPrimaryKey(beyeahShoppingCartItem record);

    int deleteBatch(List<Long> ids);
}