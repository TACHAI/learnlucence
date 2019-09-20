package com.laishishui.learnlucence.common.fulltest;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Create by tachai on 2019-09-19 10:49
 * gitHub https://github.com/TACHAI
 * Email tc1206966083@gmail.com
 */
@Slf4j
public class IndexUtils {

    static List<Document> doList = null;


    static Document document = null;
    static IndexSearcher searcher = null;
    static IndexWriter writer = null;
    static IndexReader reader = null;
    IndexWriter ramWriter = null;

    public static Analyzer analyzer;
    static Directory fsd;

    static Path path;

    // 静态资源加载，当类加载的时候运行（因为只要加载一次）
    static {
        log.info("lucene 初始化");
        try {
            // IKAnalyzer 中文分词器（可扩展）
            // 标准分词器：Analyzer analyzer = new StandardAnalyzer();
            // new IKAnalyzer(true)表示智能分词
            // new IKAnalyzer(false)表示最细粒度分词(默认也是这个)
            analyzer = new StandardAnalyzer();

            // 使用物理磁盘
//            path = Paths.get("mysql/keyword");// 磁盘索引库路径(相对路径)
//            fsd = FSDirectory.open(path);// 创建磁盘目录


            // 使用内存磁盘
            fsd = new RAMDirectory();

        } catch (Exception e) {
            log.error("lucence初始化发生IOException");
            e.printStackTrace();
        }
    }

    /**
     * 采集数据
     *
     * @throws IOException
     */

    public void createIndex() throws IOException {
        // 分词
        doList = analyze();
        // lucene没有提供相应的更新方法，只能先删除然后在创建新的索引(耗时)
        // 由于IndexWriter对象只能实例化一次如果使用内存和磁盘想结合的方式则需要两个IndexWriter故行不通
        // 虽然创建的时候耗时但是这样使得文件只有6个 ，搜索时减少了一些io操作加快了搜索速度
        writer = deleteAllIndex();
        for (Document doc : doList) {
            writer.addDocument(doc);
        }
        writer.commit();
        writer.close();
    }

    /**
     * 分词，工具方法
     */
    public List<Document> analyze() throws IOException {
        doList = new ArrayList<Document>();
        File resource = new ClassPathResource("mysql.txt").getFile();
        BufferedReader reader = new BufferedReader(new FileReader(resource));
        String keyword = null;
        while ((keyword = reader.readLine()) != null) {
            document = new Document();
            Field mysql = new TextField("keyword", keyword, Field.Store.YES);
            document.add(mysql);
            doList.add(document);
        }

        if (reader != null) {
            reader.close();
        }
        return doList;
    }

    /**
     * 删除索引库
     *
     * @throws IOException
     */
    public IndexWriter deleteAllIndex() throws IOException {
        writer = getWriter();
        writer.deleteAll();
        return writer;
    }


    /**
     * 获取搜索器
     */
    public static IndexSearcher getIndexSearcher(ExecutorService service) throws IOException {
        if (null == searcher) {
            reader = DirectoryReader.open(fsd);
            searcher = new IndexSearcher(reader,service);
        }
        return searcher;
    }


    /**
     * 获取搜索器
     */
    public static IndexSearcher getIndexSearcher() throws IOException {
        if (null == searcher) {
            reader = DirectoryReader.open(fsd);
            searcher = new IndexSearcher(reader);
        }
        return searcher;
    }


    /**
     * 获取磁盘写入
     */

    public  IndexWriter getWriter() throws IOException {
        if (null == writer) {
            // 为什么使用这种new 匿名方式创建该对象 IndexWriterConfig(Version.LUCENE)
            // analyzer)
            // 因为IndexWriterConfig对象只能使用一次、一次
            synchronized (this){
                writer = new IndexWriter(fsd, new IndexWriterConfig(analyzer));
            }

        }
        return writer;
    }

    /**
     * 工具方法
     */
    public static List<Document> searchUtil(Query query, IndexSearcher searcher) throws IOException {
        List<Document> docList = new ArrayList<Document>();
        TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);

        ScoreDoc[] sd = topDocs.scoreDocs;
        for (ScoreDoc score : sd) {
            int documentId = score.doc;
            Document doc = searcher.doc(documentId);
            docList.add(doc);
        }

        return docList;
    }

    /**
     * 给查询的文字上色
     */

    public static String displayHtmlHighlight(Query query, Analyzer analyzer, String fieldName, String fieldContent, int fragmentSize) throws IOException, InvalidTokenOffsetsException {
        // 创建一个高亮器
        Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<font style='font-weight:bold;'>", "</font>"),
                new QueryScorer(query));
        Fragmenter fragmenter = new SimpleFragmenter(fragmentSize);
        highlighter.setTextFragmenter(fragmenter);

        return highlighter.getBestFragment(analyzer, fieldName, fieldContent);

    }
}
