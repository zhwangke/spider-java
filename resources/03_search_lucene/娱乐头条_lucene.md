# 娱乐头条 _search_lucene

今日内容:

* lucene:
  * 搜索引擎基本概念: 理解
  * lucene的基本概念
  * lucene的CURD操作 :  
  * lucene的高级内容:  
    * **lucene的高亮**
    * lucene的排序
    * lucene的分页
    * lucene的加权因子



## 1. 搜索引擎

###1.1  什么是搜索引擎

​	搜索引擎是指根据一定的策略、运用特定的计算机程序从互联网上搜集信息，在对信息进行组织和处理后，为用户提供检索服务，将用户检索相关的信息展示给用户的系统。例如: 百度 谷歌

### 1.2  搜索引擎基本的运行原理

![](图片\搜索系统基本运行原理png.png)

### 1.3 原始数据库查询的缺陷

* 1)  慢, 当数据库中的数据量很庞大的时候, 整个的查询效率非常低, 无法及时返回内容
* 2) 搜索效果比较差, 只能根据用户输入的完整关键字的进行首尾的模糊匹配
* 3)  如果用户输入的关键字出现错别字, 或者多输入了内容, 可能就导致结果远离用户期望的内容

### 1.4 倒排索引技术  (理解)

​	倒排索引, 又称为反向索引: 以字或者词,甚至是一句话一段话作为一个关键字进行索引, 每一个关键字都会对应着一个记录项, 记录项中记录了这个关键字出现在那些文档中, 已经在此文档的什么位置上						

![](图片\倒排索引.png)

为什么说倒排索引可以提升查询的效率和精准度呢?

​	倒排索引, 是将数据提前按照格式分词放好,建立索引, 当用户进行搜索, 将用户的关键字进行分词, 然后根据分词后的单词到索引库中寻找对应词条,根据词条, 查到对应所在的文档位置, 将其文档内容直接获取即可



整个查询过程 **基于索引**查询

##2.Lucene

![](图片\1532265270996.png)

​	Lucene是Apache提供的一个开源的全文检索引擎工具包, 其本质就是一堆jar包而已, 而非一个完整的搜索引擎, 但是我们可以通过Lucene来构建一个搜索引擎

官方网址:  http://lucene.apache.org/

### 2.1 : Lucene 与solr的关系

* Lucene: 底层的api, 工具包
* solr: 基于Lucene开发的企业级的搜索引擎产品   

### 2.2 使用Lucene如何构建索引

#### 2.2.1 第一步: 导入相关的jar包(pom依赖)

```xml
		<dependency>
			<groupId>org.apache.lucene</groupId>
			<artifactId>lucene-core</artifactId>
			<version>4.10.2</version>
		</dependency>
		<dependency>
			<groupId>org.apache.lucene</groupId>
			<artifactId>lucene-queries</artifactId>
			<version>4.10.2</version>
		</dependency>
		<dependency>
			<groupId>org.apache.lucene</groupId>
			<artifactId>lucene-test-framework</artifactId>
			<version>4.10.2</version>
		</dependency>
		<dependency>
			<groupId>org.apache.lucene</groupId>
			<artifactId>lucene-analyzers-common</artifactId>
			<version>4.10.2</version>
		</dependency>
		<dependency>
			<groupId>org.apache.lucene</groupId>
			<artifactId>lucene-queryparser</artifactId>
			<version>4.10.2</version>
		</dependency>
		<dependency>
			<groupId>org.apache.lucene</groupId>
			<artifactId>lucene-highlighter</artifactId>
			<version>4.10.2</version>
		</dependency>	
```

#### 2.2.2 第二步: 书写写入索引的代码

![img](图片\写入索引.jpg)

```java
// 索引写入相关的内容
public class IndexWriterTest {

    public static void main(String[] args) throws  Exception{
        //1. 创建 lucene用于写入索引的核心类: IndexWriter
        Directory d = FSDirectory.open(new File("F:\\index"));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(Version.LATEST,analyzer);

        IndexWriter indexWriter = new IndexWriter(d,conf);

        //2. 添加原始文档数据
        Document doc = new Document();
        doc.add(new LongField("id",1, Field.Store.YES));
        doc.add(new StringField("title","母爱满满！哈里王子梅根王妃母亲节晒宝宝脚丫", Field.Store.YES));
        doc.add(new TextField("content","哈里王子梅根王妃在社交媒体上发图文", Field.Store.YES));

        indexWriter.addDocument(doc);

        //3. 提交文档数据
        indexWriter.commit();

        //4. 释放资源
        indexWriter.close();

    }
}

```

