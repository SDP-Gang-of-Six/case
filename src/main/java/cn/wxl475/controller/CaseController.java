package cn.wxl475.controller;


import cn.wxl475.Service.CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/case")
public class CaseController {
    @Autowired
    private CaseService caseService;
}
