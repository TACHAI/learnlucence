package com.laishishui.learnlucence.common.fulltest;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tachai on 2019-09-19 12:01
 * gitHub https://github.com/TACHAI
 * Email tc1206966083@gmail.com
 */
public class SearchMethod extends IndexUtils{

    List<Document> doList = null;

    /**
     * 通过lucence最小单元term进行查询
     */
    public List<Document> searchByTermQuery(Term term)throws IOException{

        searcher = getIndexSearcher();
        Query query = new TermQuery(term);
        doList = searchUtil(query,searcher);
        return doList;
    }


    /**
     * 前缀查询
     * @param term
     * @return
     * @throws IOException
     */
    public Map<List<Document>,Query> searchByPrefixQuery(Term term) throws IOException{
        Map<List<Document>,Query> map = new HashMap<List<Document>,Query>();
        searcher = getIndexSearcher();
        Query query = new PrefixQuery(term);
        doList = searchUtil(query,searcher);
        map.put(doList, query);
        return map;
    }


    /**
     * 智能提示
     * @param field
     * @param content
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public List<String> suggestion(String field,String content) throws ParseException, IOException{

        List<String> strList = new ArrayList<String>();
        QueryParser qp = new QueryParser(field, analyzer);
        Query query =  qp.parse(content);
        searcher = getIndexSearcher();
        TopDocs topDoc = searcher.search(query, Integer.MAX_VALUE);
        ScoreDoc[] sd = topDoc.scoreDocs;
        for (ScoreDoc score : sd) {
            int documentId = score.doc;
            Document doc = searcher.doc(documentId);
            String str = doc.get(field);
            strList.add(str);
        }
        return strList;
    }

    /**
     * QueryParser 会对输入的语句进行分词然后查询
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public Map<List<Document>,Query> searchByQueryParser(String field, String content) throws ParseException, IOException{
        Map<List<Document>,Query> map = new HashMap<List<Document>,Query>();
        QueryParser qp = new QueryParser(field, analyzer);
        Query query =  qp.parse(content);
        searcher = getIndexSearcher();
        map.put(searchUtil(query,searcher), query);

        return map;
    }
    /**
     * 多域、多条件查询
     * @param fields    域数组
     * @param queries    查询数组
     * @param flags        域之间的关系
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public List<Document> searcherByMultiFieldQueryParser(String[] fields, String[] queries, BooleanClause.Occur[] flags) throws ParseException, IOException{

        Query mfQuery = MultiFieldQueryParser.parse(queries, fields, flags, analyzer);
        searcher = getIndexSearcher();
        doList = searchUtil(mfQuery,searcher);
        return doList;
    }


}

