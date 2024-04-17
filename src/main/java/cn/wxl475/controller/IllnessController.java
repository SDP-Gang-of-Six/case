package cn.wxl475.controller;


import cn.wxl475.Service.IllnessService;
import cn.wxl475.client.DataClient;
import cn.wxl475.config.DefaultFeignConfiguration;
import cn.wxl475.pojo.Illness;
import cn.wxl475.pojo.Result;
import cn.wxl475.repo.IllnessEsRepo;
import cn.wxl475.utils.JwtUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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

    @Value("${jwt.signKey}")
    private String signKey;

    @Value("${jwt.expire}")
    private Long expire;

    @PostMapping("/addIllness")
    public Result addIllness(@RequestHeader("Authorization") String token, @RequestBody Illness illness) {
        Claims claims = JwtUtils.parseJWT(token, signKey);
        Boolean userType = (Boolean) claims.get("userType");
        if(!userType) {
            return Result.error("无增加病例权限");
        }
        String illnessName = illness.getIllnessName();
        String illnessType = illness.getIllnessType();
        String symptom = illness.getSymptom();
        String process = illness.getProcess();
        String consequence = illness.getConsequence();
        String schedule = illness.getSchedule();
        if(illnessName == null || illnessName.isEmpty()) {
            return Result.error("病例名称不能为空");
        }
        if(illnessType == null || illnessType.isEmpty()) {
            return Result.error("病例类型不能为空");
        }
        if(symptom == null || symptom.isEmpty()) {
            return Result.error("症状不能为空");
        }
        if(process == null || process.isEmpty()) {
            return Result.error("检查过程不能为空");
        }
        if(consequence == null || consequence.isEmpty()) {
            return Result.error("诊断结果不能为空");
        }
        if(schedule == null || schedule.isEmpty()) {
            return Result.error("治疗方案不能为空");
        }
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
    public Result deleteIllness(@RequestHeader("Authorization") String token, @RequestBody List<Long> ids) {
        Claims claims = JwtUtils.parseJWT(token, signKey);
        Boolean userType = (Boolean) claims.get("userType");
        if(!userType) {
            return Result.error("无删除病例权限");
        }
        for(Long id: ids) {
            stringRedisTemplate.delete(CACHE_ILLNESS_KEY + id);
        }
        illnessService.removeBatchByIds(ids);
        illnessEsRepo.deleteAllById(ids);
        return Result.success();
    }

    @GetMapping("/illnessPage/{pageNum}/{pageSize}")
    public Result illnessPage(@RequestHeader("Authorization") String token, @PathVariable Integer pageNum, @PathVariable Integer pageSize){
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
    public Result getByIllnessType(@RequestHeader("Authorization") String token, @PathVariable String illnessType, @PathVariable Integer pageNum, @PathVariable Integer pageSize) {
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
    public Result updateIllness(@RequestHeader("Authorization") String token, @RequestBody Illness illness) {
        Claims claims = JwtUtils.parseJWT(token, signKey);
        Boolean userType = (Boolean) claims.get("userType");
        if(!userType) {
            return Result.error("无修改病例权限");
        }
        String illnessName = illness.getIllnessName();
        Illness oldIllness = illnessService.getByIllnessName(illnessName);
        if(oldIllness != null && !oldIllness.getIllnessId().equals(illness.getIllnessId())) {
            return Result.error("病例名不能重复");
        }
        illnessService.updateById(illness);
        illnessEsRepo.save(illness);
        stringRedisTemplate.delete(CACHE_ILLNESS_KEY + illness.getIllnessId());
        return Result.success();
    }

    @GetMapping("/getIllnessById/{illnessId}")
    public Result getIllnessById(@RequestHeader("Authorization") String token, @PathVariable Long illnessId) {
        Illness illness = illnessService.getIllnessById(illnessId);
        return Result.success(illness);
    }

    @PostMapping("/searchIllnessByKeyword")
    public Result searchIllnessByKeyword(@RequestHeader("Authorization") String token,
                                         @RequestParam(required = false) String keyword,
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
