package com.easy.custom.functionquery;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * solrconfig.xml里面配置示例
 <valueSourceParser name="myfunc" class="com.easy.custom.functionquery.MyValueParser" >
 <lst name="strategy">
 <int name="maxYears">100</int>
 <int name="money_maxTimes">100</int>
 <int name="money_base">100</int>
 </lst>
 </valueSourceParser>
 *
 *
 *
 */
public class MyValueParser extends ValueSourceParser {


    final static Logger log= LoggerFactory.getLogger(MyValueParser.class);

    //接受传过来的参数


    // handle configuration parameters
    // passed through solrconfig.xml
    public void init(NamedList args) {
        //得到一个映射之后，转成NamedList便于操作，用于solrconfig.xml读取配置项

//        log.info("初始化加权因子参数：  maxYears:{} money_maxTimes:{}  money_base:{} ",maxYears,money_maxTimes,money_base);
    }

    @Override
    public ValueSource parse(FunctionQParser fq) throws SyntaxError {
        return new FunctionValueSource(fq.parseValueSourceList());
    }
}

