package com.example.dbms.Service;

import com.example.dbms.Pojo.Result;

public interface userService {
    public Result login(String username, String password);
    public Result register(String username,String password);
}
