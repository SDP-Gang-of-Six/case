package cn.wxl475.Service.impl;


import cn.wxl475.Service.CaseService;
import cn.wxl475.mapper.CaseMapper;
import cn.wxl475.pojo.Case;
import cn.wxl475.pojo.User;
import cn.wxl475.redis.CacheClient;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CaseServiceImpl extends ServiceImpl<CaseMapper, Case> implements CaseService {
    @Autowired
    private CaseMapper caseMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CacheClient cacheClient;
}
