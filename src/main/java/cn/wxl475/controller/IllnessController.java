package cn.wxl475.controller;


import cn.wxl475.Service.IllnessService;
import cn.wxl475.client.DataClient;
import cn.wxl475.config.DefaultFeignConfiguration;
import cn.wxl475.pojo.Illness;
import cn.wxl475.pojo.Result;
import cn.wxl475.pojo.User;
import cn.wxl475.repo.IllnessEsRepo;
import cn.wxl475.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static cn.wxl475.redis.RedisConstants.CACHE_ILLNESS_KEY;

@RestController
@RequestMapping("/illness")
@EnableFeignClients(clients = DataClient.class, defaultConfiguration = DefaultFeignConfiguration.class)
@Slf4j
public class IllnessController {
    @Autowired
    private IllnessService illnessService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IllnessEsRepo illnessEsRepo;

    @PostMapping("/addIllness")
    public Result addIllness(@RequestBody Illness illness) {
        String illnessName = illness.getIllnessName();
        Illness oldIllness = illnessService.getByIllnessName(illnessName);
        if(oldIllness == null) {
            illnessService.save(illness);
            illnessEsRepo.save(illness);
            return Result.success();
        }
        else {
            return Result.error("该病例已存在");
        }
    }

    @PostMapping("/deleteIllness")
    public Result deleteIllness(@RequestBody List<Long> ids) {
        for(Long id: ids) {
            stringRedisTemplate.delete(CACHE_ILLNESS_KEY + id);
        }
        illnessService.removeBatchByIds(ids);
        illnessEsRepo.deleteAllById(ids);
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

    @GetMapping("/getByIllnessType/{illnessType}/{pageNum}/{pageSize}")
    public Result getByIllnessType(@PathVariable Integer illnessType, @PathVariable Integer pageNum, @PathVariable Integer pageSize) {
        try {
            //1.引入分页插件,pageNum是第几页，pageSize是每页显示多少条,默认查询总数count
            PageHelper.startPage(pageNum, pageSize);
            //2.紧跟的查询就是一个分页查询-必须紧跟.后面的其他查询不会被分页
            List<Illness> illnessList = illnessService.getByType(illnessType);
            //3.使用PageInfo包装查询后的结果, pageSize是连续显示的条数
            PageInfo pageInfo = new PageInfo(illnessList, pageSize);
            return Result.success(pageInfo);
        }finally {
            //清理 ThreadLocal 存储的分页参数,保证线程安全
            PageHelper.clearPage();
        }
    }

    @PostMapping("/updateIllness")
    public Result updateIllness(@RequestBody Illness illness) {
        illnessService.updateById(illness);
        illnessEsRepo.save(illness);
        stringRedisTemplate.delete(CACHE_ILLNESS_KEY + illness.getIllnessId());
        return Result.success();
    }

    @GetMapping("/getIllnessById/{illnessId}")
    public Result getIllnessById(@PathVariable Long illnessId) {
        Illness illness = illnessService.getIllnessById(illnessId);
        return Result.success(illness);
    }

    @PostMapping("/searchIllnessByKeyword")
    public Result searchIllnessByKeyword(@RequestParam(required = false) String keyword,
                                        @RequestParam Integer pageNum,
                                        @RequestParam Integer pageSize,
                                        @RequestParam(required = false) String sortField,
                                        @RequestParam(required = false) Integer sortOrder){
        if(pageNum <= 0 || pageSize <= 0){
            return Result.error("页码或页大小不合法");
        }
        return Result.success(illnessService.searchIllnessWithKeyword(keyword,pageNum,pageSize,sortField,sortOrder));
    }


}
