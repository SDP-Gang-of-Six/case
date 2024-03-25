package cn.wxl475.Service.impl;


import cn.wxl475.Service.IllnessService;
import cn.wxl475.mapper.IllnessMapper;
import cn.wxl475.pojo.Illness;
import cn.wxl475.pojo.Image;
import cn.wxl475.pojo.User;
import cn.wxl475.redis.CacheClient;
import cn.wxl475.repo.IllnessEsRepo;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private IllnessEsRepo illnessEsRepo;

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
    public List<Illness> getByType(String illnessType) {
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

    @Override
    public List<Illness> searchIllnessWithKeyword(String keyword, Integer pageNum, Integer pageSize, String sortField, Integer sortOrder) {
        List<Illness> illnesses = new ArrayList<>();
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().withPageable(PageRequest.of(pageNum - 1, pageSize));
        if(keyword != null && !keyword.isEmpty()){
            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(keyword,"illnessName", "illnessType", "symptom", "process", "consequence", "schedule"));
        }
        if(sortField == null || sortField.isEmpty()){
            sortField = "illnessId";
        }
        if(sortOrder == null || !(sortOrder == 1 || sortOrder == -1)){
            sortOrder = -1;
        }
        queryBuilder.withSorts(SortBuilders.fieldSort(sortField).order(sortOrder == -1? SortOrder.DESC: SortOrder.ASC));
        SearchHits<Illness> hits = elasticsearchRestTemplate.search(queryBuilder.build(), Illness.class);
        hits.forEach(illness -> illnesses.add(illness.getContent()));
        return illnesses;
    }
}
