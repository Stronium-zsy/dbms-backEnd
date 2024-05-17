package com.example.dbms.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result <T>{
    private Integer code;
    private String message;
    private T data;

    public static<E> Result<E> success(E data){

        return new Result<>(0,"success",data);
    }
    public static<E> Result<E> success(String message,E data){
        return new Result<>(0,message,data);
    }
    public static Result success(){
        return new Result(0,"success",null);
    }
    public static Result error(String message){
        System.out.println(message);
        return new Result(1,message,null);
    }
}
