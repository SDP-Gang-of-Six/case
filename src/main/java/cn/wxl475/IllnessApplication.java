package cn.wxl475;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.wxl475.mapper")
public class IllnessApplication {
    public static void main(String[] args) {
        SpringApplication.run(IllnessApplication.class,args);
    }
}
