package com.laishishui.learnlucence.dao;

import com.laishishui.learnlucence.po.Baike;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface BaikeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Baike record);

    int insertSelective(Baike record);

    Baike selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Baike record);

    int updateByPrimaryKeyWithBLOBs(Baike record);

    int updateByPrimaryKey(Baike record);

    List<Baike> getAllBaike();
}