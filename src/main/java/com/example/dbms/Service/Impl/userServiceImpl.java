package com.example.dbms.Service.Impl;

import com.example.dbms.Pojo.Result;
import com.example.dbms.Pojo.SharedDataStorage;
import com.example.dbms.Service.userService;
import com.example.dbms.Utils.FileUtil;
import com.example.dbms.Utils.PasswordEncoderUtil;
import com.example.dbms.Utils.XMLUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class userServiceImpl implements userService {
    XMLUtil xmlUtil;
    @Override
    public Result register(String username, String password){
        try {
            if(!FileUtil.findUser(username)) {
                this.xmlUtil = new XMLUtil("./resources/static/users/" + username);
                String encodePassword = PasswordEncoderUtil.encodePassword(password);
                xmlUtil.createUser(username, encodePassword);
                SharedDataStorage.getInstance().setThreadLocalData(username);
                System.out.println(SharedDataStorage.getInstance().getThreadLocalData());

                return new Result(0,"success register user",null);}
            else{
                return new Result(1,"user already exists",null);
            }

        }catch(Exception e){
            return Result.error(e.getMessage());
        }

    }

    @Override
    public Result login(String username,String password) {
        try{
        this.xmlUtil=new XMLUtil("./resources/static/users/"+username);
        String encodePassword= xmlUtil.getPassWord(username);
        if(PasswordEncoderUtil.matches(password,encodePassword)){
            SharedDataStorage.getInstance().setThreadLocalData(username);
            System.out.println(SharedDataStorage.getInstance().getThreadLocalData());
            return new Result(0,"success",null);
        }
        return Result.error("用户名或密码错误");}
        catch(Exception e){
            return Result.error(e.getMessage());
        }
    }
}
