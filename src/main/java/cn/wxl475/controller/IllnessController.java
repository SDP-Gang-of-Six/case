package cn.wxl475.controller;


import cn.wxl475.Service.IllnessService;
import cn.wxl475.client.DataClient;
import cn.wxl475.config.DefaultFeignConfiguration;
import cn.wxl475.pojo.Illness;
import cn.wxl475.pojo.Result;
import cn.wxl475.pojo.User;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.wxl475.redis.RedisConstants.CACHE_ILLNESS_KEY;

@RestController
@RequestMapping("/illness")
@EnableFeignClients(clients = DataClient.class, defaultConfiguration = DefaultFeignConfiguration.class)
public class IllnessController {
    @Autowired
    private IllnessService illnessService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/addIllness")
    public Result addIllness(@RequestBody Illness illness) {
        String illnessName = illness.getIllnessName();
        Illness oldIllness = illnessService.lambdaQuery()
                .eq(Illness::getIllnessName, illnessName)
                .one();
        if(oldIllness == null || oldIllness.getDeleted() == true) {
            illnessService.save(illness);
            return Result.success();
        }
        else {
            return Result.error("该病例已存在");
        }
    }

    @PostMapping("/deleteIllness")
    public Result deleteIllness(@RequestBody List<Illness> illnesses) {
        for(Illness illness: illnesses) {
            illnessService.lambdaUpdate()
                    .set(Illness::getDeleted, true)
                    .eq(Illness::getIllnessId, illness.getIllnessId())
                    .update();
            stringRedisTemplate.delete(CACHE_ILLNESS_KEY + illness.getIllnessId());
        }
        return Result.success();
    }

    @GetMapping("/illnessPage/{pageNum}/{pageSize}")
    public Result illnessPage(@PathVariable Integer pageNum, @PathVariable Integer pageSize){
        try {
            //1.引入分页插件,pageNum是第几页，pageSize是每页显示多少条,默认查询总数count
            PageHelper.startPage(pageNum, pageSize);
            //2.紧跟的查询就是一个分页查询-必须紧跟.后面的其他查询不会被分页
            List<Illness> illnessList = illnessService.getAll();
            //3.使用PageInfo包装查询后的结果, pageSize是连续显示的条数
            PageInfo pageInfo = new PageInfo(illnessList, pageSize);
            return Result.success(pageInfo);
        }finally {
            //清理 ThreadLocal 存储的分页参数,保证线程安全
            PageHelper.clearPage();
        }
    }
}
