package com.easy.custom.customparser;

/**
 * Created by Administrator on 2017/3/24 0024.
 */

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DisMaxQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.util.SolrPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class SimpleQParser extends DisMaxQParser {
    private final Logger LOG = LoggerFactory.getLogger(SimpleQParser.class);
    // using low level Term query? For internal search usage.
    private boolean useLowLevelTermQuery = false;
    private float tiebreaker = 1f;
    private static Float mainBoost = 1.0f;
    private static Float frontBoost = 1.0f;
    private static Float rearBoost = 1.0f;
    private String userQuery = "";
//    final IndexSchema schema = req.getSchema();

    public SimpleQParser(String qstr, SolrParams localParams,
                         SolrParams params, SolrQueryRequest req) {
        super(qstr, localParams, params, req);
    }

    @Override

    public Query parse() throws SyntaxError {
        SolrParams solrParams = SolrParams.wrapDefaults(this.localParams, this.params);
        queryFields = SolrPluginUtils.parseFieldBoosts(solrParams.getParams(DisMaxParams.QF));
        if (0 == queryFields.size()) {
            queryFields.put(req.getSchema().getDefaultSearchFieldName(), 1.0f);
        }

        /* the main query we will execute.  we disable the coord because
         * this query is an artificial construct
         */
        BooleanQuery.Builder query = new BooleanQuery.Builder();
        addMainQuery(query, solrParams);
        // rewrite q parameter for highlighting
        if(useLowLevelTermQuery) {
            query = new BooleanQuery.Builder();
            rewriteAndOrQuery(userQuery, query, solrParams);
        }
        addBoostQuery(query, solrParams);
        addBoostFunctions(query, solrParams);
        return query.build();
    }

    protected boolean addMainQuery(BooleanQuery.Builder query, SolrParams solrParams)throws SyntaxError {
        tiebreaker = solrParams.getFloat(DisMaxParams.TIE, 0.0f);
        // get the comma separated list of fields used for payload

        /*
         * a parser for dealing with user input, which will convert things to
         * DisjunctionMaxQueries
         */
        SolrPluginUtils.DisjunctionMaxQueryParser up = getParser(queryFields,DisMaxParams.QS, solrParams, tiebreaker);

        /* * * Main User Query * * */
        parsedUserQuery = null;
        userQuery = getString();
        altUserQuery = null;
        if (userQuery == null || userQuery.trim().length() < 1) {
            // If no query is specified, we may have an alternate
            altUserQuery = getAlternateUserQuery(solrParams);
            query.add(altUserQuery, BooleanClause.Occur.MUST);
        } else {
            // There is a valid query string
            userQuery = SolrPluginUtils.partialEscape(SolrPluginUtils.stripUnbalancedQuotes(userQuery)).toString();
            userQuery = SolrPluginUtils.stripIllegalOperators(userQuery).toString();

            // use low level Term for constructing TermQuery or BooleanQuery.
            // warning: for internal AND, OR query, in order to integrate with Solr for obtaining highlight
            String luceneQueryText = userQuery;
//            String q = solrParams.get(CommonParams.Q);
//            if(q!=null && (q.indexOf("AND")!=-1 || q.indexOf("OR")!=-1)) {
//                addBasicAndOrQuery(luceneQueryText, query, solrParams);
//                luceneQueryText = query.toString();
//                useLowLevelTermQuery = true;
//            }

            LOG.debug("userQuery=" + luceneQueryText);
            ///在这之前加入分词模块也可以简单实用dismax代码解决问题
            // todo
            parsedUserQuery = getUserQuery(luceneQueryText, up, solrParams);

            BooleanQuery rewritedQuery = rewriteQueries(parsedUserQuery);
            query.add(rewritedQuery, BooleanClause.Occur.MUST);
        }
        return true;
    }

    protected void rewriteAndOrQuery(String userQuery, BooleanQuery.Builder query, SolrParams solrParams)throws SyntaxError {
        addBasicAndOrQuery(userQuery, query, solrParams);
    }

    /**
     * Parse mixing MUST and SHOULD query defined by us,
     * e.g. 首都OR北京OR北平AND首博OR首都博物馆
     * @param userQuery
     * @param query
     * @param solrParams
     * @throws ParseException
     */
    protected void addBasicAndOrQuery(String userQuery, BooleanQuery.Builder query, SolrParams solrParams)throws SyntaxError {
        userQuery = SolrPluginUtils.partialEscape(SolrPluginUtils.stripUnbalancedQuotes(userQuery)).toString();
        userQuery = SolrPluginUtils.stripIllegalOperators(userQuery).toString();
        LOG.debug("userQuery=" + userQuery);
        BooleanQuery parsedUserQuery = new BooleanQuery(true);
        String[] a = userQuery.split("\\s*AND\\s*");
        String q = "";
        if(a.length==0) {
            createTermQuery(parsedUserQuery, userQuery);
        } if(a.length>=3) {
            if(userQuery.indexOf("OR")==-1) { // e.g. 首都AND北京AND北平
                BooleanQuery andBooleanQuery = parseAndQuery(a);
                parsedUserQuery.add(andBooleanQuery, BooleanClause.Occur.MUST);
            }
        } else{
            if(a.length>0) {
                q = a[0].trim();
                if(q.indexOf("OR")!=-1 || q.length()>0) {
                    parsedUserQuery.add(parseOrQuery(q, frontBoost), BooleanClause.Occur.MUST);
                }
            }
            if(a.length==2) {
                q = a[1].trim();
                if(q.indexOf("OR")!=-1 || q.length()>0) {
                    parsedUserQuery.add(parseOrQuery(q, rearBoost), BooleanClause.Occur.MUST);
                }
            }
        }
        parsedUserQuery.setBoost(mainBoost);
        BooleanQuery rewritedQuery = rewriteQueries(parsedUserQuery);
        query.add(rewritedQuery, BooleanClause.Occur.MUST);
    }

    /**
     * Parse SHOULD query, e.g. 北京OR北平OR首都
     * @param ors
     * @param boost
     * @return
     */
    private BooleanQuery parseOrQuery(String ors, Float boost) {
        BooleanQuery bq = new BooleanQuery(true);
        for(String or : ors.split("\\s*OR\\s*")) {
            if(!or.isEmpty()) {
                createTermQuery(bq, or.trim());
            }
        }
        bq.setBoost(boost);
        return bq;
    }

    /**
     * Create TermQuery for some term text, query fields.
     * @param bq
     * @param qsr
     */
    private void createTermQuery(BooleanQuery bq, String qsr) {
        for(String field : queryFields.keySet()) {
            TermQuery tq = new TermQuery(new Term(field, qsr));
            if(queryFields.get(field)!=null) {
                tq.setBoost(queryFields.get(field));
            }
            bq.add(tq, BooleanClause.Occur.SHOULD);
        }
    }

    /**
     * Parse MUST query, e.g. 首都AND北京AND北平
     * @param ands
     * @return
     */
    private BooleanQuery parseAndQuery(String[] ands) {
        BooleanQuery andBooleanQuery = new BooleanQuery(true);
        for(String and : ands) {
            if(!and.isEmpty()) {
                BooleanQuery bq = new BooleanQuery(true);
                createTermQuery(bq, and);
                andBooleanQuery.add(bq, BooleanClause.Occur.MUST);
            }
        }
        return andBooleanQuery;
    }

    /**
     * Rewrite a query, especially a {@link BooleanQuery}, whose
     * subclauses maybe include {@link BooleanQuery}s, {@link DisjunctionMaxQuery}s,
     * {@link TermQuery}s, {@link PhraseQuery}s, {@link }s, etc.
     * @param input
     * @return
     */
    private BooleanQuery rewriteQueries(Query input) {
        BooleanQuery output = new BooleanQuery(true);
        if(input instanceof BooleanQuery) {
            BooleanQuery bq = (BooleanQuery) input;
            for(BooleanClause clause : bq.clauses()) {
                if(clause.getQuery() instanceof DisjunctionMaxQuery) {
                    BooleanClause.Occur occur = clause.getOccur();
                     output.add(rewriteDisjunctionMaxQueries((DisjunctionMaxQuery) clause.getQuery()), occur); // BooleanClause.Occur.SHOULD
                } else {
                    output.add(clause.getQuery(), clause.getOccur());
                }
            }
        } else if(input instanceof DisjunctionMaxQuery) {
             output.add(rewriteDisjunctionMaxQueries((DisjunctionMaxQuery) input), BooleanClause.Occur.SHOULD); // BooleanClause.Occur.SHOULD
        }
        output.setBoost(input.getBoost()); // boost main clause
        return output;
    }

    /**
     * Rewrite the {@link DisjunctionMaxQuery}, because of default parsing
     * query string to {@link PhraseQuery}s which are not what we want.
     * @param input
     * @return
     */
    private BooleanQuery rewriteDisjunctionMaxQueries(DisjunctionMaxQuery input) {
        // input e.g. (content:"吉林" content:"长白山" ） | （title:"吉林"^1.5 title:"长白山"^1.5 )
        Map<String, BooleanQuery> m = new HashMap<String, BooleanQuery>();
//        Analyzer analyzer = req.getSchema().getQueryAnalyzer();
        Iterator<Query> iter = input.iterator();
        while (iter.hasNext()) {
            Query query = iter.next();
            if (query instanceof BoostQuery) {
                query = ((BoostQuery) query).getQuery();
            }
            if (query instanceof BooleanQuery) {
                BooleanQuery bq = (BooleanQuery) query;
                for (BooleanClause clause : bq.clauses()) {
                    Query subquery = clause.getQuery();
                    LOG.debug("subquery = " + subquery.toString());
                    LOG.debug("subquery type= " + subquery.getClass().getName());
                    if (subquery instanceof PhraseQuery) {
                        PhraseQuery pq = (PhraseQuery) subquery; // e.g. content:"吉林 长春"
                        for (Term term : pq.getTerms()) {
                            ///加入分词模块，构造term(field, text)
//                    String terms = TextField.analyzeMultiTerm(term.field(), term.text(), analyzer).utf8ToString();

                            BooleanQuery fieldsQuery = m.get(term.text());
                            if (fieldsQuery == null) {
                                fieldsQuery = new BooleanQuery(true);
                                m.put(term.text(), fieldsQuery);//这里存储的fieldsQuery作用是为了让设置里的minboost生效
                            }
                            fieldsQuery.setBoost(pq.getBoost());
                            fieldsQuery.add(new TermQuery(term), BooleanClause.Occur.SHOULD);
                        }
                    } else if (subquery instanceof TermQuery) {
                        TermQuery termQuery = (TermQuery) subquery;
                        BooleanQuery fieldsQuery = m.get(termQuery.getTerm().text());
                        if (fieldsQuery == null) {
                            fieldsQuery = new BooleanQuery(true);
                            m.put(termQuery.getTerm().text(), fieldsQuery);
                        }
                        fieldsQuery.setBoost(termQuery.getBoost());
                        fieldsQuery.add(termQuery, BooleanClause.Occur.SHOULD);
                    }
                }

            }
        }

        Iterator<Map.Entry<String, BooleanQuery>> it = m.entrySet().iterator();
        BooleanQuery mustBooleanQuery = new BooleanQuery(true);
        while (it.hasNext()) {
            Map.Entry<String, BooleanQuery> entry = it.next();
            BooleanQuery shouldBooleanQuery = new BooleanQuery(true);
            createTermQuery(shouldBooleanQuery, entry.getKey());//构造形如（title:武汉 context:武汉）的子查询
            mustBooleanQuery.add(shouldBooleanQuery, BooleanClause.Occur.MUST);
        }
        return mustBooleanQuery;
    }

}