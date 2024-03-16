package cn.wxl475.Service.impl;


import cn.wxl475.Service.IllnessService;
import cn.wxl475.mapper.IllnessMapper;
import cn.wxl475.pojo.Illness;
import cn.wxl475.redis.CacheClient;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return illnessMapper.selectList(null);
    }

    @Override
    @DS("slave")
    public Illness getByIllnessName(String illnessName) {
        QueryWrapper<Illness> wrapper = new QueryWrapper<Illness>().eq("illness_name", illnessName);
        return illnessMapper.selectOne(wrapper);
    }
}
