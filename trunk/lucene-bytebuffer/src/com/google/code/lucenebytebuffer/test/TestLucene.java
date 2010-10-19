package com.google.code.lucenebytebuffer.test;

import java.io.IOException;
import java.util.Random;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class TestLucene {
    
    

    private static void addDoc(IndexWriter w, String value) throws IOException {
        Document doc = new Document();
        doc.add(new Field("title", value, Field.Store.YES, Field.Index.ANALYZED));
        w.addDocument(doc);
      }
    
    public static void main(String[] args) {
        final int ELEMENTS = 500000;
        Directory index = new RAMDirectory();
        IndexWriter w;
        try {            
            Runtime runtime = Runtime.getRuntime();
            
            Analyzer a = new StandardAnalyzer();
            w = new IndexWriter(index,a , true,
                    IndexWriter.MaxFieldLength.UNLIMITED);
            
            addDoc(w, "Lucene in Action");
            addDoc(w, "Lucene for Dummies");
            addDoc(w, "Managing Gigabytes");
            addDoc(w, "The Art of Computer Science");
            
            long l1 = System.nanoTime();
            for(int i=0;i<ELEMENTS;i++)
                addDoc(w,"Lucene" + i);
            long l2 = System.nanoTime();
            
            System.gc();
            long m1 = runtime.totalMemory() - runtime.freeMemory();
            w.close();
            
            long l3 = System.nanoTime();
            Random r = new Random();
            for(int qi=0;qi<100;qi++){
                int qii = r.nextInt(ELEMENTS);
                String querystr = "title:lucene"+qii;
                Query q = new QueryParser(Version.LUCENE_CURRENT, "title", a).parse(querystr);
    
                
                int hitsPerPage = 10;
                IndexSearcher searcher = new IndexSearcher(index, true);
                //System.out.println("Max Doc reuturns : " + searcher.maxDoc());
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
    
                System.out.println("Found " + hits.length + " hits.");
                for(int i=0;i<hits.length;++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    System.out.println((i + 1) + ". " + d.get("title") + " String " + querystr);
                }
            }
            long l4 = System.nanoTime();
            System.out.println("Memory taken after indexing " + m1/1048576);
            System.out.println("Time taken for indexing " + (l2-l1)/1000000);
            System.out.println("Time taken for querying " + (l4-l2)/1000000);            
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (LockObtainFailedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {            
            e.printStackTrace();
        }
    }


}
