package cn.wxl475.Service;

import cn.wxl475.pojo.Illness;
import cn.wxl475.pojo.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IllnessService extends IService<Illness> {
    List<Illness> getAll();

    Illness getByIllnessName(String illnessName);

    List<Illness> getByType(String illnessType);

    Illness getIllnessById(Long illnessId);

    Page<Illness> searchIllnessWithKeyword(String keyword, Integer pageNum, Integer pageSize, String sortField, Integer sortOrder);
}
