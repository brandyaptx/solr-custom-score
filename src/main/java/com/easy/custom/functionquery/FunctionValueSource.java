package com.easy.custom.functionquery;

import com.easy.custom.tools.ScoreTools;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.FloatDocValues;
import org.apache.lucene.queries.function.valuesource.FieldCacheSource;
import org.apache.lucene.index.NumericDocValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FunctionValueSource extends ValueSource {
    final static Logger log= LoggerFactory.getLogger(FunctionValueSource.class);

    private  List<ValueSource>  valueSources;


    private static long now;
    public FunctionValueSource(List<ValueSource> source) {
        //初始化当前时间
//        now = System.currentTimeMillis();
        this.valueSources=source;
    }


    @Override
    public FunctionValues getValues(Map map, final LeafReaderContext leafReaderContext) throws IOException {

        final FunctionValues hour =this.valueSources.get(0).getValues(map,leafReaderContext);
        final FunctionValues url=this.valueSources.get(1).getValues(map,leafReaderContext);
        final FunctionValues senti=this.valueSources.get(2).getValues(map,leafReaderContext);
//        final NumericDocValues numericDocValues = DocValues.getNumeric(leafReaderContext.reader(), field);
        return new FloatDocValues(this) {
            @Override
            public float floatVal(int i) {
                String time = hour.strVal(i);
                String site_url=url.strVal(i);
                String senti_score=senti.strVal(i);
                float sentiscore = ScoreTools.getSentimentScore(senti_score);
                float timesocre = ScoreTools.getTimeScore(time);
                float sitesocre = ScoreTools.getSiteScore(site_url);

                return timesocre*sitesocre*sentiscore;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String description() {
        return null;
    }
}

