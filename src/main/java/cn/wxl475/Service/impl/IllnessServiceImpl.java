package cn.wxl475.Service.impl;


import cn.wxl475.Service.IllnessService;
import cn.wxl475.mapper.IllnessMapper;
import cn.wxl475.pojo.Illness;
import cn.wxl475.pojo.User;
import cn.wxl475.redis.CacheClient;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.wxl475.redis.RedisConstants.*;

@Service
public class IllnessServiceImpl extends ServiceImpl<IllnessMapper, Illness> implements IllnessService {
    @Autowired
    private IllnessMapper illnessMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CacheClient cacheClient;

    @Override
    @DS("slave")
    public List<Illness> getAll() {
        QueryWrapper<Illness> wrapper = new QueryWrapper<Illness>()
                .select("illness_id", "illness_name", "illness_type", "create_time", "update_time");
        return illnessMapper.selectList(wrapper);
    }

    @Override
    @DS("slave")
    public Illness getByIllnessName(String illnessName) {
        QueryWrapper<Illness> wrapper = new QueryWrapper<Illness>()
                .select("illness_id", "illness_name", "illness_type", "create_time", "update_time")
                .eq("illness_name", illnessName);
        return illnessMapper.selectOne(wrapper);
    }

    @Override
    @DS("slave")
    public List<Illness> getByType(Integer illnessType) {
        QueryWrapper<Illness> wrapper = new QueryWrapper<Illness>()
                .select("illness_id", "illness_name", "illness_type", "create_time", "update_time")
                .eq("illness_type", illnessType);
        return illnessMapper.selectList(wrapper);
    }

    @Override
    @DS("slave")
    public Illness getIllnessById(Long illnessId) {
        return cacheClient.queryWithPassThrough(
                CACHE_ILLNESS_KEY,
                LOCK_ILLNESS_KEY,
                illnessId,
                Illness.class,
                id ->  illnessMapper.selectById(illnessId),
                CACHE_ILLNESS_TTL,
                TimeUnit.MINUTES
        );
    }
}
