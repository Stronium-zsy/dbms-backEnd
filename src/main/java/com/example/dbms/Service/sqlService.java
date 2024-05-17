package com.example.dbms.Service;
import com.example.dbms.Pojo.Result;
import org.springframework.stereotype.Service;

import java.util.List;
public interface sqlService {
    public Result processSql(String sql);
}