### 2.3 索引查看工具

* 今日资料中打开索引查看工具, 执行run.bat

![](图片\索引查看工具01.png)

![](图片\索引查看工具02.png)

![](图片\索引查看工具03.png)

> lucene会自己进行维护原始文档的唯一值, 不需要程序员自己维护

### 2.4 API详解

* IndexWriter: 索引写入器对象

  其主要的作用: 添加索引, 修改索引和删除索引

  * 创建此对象的时候, 需要传入Directory和indexWriterConfig对象

* Directory: 目录类, 用来指定索引库的目录

  * 常用的实现类:
    * **FSDirectory**: 用来指定文件系统的目录, 将索引信息保存到磁盘上
      * 优点: 索引可以进行长期保存, 安全系数高
      * 缺点: 读取略慢   
    * RAMDriectory: 内存目录, 将索引库信息存放到内存中
      * 优点: 读取速度快
      * 缺点: 不安全, 无法长期保存, 关机后就消失了

* IndexWriterConfig: 索引写入器的配置类

  * 创建此对象, 需要传递Lucene的版本和分词器
  * 作用:
    * 作用1 : 指定Lucene的版本和需要使用的分词器
    * 作用2: 设置Lucene的打开索引库的方式: setOpenMode();

```java
		//参数值: APPEND CREATE   CREATE_OR_APPEND
        /**
         * APPEND: 表示追加, 如果索引库存在, 就会向索引库中追加数据, 如果索引库不存在, 直接报错
         * 
         * CREATE: 表示创建, 不管索引库有没有, 每一次都是重新创建一个新的索引库
         * 
         * CREATE_OR_APPEND: 如果索引库有, 就会追加, 如果没有 就会创建索引库
         		默认值也是 CREATE_OR_APPEND
         */
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
```

* Document: 文档

​       在Lucene中, 每一条数据以文档的形式进行存储, 文档中也有其对应的属性(字段)和值, Lucene中一个文档类似数据库的一个表, 表中的字段类似于文档中的字段,只不过这个文档只能保存一条数据

​	Document看做是一个文件, 文件的属性就是文档的属性, 文件对应属性的值就是文档的属性的值 content

* 一个文档中可以有多个字段, 每一个字段就是一个field对象,不同的文档可以有不同的属性
* 字段也有其对应数据类型, 故Field类也提供了各种数据类型的实现类

| Field类                                                      | 数据类型               | Analyzed是否分词 | Indexed是否索引 | Stored是否存储 | 说明                                                         |
| ------------------------------------------------------------ | ---------------------- | ---------------- | --------------- | -------------- | ------------------------------------------------------------ |
| StringField(FieldName, FieldValue,Store.YES))                | 字符串                 | N                | Y               | Y或N           | 这个Field用来构建一个字符串Field，但是不会进行分析，会将整个串存储在索引中，比如(订单号,姓名等)是否存储在文档中用Store.YES或Store.NO决定 |
| LongField(FieldName, FieldValue,Store.YES)                   | Long型                 | Y                | Y               | Y或N           | 这个Field用来构建一个Long数字型Field，进行分析和索引，比如(价格)是否存储在文档中用Store.YES或Store.NO决定 |
| StoredField(FieldName, FieldValue)                           | 重载方法，支持多种类型 | N                | N               | Y              | 这个Field用来构建不同类型Field不分析，不索引，但要Field存储在文档中 |
| TextField(FieldName, FieldValue, Store.NO)或TextField(FieldName, reader) | 字符串或流             | Y                | Y               | Y或N           | 如果是一个Reader, lucene猜测内容比较多,会采用Unstored的策略. |

名称解释:

​	分析: 是否将字段的值进行分词

​	索引: 指的是能否被搜索

