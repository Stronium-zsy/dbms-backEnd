package com.example.dbms.Service.Impl;

import com.example.dbms.Pojo.Result;
import com.example.dbms.Service.sqlService;
import com.example.dbms.Utils.sqlParserUtil;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class sqlServiceImpl implements sqlService {
    private sqlParserUtil sqlUtil=new sqlParserUtil();

    @Override
    public Result processSql(String sql) {
        System.out.println(sql);

        return sqlUtil.processSql(sql);

    }


}
