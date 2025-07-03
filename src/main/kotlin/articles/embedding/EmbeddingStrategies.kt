package articles.embedding

import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ModelZoo
import ai.djl.repository.zoo.ZooModel
import kotlin.random.Random

interface EmbeddingStrategy {
    fun embed(text: String): List<Float>
}

class DjlEmbeddingStrategy : EmbeddingStrategy {
    private val model: ZooModel<String, FloatArray>

    init {
        val criteria =
            Criteria
                .builder()
                .setTypes(String::class.java, FloatArray::class.java)
                .optModelUrls("https://djl-ai.s3.amazonaws.com/resources/demo/pytorch/trace_bert_embedding.zip")
                .build()
        model = ModelZoo.loadModel(criteria)
    }

    override fun embed(text: String): List<Float> {
        model.newPredictor().use { predictor ->
            val embedding: FloatArray = predictor.predict(text)
            return embedding.toList()
        }
    }
}

class FakeEmbeddingStrategy : EmbeddingStrategy {
    override fun embed(text: String): List<Float> {
        // Generate a fake embedding of 384 dimensions (same as all-MiniLM-L6-v2)
        return List(384) { Random.nextFloat() }
    }
}
