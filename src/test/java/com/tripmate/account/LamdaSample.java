package com.tripmate.account;

import java.util.function.Supplier;

/**
 * 람다 구조 (매개변수) -> { 실행할 코드 }
 * 람다를 쓰는 이유는 메소드를 인자값으로 전달해서 전달한 메소드 내부에서 실행 시점을 정할 수 있도록 처리하기 위함
 */
public class LamdaSample {

    // Supplier 는 () -> 결과 값을 리턴하는 함수를 쓴다
    public void sample1(Supplier<String> func) {
        System.out.println("위에서 다른 메소드를 실행 하거나 if 문으로 실행할지 말지 정할 수 있음");
        // func.get() 을 호출해서 실행 하는 시점을 정할 수 있음
        System.out.println(func.get());

        System.out.println("또는 한번도 호출 가능");
        System.out.println(func.get());
    }

    // static 메소드
    public static void sample2(Supplier<String> func) {
        System.out.println(func.get());
    }

    public String call1() {
        return "call1";
    }

    public static String call2() {
        return "call2";
    }

    public static void main(String[] args) {
        LamdaSample lamda = new LamdaSample();

        // 일반 적으로 아래처럼 사용하는건 LamdaSample.call2() 를 먼저 실행하고
        // 결과를 lamda.sample1(실행 결과) 메서드에서 인자로 받아서 실행됨
        // lamda.sample1(LamdaSample.call2());

        // 람다는 위에처럼 실행되는게 아니라 메소드 자체를 전달해서 sample2 내부에서 실행 시점을 정할 수 있게함
        // lamda.sample2(() -> "test") 또는 lamda.sample2(LamdaSample::call2)
        // () -> "" 는 익명함수를 의미함

        // 1. [ () -> 응답값 ] 이렇게 쓰면 익명 함수를 전달함
        lamda.sample1(() -> "test");

        // 아래 메소드가 1번처럼 줄여서 표현 할 수 있음
        lamda.sample1(() -> {
            return "test";
        });

        // 익명 함수 아니라도 동일한 형태의 메소드를 인자값으로 전달 할 수 있음

        // 2. 객체를 생성 했을 때는 lamda.call1() 을 lamd::call1 로 표현
        lamda.sample1(lamda::call1);
        // 3. 스타틱 메소드는 LamdaSample.call2() 대신 LamdaSample::call2 로 표현
        lamda.sample1(LamdaSample::call2);

        // 4. 꼭 객체를 생성하지 않고 스타틱 메소드를 호출 할 수 있음
        LamdaSample.sample2(() -> "test2");
        // 5. 2번 처럼 사용가능
        LamdaSample.sample2(lamda::call1);
        //6. 3번 처럼 사용 가능
        LamdaSample.sample2(LamdaSample::call2);

        // 익명 함수 안에 변수를 넣어서 메소드를 호출 할 수 있음
        LamdaSample.sample2(() -> lamda.call1());
        // 익명 함수 안에 변수를 넣어서 메소드를 호출 할 수 있음
        LamdaSample.sample2(() -> LamdaSample.call2());
    }
}
