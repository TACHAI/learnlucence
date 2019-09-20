package com.laishishui.learnlucence.controller;

import com.laishishui.learnlucence.dao.BaikeMapper;
import com.laishishui.learnlucence.po.Baike;
import com.laishishui.learnlucence.service.impl.SearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {
    @Autowired
    private BaikeMapper baikeMapper;
    @Autowired
    private SearchServiceImpl searchService;


    @GetMapping("/index")
    public String createIndex() {
        // 拉取数据
        List<Baike> baikes = baikeMapper.getAllBaike();
        searchService.write(baikes);
        return "成功";
    }

    /**
     * 搜索，实现高亮
     * @param q
     * @return
     * @throws Exception
     */
    @GetMapping("search/{q}")
    public List<Map> getSearchText(@PathVariable String q) throws Exception {
        List<Map> mapList = searchService.search(q);
        return mapList;
    }

    @GetMapping("/search")
    public ModelAndView test(ModelAndView mv) {
        mv.setViewName("/search");
        return mv;
    }

//    旧的创建索引的方式，效率较低
//          Baike baike = new Baike();
//        //获取字段
//        for (int i = 0; i < baikes.size(); i++) {
//            //获取每行数据
//            baike = baikes.get(i);
//            //创建Document对象
//            Document doc = new Document();
//            //获取每列数据
//
//            Field id = new Field("id", baike.getId()+"", TextField.TYPE_STORED);
//            Field title = new Field("title", baike.getTitle(), TextField.TYPE_STORED);
//            Field summary = new Field("summary", baike.getSummary(), TextField.TYPE_STORED);
//            //添加到Document中
//            doc.add(id);
//            doc.add(title);
//            doc.add(summary);
//            //调用，创建索引库
//            indexDataBase.write(doc);
//
//        }



}