​	是否保存: 指的的原始数据初始值是否需要保存

​      如果一个字段中的值可以被分词, 那么必然是支持搜索的

* Analyzer: 分词器:

  ​	用于对文档中的数据进行分词, 其分词的效果取决于分词器的选择, Lucene中根据各个国家制定了各种语言的分词器,对中文有一个ChineseAnalyzer 但是其分词的效果, 是将中文进行一个一个字的分开

  针对中文分词一般只能使用第三方的分词词:

![](图片\第三方中文分词器.png)

​	一般采用IK分词器

### 2.5 集成IK分词器

![](图片\ik概述.png)

说明: ik分词器官方版本并不支持Lucene4.x版本, 有人基本官方版本做了改进, 使其支持Lucene4.x

![](图片\ik.png)

* 基本使用:
  * 导入相关依赖, 将分词器切换成ikanalyzer即可

```xml
		<!-- 引入IK分词器 -->
		<dependency>
			<groupId>com.janeluo</groupId>
			<artifactId>ikanalyzer</artifactId>
			<version>2012_u6</version>
		</dependency>
```

![](图片\切换ik.png)

* 高级使用:

  ik分词器在2012年更新后, 就在没有更新, 其原因就取决于其强大的扩展功能,以保证ik能够持续使用

  * ik支持对自定义词库, 其可以定义两个扩展的词典
    * 1) 扩展词典（新创建词功能）:有些词IK分词器不识别 例如：“传智播客”，“碉堡了”
    * 2) 停用词典（停用某些词功能）有些词不需要建立索引  例如：“哦”，“啊”，“的”

* 如何使用:

![](图片\ik使用1.png)

​	将此三个文件复制到项目中

![](图片\ik使用2.png)

​	接着在ext.dic中设置需要进行分词的内容即可, 在stopword中设置不被分词的内容即可



注意点:

​	1)    需要将配置文件中 扩展词典的注释打开

​	2)  在使用ik分词器的时候, 添加文档数据中, 必须要有一个字段的类型是TextField字段, 否则无法分词

### 2.6 查询索引

#### 2.6.1 查询入门:

```java
  //索引查询的入门代码
public class IndexSearcherTest {


    public static void main(String[] args) throws  Exception {
        //1. 创建lucene用于查询索引核心对象
        IndexReader r  = DirectoryReader.open(FSDirectory.open(new File("F:\\index")));
        IndexSearcher indexSearcher = new IndexSearcher(r);

        //2. 添加查询的条件
        //2.1 通过查询解析器的方式来获取query对象
        QueryParser queryParser = new QueryParser("content",new IKAnalyzer());
        // 参数: 表示的是用户输入的内容
        Query query = queryParser.parse("蓝瘦香菇吊炸天");
        //3. 执行查询
        // 参数1: 查询的条件
        //参数2:  查询前几个
        // 返回值: 结果集
        //      主要包含二部分的内容:  1)  查询的结果集的得分数组   2) 查询的总条数
        TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);

        //4. 获取数据
        ScoreDoc[] scoreDocs = topDocs.scoreDocs; // 得分文档的数组
        int totalHits = topDocs.totalHits; //查询的总条数
        // ScoreDoc :得分文档对象, 包含二部分内容:  1)  文档的id  2) 文档的得分
        for (ScoreDoc scoreDoc : scoreDocs) {
            float score = scoreDoc.score; //得分
            int docId = scoreDoc.doc; //文档的id

            Document document = indexSearcher.doc(docId);
            String id = document.get("id");
            String title = document.get("title");
            String content = document.get("content");

            System.out.println("文档的得分为:" + score +" 文档的id:"+id+" 文档的标题:"+title+" 文档的内容:"+ content);
        }
    }
}
```

### 2.7 查询相关API详解

* IndexSearcher: Lucene中查询对象, 用来执行查询和排序操作
  * 常用方法:
    * search(Query query, int n);//执行查询
      * 参数1:  查询条件
      * 参数2: 返回的最大条数
    * search(Query query, int n,Sort sort);
      * 参数1: 查询的条件
      * 参数2: 返回的最大的条数
      * 参数3: 排序
    * doc(int id);//根据文档id查询文档对象
