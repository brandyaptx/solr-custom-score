package com.easy.custom.customparser;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

/**
 * Created by Administrator on 2017/3/24 0024.
 */
public class SimpleQParserPlugin extends QParserPlugin {
    @SuppressWarnings("rawtypes")
    @Override
    public void init(NamedList args) {
    }

    @Override
    public QParser createParser(String qstr, SolrParams localParams,
                                SolrParams params, SolrQueryRequest req) {
        return new SimpleQParser(qstr, localParams,params, req);
    }
}
