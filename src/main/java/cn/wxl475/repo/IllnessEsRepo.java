package cn.wxl475.repo;

import cn.wxl475.pojo.Illness;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface IllnessEsRepo extends ElasticsearchRepository<Illness, Long> {
}
