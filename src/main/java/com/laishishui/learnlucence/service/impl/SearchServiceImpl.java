package com.laishishui.learnlucence.service.impl;

import com.laishishui.learnlucence.common.fulltest.BaiKeBeanIndex;
import com.laishishui.learnlucence.common.fulltest.IKAnalyzer;
import com.laishishui.learnlucence.common.fulltest.SearchMethod;
import com.laishishui.learnlucence.dao.BaikeMapper;
import com.laishishui.learnlucence.po.Baike;
import com.laishishui.learnlucence.service.SearchService;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.ws.Action;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Create by tachai on 2019-09-19 20:30
 * gitHub https://github.com/TACHAI
 * Email tc1206966083@gmail.com
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private BaikeMapper baikeMapper;

    @Override
    public String write(List<Baike> baikes) {
        try {
            int totalCount = baikes.size();
            int perThreadCount = 3000;
            int threadCount = totalCount/perThreadCount + (totalCount%perThreadCount == 0 ? 0 : 1);
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch countDownLatch1 = new CountDownLatch(1);
            CountDownLatch countDownLatch2 = new CountDownLatch(threadCount);

            for(int i = 0; i < threadCount; i++) {
                int start = i*perThreadCount;
                int end = (i+1) * perThreadCount < totalCount ? (i+1) * perThreadCount : totalCount;
                List<Baike> subList = baikes.subList(start, end);
                Runnable runnable = new BaiKeBeanIndex(countDownLatch1, countDownLatch2, subList);
                //子线程交给线程池管理
                pool.execute(runnable);
            }
            countDownLatch1.countDown();
            System.out.println("开始创建索引");
            //等待所有线程都完成
            countDownLatch2.await();
            //线程全部完成工作
            System.out.println("所有线程都创建索引完毕");
            //释放线程池资源
            pool.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 搜索
     * @param value
     * @return
     * @throws Exception
     */
    public List<Map> search(String value){
        List<Map> list = new ArrayList<>();

        ExecutorService service = Executors.newCachedThreadPool();
        // 定义分词器 IKAnalyzer （中文分词器）
//        Analyzer analyzer = new IKAnalyzer();
        Analyzer analyzer = new StandardAnalyzer();
        try {
            IndexSearcher searcher = SearchMethod.getIndexSearcher(service);
            String[] fields = {"title","summary"};
            //构造Query对象
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields,analyzer);

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            String line = value != null ? value : in.readLine();
            Query query = parser.parse(line);
            //最终被分词后添加的前缀和后缀处理器，默认是粗体<B></B>
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<font color=").append("\"").append("red").append("\"").append(">");
            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter(stringBuffer.toString(),"</font>");
            //高亮搜索的词添加到高亮处理器中
            Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));

            //获取搜索的结果，指定返回document返回的个数
            //默认搜索结果为显示第一页，1000 条，可以优化
            TopDocs results = searcher.search(query, Integer.MAX_VALUE);

//            TopDocs results = SearchMethod.searchUtil(query,searcher);
            ScoreDoc[] hits = results.scoreDocs;


            //遍历，输出
            for (int i = 0; i < hits.length; i++) {
                int id = hits[i].doc;
                float score = hits[i].score;
                Document hitDoc = searcher.doc(hits[i].doc);
                Map map=new HashMap();
                map.put("id", hitDoc.get("id"));

                //获取到summary
                String name=hitDoc.get("summary");
                //将查询的词和搜索词匹配，匹配到添加前缀和后缀
                TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "summary", analyzer);
                //传入的第二个参数是查询的值
                TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, name, false, 10);
                String baikeValue="";
                for (int j = 0; j < frag.length; j++) {
                    if ((frag[j] != null) && (frag[j].getScore() > 0)) {
//                    if ((frag[j] != null)) {
                        //获取 summary 的值
                        baikeValue=baikeValue+((frag[j].toString()));
                    }
                }

                //获取到title
                String title=hitDoc.get("title");
                TokenStream titleTokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "title", analyzer);
                TextFragment[] titleFrag = highlighter.getBestTextFragments(titleTokenStream, title, false, 10);
                String titleValue="";
                for (int j = 0; j < titleFrag.length; j++) {
                    if ((frag[j] != null)) {
                        titleValue=titleValue+((titleFrag[j].toString()));
                    }
                }
                map.put("title", titleValue);
                map.put("summary", baikeValue);
                map.put("score",score);
                list.add(map);
            }
        }catch (IOException e){
            e.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }finally {
            service.shutdownNow();
        }
        return list;
    }

}