* IndexReader: 索引库读取工具
  * 使用DirectoryReader来打开索引库
* Query:查询对象
  * 获取方式:
    * 通过查询解析器
      * 单字段的解析器: queryParse
      * 多字段的解析器: multiFieldQueryParse
    * 使用Lucene自定义的实现类 : 多样化的查询
      * Lucene中提供了五种常用的多样化的查询
* TopDocs:查询结果对象
  * 第一部分: 查询到的总条数
    * int  topDocs.totalHits
  * 第二部分: 得分文档的数组
    * ScoreDoc[]  topDocs.scoreDocs;
* ScoreDoc: 得分文档对象
  * 第一部分: 文档的id (lucene自己进行维护的id值)
    * topDoc.doc
  * 第二部分: 文档的得分 (匹配度的得分)
    * topDoc.score

### 2.8 多样化查询(特殊查询)

* 一次性添加多条数据

```java
   // 一次性添加多条数据
    @Test
    public void indexWriterManyTest() throws  Exception{
        //1. 创建lucene用于添加索引的核心对象
        Directory r = FSDirectory.open(new File("F:\\index"));
        IndexWriterConfig conf = new IndexWriterConfig(Version.LATEST,new IKAnalyzer());
        IndexWriter indexWriter = new IndexWriter(r,conf);

        //2. 添加文档数据
        List<Document> docs = new ArrayList<Document>();

        Document doc1 = new Document();
        doc1.add(new LongField("id",1, Field.Store.YES));
        doc1.add(new TextField("title","杭州女子退货18件", Field.Store.YES));
        doc1.add(new TextField("content","网友评论, 吊炸天了", Field.Store.YES));

        Document doc2 = new Document();
        doc2.add(new LongField("id",2, Field.Store.YES));
        doc2.add(new TextField("title","美国贸易战争, 加2500亿关税", Field.Store.YES));
        doc2.add(new TextField("content","中国新闻报道说, 愿谈愿谈, 能打则打", Field.Store.YES));


        Document doc3 = new Document();
        doc3.add(new LongField("id",3, Field.Store.YES));
        doc3.add(new TextField("title","高考马上就要到了", Field.Store.YES));
        doc3.add(new TextField("content","再有一个月, 高考就要来临了, 希望都中状元", Field.Store.YES));

        Document doc4 = new Document();
        doc4.add(new LongField("id",4, Field.Store.YES));
        doc4.add(new TextField("title","lucene的简介", Field.Store.YES));
        doc4.add(new TextField("content","lucene是apache的开源的全文检索引擎的工具包, 使用lucene来构建一个搜索引擎", Field.Store.YES));


        docs.add(doc1);
        docs.add(doc2);
        docs.add(doc3);
        docs.add(doc4);

        indexWriter.addDocuments(docs);

        //3. 提交数据
        indexWriter.commit();

        //4. 释放资源
        indexWriter.close();

    }
```

* 抽取一个公共的查询的方法

```java
// 抽取一个公共的查询的方法
    public void  publicQuery(Query query) throws  Exception{
        //1. 创建lucene用于查询索引核心对象
        IndexReader r  = DirectoryReader.open(FSDirectory.open(new File("F:\\index")));
        IndexSearcher indexSearcher = new IndexSearcher(r);

        //2. 添加查询的条件

        TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);

        //4. 获取数据
        ScoreDoc[] scoreDocs = topDocs.scoreDocs; // 得分文档的数组
        int totalHits = topDocs.totalHits; //查询的总条数
        // ScoreDoc : 包含二部分内容:  1)  文档的id  2) 文档的得分
        for (ScoreDoc scoreDoc : scoreDocs) {
            float score = scoreDoc.score; //得分
            int docId = scoreDoc.doc; //文档的id

            Document document = indexSearcher.doc(docId);
            String id = document.get("id");
            String title = document.get("title");
            String content = document.get("content");

            System.out.println("文档的得分为:" + score +" 文档的id:"+id+" 文档的标题:"+title+" 文档的内容:"+ content);
        }

    }
```



#### 2.8.1 词条查询: TermQuery

