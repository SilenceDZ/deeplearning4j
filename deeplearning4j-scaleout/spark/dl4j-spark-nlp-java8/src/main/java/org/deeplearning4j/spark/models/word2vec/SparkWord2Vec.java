package org.deeplearning4j.spark.models.word2vec;

import lombok.NonNull;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.deeplearning4j.models.embeddings.loader.VectorsConfiguration;
import org.deeplearning4j.models.sequencevectors.sequence.Sequence;
import org.deeplearning4j.models.sequencevectors.sequence.ShallowSequenceElement;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.spark.models.sequencevectors.SparkSequenceVectors;
import org.deeplearning4j.spark.models.sequencevectors.functions.TokenizerFunction;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

/**
 * @author raver119@gmail.com
 */
public class SparkWord2Vec extends SparkSequenceVectors<VocabWord> {

    protected SparkWord2Vec() {
        // FIXME: this is development-time constructor, please remove before release
        configuration = new VectorsConfiguration();
        configuration.setTokenizerFactory(DefaultTokenizerFactory.class.getCanonicalName());
    }

    protected SparkWord2Vec(@NonNull VectorsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected VocabCache<ShallowSequenceElement> getShallowVocabCache() {
        return super.getShallowVocabCache();
    }


    /**
     * PLEASE NOTE: This method isn't supported for Spark implementation. Consider using fitLists() or fitSequences() instead.
     */
    @Override
    @Deprecated
    public void fit() {
        throw new UnsupportedOperationException("To use fit() method, please consider using standalone implementation");
    }

    public void fitSentences(JavaRDD<String> sentences) {
        /**
         * Basically all we want here is tokenization, to get JavaRDD<Sequence<VocabWord>> out of Strings, and then we just go  for SeqVec
         */
        final JavaSparkContext context = new JavaSparkContext(sentences.context());

        broadcastEnvironment(context);

        JavaRDD<Sequence<VocabWord>> seqRdd = sentences.map(new TokenizerFunction(configurationBroadcast));

        // now since we have new rdd - just pass it to SeqVec
        super.fitSequences(seqRdd);
    }


    public static class Builder extends SparkSequenceVectors.Builder<VocabWord> {

        public Builder() {
            super();
        }

        public Builder(@NonNull VectorsConfiguration configuration) {
            super(configuration);
        }

        public Builder setTokenizerFactory(@NonNull TokenizerFactory tokenizerFactory) {
            configuration.setTokenizerFactory(tokenizerFactory.getClass().getCanonicalName());
            if (tokenizerFactory.getTokenPreProcessor() != null)
                configuration.setTokenPreProcessor(tokenizerFactory.getTokenPreProcessor().getClass().getCanonicalName());

            return this;
        }


        public SparkWord2Vec build() {
            SparkWord2Vec sw2v = new SparkWord2Vec(configuration);

            return sw2v;
        }
    }
}