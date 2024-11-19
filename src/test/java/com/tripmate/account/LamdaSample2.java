package com.tripmate.account;

import java.util.function.Function;

public class LamdaSample2 {

    // Function<인자값 타입, 반환값 타입> 이를 람다로 표현하면
    // 인자값 -> 반환값 타입의 메소드
    public static void sample1(Function<String, Integer> func) {
        System.out.println(func.apply("1"));
    }

    public static void main(String[] args) {
        LamdaSample2.sample1(s -> Integer.parseInt(s) + 1);

    }
}