```java
// 多样化的查询:  TermQuery
    // 词条查询 :  词条 是一个不可在分割的单词, 可以是一个词语, 也可以是一句话或者一段话
    // 根据词条查询,参数必须是一个词条, 而且这个词条必须在索引库中是存在的, 否则都查询不到
    // 词条查询适合于: 适合于查询那种不需要进行分词的字段, 例如id查询 例如根据名字查询
   
    @Test
    public void termQueryTest() throws  Exception{
        TermQuery query = new TermQuery(new Term("title","杭州"));

        publicQuery(query);
    }
```

#### 2.8.2 通配符查询: WildcardQuery

```java
    // 多样化查询:  WildcardQuery  通配符查询  一定要和mysql中like查询区分开  _ 和 %
    //  通配符:  ?  *										mysql:like查询:  _  %
    //       ?  :  表示占用一个字符的位置, 这个位置任意的内容
    //       *  :  表示占用0~n个字符的位置 这个位置任意的内容
    @Test
    public void wildcardQueryTest() throws  Exception{

        WildcardQuery query = new WildcardQuery(new Term("title","lucene*"));

        publicQuery(query);
    }
```

#### 2.8.3 模糊查询: FuzzyQuery

```java
    //多样化查询： FuzzyQuery  模糊查询
    // 模糊查询:  最大的编辑(修正)次数   2
     //       编辑:  替换, 移动, 补位  组合在一块最多只能错2次
    //      每一个修正都是一个一个字符修改的, 不能一次性改正多个字符
    // 支持修改最大的编辑次数, 但是修改的范围 :0~2 如果修改的范围不是这个范围内,那么就会报错
	//  过半机制:  一旦错误率达到50% 以上, 也会影响最大的编辑次数
    @Test
    public void fuzzyQueryTest() throws  Exception{
        FuzzyQuery query = new FuzzyQuery(new Term("title","luc"),2);

        publicQuery(query);
    }
```

#### 2.8.4 数值范围查询: NumericRangeQuery

```java
    // 多样化查询:  NumericRangeQuery 数值范围查询
    //  创建对象的方法是通过静态的方法来创建的:  使用那个静态的方法创建对象取决于 field查询字段的类型
     /*
                NumericRangeQuery.newLongRange();
                NumericRangeQuery.newFloatRange();
                NumericRangeQuery.newDoubleRange();
                NumericRangeQuery.newIntRange();
           方法的参数:
                1)  field:  查询的字段
                2) min :   最小值
                3) max :  最大值
                4) minInclesive:  是否包含最小值
                5) maxInclesive:  是否包含最大值
                6) 步长: 每次间隔多少   默认为 1 
     */
    @Test
    public void numericRangeQueryTest() throws  Exception{

        NumericRangeQuery query = NumericRangeQuery.newLongRange("id",0L,4L,true,true);

        publicQuery(query);
    }
```

#### 2.8.5 组合查询: BooleanQuery

```java
// 多样化查询:  BooleanQuery  组合查询
    //   组合查询本身自己是没有任何的条件的, 将其他的条件组合在一块, 共同作用于着整个的查询

    /**
     *   MUST : 必须,    表示查询出来的结果必须是这个条件中的数据
     *   MUST_NOT:  不必须 , 表示查询出来的结果必须不能包含这个条件中的数据
     *   SHOULD:   应该 ,   表示查询出来的结果, 如果有这个条件中数据就显示, 没有就不显示, 对整个结果没有太大影响
     */
    @Test
    public void booleanQueryTest()throws  Exception{
        BooleanQuery query = new BooleanQuery();

        NumericRangeQuery numericRangeQuery = NumericRangeQuery.newLongRange("id",2L,3L,true,true);
        query.add(numericRangeQuery, BooleanClause.Occur.MUST);

        TermQuery termQuery = new TermQuery(new Term("title","lucene"));
        query.add(termQuery, BooleanClause.Occur.MUST);

        publicQuery(query);
    }
```

### 2.8 Lucene的索引修改

