/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package SCG.beyeah1211.dao;

import SCG.beyeah1211.entity.beyeahGoods;
import SCG.beyeah1211.entity.StockNumDTO;
import SCG.beyeah1211.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface beyeahGoodsMapper {
    int deleteByPrimaryKey(Long goodsId);

    int insert(beyeahGoods record);

    int insertSelective(beyeahGoods record);

    beyeahGoods selectByPrimaryKey(Long goodsId);

    beyeahGoods selectByCategoryIdAndName(@Param("goodsName") String goodsName, @Param("goodsCategoryId") Long goodsCategoryId);

    int updateByPrimaryKeySelective(beyeahGoods record);

    int updateByPrimaryKeyWithBLOBs(beyeahGoods record);

    int updateByPrimaryKey(beyeahGoods record);

    List<beyeahGoods> findbeyeahGoodsList(PageQueryUtil pageUtil);
    @Select("SELECT * FROM beyeah_mall_goods_info ORDER BY selling_price DESC LIMIT #{limit}")
    List<beyeahGoods> findHotGoodsList(@Param("limit") int limit);
    int getTotalbeyeahGoods(PageQueryUtil pageUtil);

    List<beyeahGoods> selectByPrimaryKeys(List<Long> goodsIds);

    List<beyeahGoods> findbeyeahGoodsListBySearch(PageQueryUtil pageUtil);

    int getTotalbeyeahGoodsBySearch(PageQueryUtil pageUtil);

    int batchInsert(@Param("beyeahGoodsList") List<beyeahGoods> beyeahGoodsList);

    int updateStockNum(@Param("stockNumDTOS") List<StockNumDTO> stockNumDTOS);

    int recoverStockNum(@Param("stockNumDTOS") List<StockNumDTO> stockNumDTOS);

    int batchUpdateSellStatus(@Param("orderIds")Long[] orderIds,@Param("sellStatus") int sellStatus);

}