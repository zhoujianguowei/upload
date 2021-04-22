package test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hello {
    private static final Logger LOGGER= LoggerFactory.getLogger(Hello.class);
    public static void main(String[] args) {
        System.out.println("hello world");
        LOGGER.info("first app");
    }
}