```java
 // 索引的修改
    //  修改后的数据都在文档的最后面,   先删除, 后添加
    @Test
    public void updateIndexTest() throws  Exception{
        //1. 创建indexWriter对象
        Directory d = FSDirectory.open(new File("F:\\index"));
        IndexWriterConfig conf = new IndexWriterConfig(Version.LATEST,new IKAnalyzer());
        IndexWriter indexWriter = new IndexWriter(d,conf);

        //2. 创建一个修改后文档对象

        Document doc = new Document();
        doc.add(new StringField("id","6", Field.Store.YES));
        doc.add(new TextField("title","这是修改后的数据", Field.Store.YES));

        //3. 执行修改 : 在执行修改的时候, 不要使用id字段, 因为id字段在书写的时候使用的Long类型, long类型是要分词的
        //  如果想根据id来修改数据, 需要将id的类型更改为StringField(不分词)
        indexWriter.updateDocument(new Term("title","杭州"),doc);

        //4. 提交修改
        indexWriter.commit();

        //5. 释放资源
        indexWriter.close();
    }
```

### 2.9 Lucene 的索引删除

```java
    // 索引的删除

    @Test
    public void deleteIndexTest() throws  Exception{
        //1. 创建indexWriter对象 :  增删改
        Directory d = FSDirectory.open(new File("F:\\index"));
        IndexWriterConfig conf = new IndexWriterConfig(Version.LATEST,new IKAnalyzer());
        IndexWriter indexWriter = new IndexWriter(d,conf);

        //2. 执行删除索引操作
        //indexWriter.deleteAll(); //删除所有
        indexWriter.deleteDocuments(new Term("title","修改"));  // 根据词条删除
        //indexWriter.deleteDocuments(query);  根据条件删除

        //3. 提交
        indexWriter.commit();

        //4. 释放资源
        indexWriter.close();
    }
```

## 3. Lucene的高级内容(了解)

### 3.1 Lucene的高亮显示

* 高亮: 实际上高亮其实就给对应的字段关键词添加一个HTML标签,并设置其css样式即可

#### 3.1.1 Lucene的高亮实现

```java
// 高亮展示数据
public class HighlighterTest {

    public static void main(String[] args) throws  Exception {
        //1.  创建一个查询索引的核心对象: indexSearcher
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("F:\\index")));
        IndexSearcher indexSearcher = new IndexSearcher(reader);

        //2. 添加查询的条件
        QueryParser queryParser = new QueryParser("content",new IKAnalyzer());
        Query query = queryParser.parse("lucene是一个全文搜索的工具包");

        // 高亮的设置 -------------------------------------------
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color='red'>","</font>");
        QueryScorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter,scorer);
        // 高亮的设置 -------------------------------------------
        //3. 执行查询 : topDocs 1) 包含分数的文档的数组  2)  总条数
        TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);

        //4. 获取数据
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        int totalHits = topDocs.totalHits; //总条数
        // scoreDoc  :  1) 文档的id  2) 文档的分数
        for (ScoreDoc scoreDoc : scoreDocs) {
            float score = scoreDoc.score;//文档的分数
            int docId = scoreDoc.doc;// 文档的id

            Document document = indexSearcher.doc(docId);

            String id = document.get("id");
            String title = document.get("title");
            String content = document.get("content");

            // 获取高亮的内容
            content = highlighter.getBestFragment(new IKAnalyzer(), "content", content);
            String title1 = highlighter.getBestFragment(new IKAnalyzer(), "title", title);
            if(title1 != null){

                title = title1;
            }

            System.out.println("文档的得分:" + score + " 文档id: "+ id  + " 文档标题: "+ title +" 文档的内容"+content);
        }

    }
}
```

### 3.2 Lucene的排序

> 一旦排序后, 就会失去匹配度的得分, 是因为如果没有排序, 默认排序方案会按照匹配度进行排序的

```java
// 高亮展示数据
public class HighlighterTest {

    public static void main(String[] args) throws  Exception {
        //1.  创建一个查询索引的核心对象: indexSearcher
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("F:\\index")));
        IndexSearcher indexSearcher = new IndexSearcher(reader);

        //2. 添加查询的条件
        QueryParser queryParser = new QueryParser("content",new IKAnalyzer());
        Query query = queryParser.parse("lucene是一个全文搜索的工具包");

        // 高亮的设置 -------------------------------------------
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color='red'>","</font>");
        QueryScorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter,scorer);
        // 高亮的设置 -------------------------------------------
        //3. 执行查询 : topDocs 1) 包含分数的文档的数组  2)  总条数
        Sort sort = new Sort();
        sort.setSort(new SortField("id", SortField.Type.LONG,true)); // 默认是按照升序方案排序, 加上reverse为true, 表示倒序
        // 注意一旦添加了排序操作后, 就会丢失按照匹配度进行排序的
        TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE,sort);

        //4. 获取数据
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        int totalHits = topDocs.totalHits; //总条数
        // scoreDoc  :  1) 文档的id  2) 文档的分数
        for (ScoreDoc scoreDoc : scoreDocs) {
            float score = scoreDoc.score;//文档的分数
            int docId = scoreDoc.doc;// 文档的id

            Document document = indexSearcher.doc(docId);

            String id = document.get("id");
            String title = document.get("title");
            String content = document.get("content");

            // 获取高亮的内容
            content = highlighter.getBestFragment(new IKAnalyzer(), "content", content);
            String title1 = highlighter.getBestFragment(new IKAnalyzer(), "title", title);
            if(title1 != null){

                title = title1;
            }

            System.out.println("文档的得分:" + score + " 文档id: "+ id  + " 文档标题: "+ title +" 文档的内容"+content);
        }

    }
}
```

### 3.3 Lucene的分页 

> lucene本身是不支持分页的, 所以分页的代码,都是通过代码的形式手动实现的
>
> mysql中 limit ? , ?     第一个问号表示从第几条开始  第二问号表示每次向下查询多少个

```java
// 分页
	@Test
	public void testPageQuery() throws Exception {
		// 实际上Lucene本身不支持分页。因此我们需要自己进行逻辑分页。我们要准备分页参数：
		int pageSize = 2;// 每页条数
		int pageNum = 1;// 当前页码
		int start = (pageNum - 1) * pageSize;// 当前页的起始条数
		int end = start + pageSize;// 当前页的结束条数（不能包含）
		
		// 目录对象
		Directory directory = FSDirectory.open(new File("indexDir"));
		// 创建读取工具
		IndexReader reader = DirectoryReader.open(directory);
		// 创建搜索工具
		IndexSearcher searcher = new IndexSearcher(reader);
		
		QueryParser parser = new QueryParser("title", new IKAnalyzer());
		Query query = parser.parse("谷歌地图");
		
		// 创建排序对象,需要排序字段SortField，参数：字段的名称、字段的类型、是否反转如果是false，升序。true降序
		Sort sort = new Sort(new SortField("id", Type.LONG, false));
		// 搜索数据，查询0~end条
		TopDocs topDocs = searcher.search(query, end,sort);
		System.out.println("本次搜索共" + topDocs.totalHits + "条数据");
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (int i = start; i < end; i++) {
			ScoreDoc scoreDoc = scoreDocs[i];
			// 获取文档编号
			int docID = scoreDoc.doc;
			Document doc = reader.document(docID);
			System.out.println("id: " + doc.get("id"));
			System.out.println("title: " + doc.get("title"));
		}
	}
```

### 3.4 Lucene的加权因子(激励因子)

> 百度竞价排名 :  当用户输入某个关键词, 让某一个网站排名靠前
>
> ​	百度: 当用户输入一个关键词, 点击某个网站  , 如果当大量的用户访问同一个关键词, 进入同一个网站, 百度认为, 用户输入这个关键词, 进入网站可能性是最大的, 为了提高用户的体验度, 会让这个网站排名靠前



* Lucene会对搜索的结果的匹配度进行一个得分, 用来表示数据和词条关联性的强弱, 得分越高, 表示匹配度越高, 排名越靠前
* Lucene支持对某一个字段设置加权因子, 来提高其打分, 使其排名更加靠前, 这样当用户搜索的时候, 便可以将此词条对应的文档展示在最前面

```java
TextField textField = new TextField("content",
				"学习lucene需要掌握搜索引擎的基本原理和lucene创建索引和查询索引,boots", Store.YES);
textField.setBoost(10); // 默认1 如果更改为10 表示匹配度扩大10倍  
```


