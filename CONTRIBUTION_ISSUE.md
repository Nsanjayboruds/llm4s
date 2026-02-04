# Add Semantic Search Example for RAG Pipeline

## 📋 Issue Type
- [x] Enhancement
- [ ] Bug Fix
- [ ] Documentation
- [ ] Feature Request

## 🎯 Description

Add a comprehensive example demonstrating semantic search capabilities in the RAG pipeline, showcasing hybrid search (vector + BM25) with reranking.

## 🔍 Problem Statement

While LLM4S has excellent RAG capabilities with hybrid search and reranking support, there isn't a standalone example that demonstrates:
- Setting up a complete RAG pipeline with hybrid search
- Comparing vector-only vs. hybrid search results
- Using the Cohere reranker for result refinement
- Performance metrics and evaluation

This makes it harder for new users to understand the benefits of hybrid search and how to implement it effectively.

## 💡 Proposed Solution

Create a new example file: `modules/samples/src/main/scala/org/llm4s/samples/rag/HybridSearchComparisonExample.scala`

**Features to demonstrate:**
1. **Vector Store Setup** - Initialize with sample documents
2. **Vector-Only Search** - Traditional semantic search
3. **Hybrid Search** - Combine vector similarity + BM25 keyword matching
4. **Reranking** - Apply Cohere cross-encoder reranking
5. **Comparison** - Show side-by-side results with relevance scores
6. **Metrics** - Display search timing and quality metrics

**Sample Documents:** Use a small corpus about LLM concepts (e.g., "retrieval augmented generation", "embeddings", "vector databases")

## 📝 Implementation Checklist

- [ ] Create `HybridSearchComparisonExample.scala` in `modules/samples/src/main/scala/org/llm4s/samples/rag/`
- [ ] Implement vector-only search method
- [ ] Implement hybrid search method
- [ ] Implement reranking integration
- [ ] Add comparison logic with side-by-side output
- [ ] Include performance metrics (timing, relevance scores)
- [ ] Add comprehensive ScalaDoc comments
- [ ] Create test file in `modules/samples/src/test/scala/org/llm4s/samples/rag/`
- [ ] Update documentation in `docs/examples/index.md` to reference new example
- [ ] Add README section explaining the example

## 🧪 Acceptance Criteria

1. ✅ Example runs successfully with `sbt "samples/runMain org.llm4s.samples.rag.HybridSearchComparisonExample"`
2. ✅ Demonstrates clear differences between search strategies
3. ✅ Includes proper error handling with `Result[_]` types
4. ✅ Follows LLM4S coding standards (scalafmt, functional style, immutability)
5. ✅ Includes unit tests with >80% coverage
6. ✅ Documentation is clear and includes expected output
7. ✅ Works with both in-memory and persistent vector stores

## 📚 Technical Details

**Dependencies:**
```scala
import org.llm4s.rag.RAG
import org.llm4s.vectorstore.VectorStoreFactory
import org.llm4s.reranker.RerankerFactory
import org.llm4s.llmconnect.config.EmbeddingProviderConfig
```

**Required Environment Variables:**
```bash
LLM_MODEL=openai/gpt-4o
OPENAI_API_KEY=sk-...
# Optional for reranking:
COHERE_API_KEY=...
```

**Code Structure:**
```scala
object HybridSearchComparisonExample {
  case class SearchResult(
    strategy: String,
    results: Seq[ScoredVectorRecord],
    durationMs: Long
  )
  
  def runVectorSearch(rag: RAG, query: String): Result[SearchResult]
  def runHybridSearch(rag: RAG, query: String): Result[SearchResult]
  def runWithReranking(rag: RAG, query: String): Result[SearchResult]
  def compareResults(results: Seq[SearchResult]): Unit
}
```

## 🎓 Learning Outcomes for Contributors

- Understanding RAG pipeline architecture in LLM4S
- Working with vector stores and embeddings
- Implementing hybrid search strategies
- Performance benchmarking and evaluation
- Functional programming patterns in Scala
- Test-driven development practices

## 🏷️ Labels

- `good-first-issue`
- `enhancement`
- `documentation`
- `rag`
- `examples`
- `help-wanted`

## 👥 Mentorship

This issue is suitable for:
- **Beginner/Intermediate** Scala developers
- Contributors interested in RAG and vector databases
- Google Summer of Code 2026 applicants

**Estimated Effort:** 8-12 hours

**Mentors Available:**
- @kannupriyakalra
- @rorygraves

## 🔗 Related Resources

- [RAG Documentation](https://llm4s.github.io/guide/rag)
- [Vector Store Guide](https://llm4s.github.io/guide/vector-store)
- [RAG Evaluation](https://llm4s.github.io/guide/rag-evaluation)
- [AGENTS.md](./AGENTS.md) - Repository guidelines

## 📞 Getting Help

- **Discord:** [LLM4S Community](https://discord.gg/4uvTPn6qww)
- **Email:** kannupriyakalra@gmail.com
- **Discussion:** Comment on this issue with questions

---

**Note:** Before starting, please:
1. Comment on this issue to express interest
2. Fork the repository
3. Set up pre-commit hooks: `./hooks/install.sh`
4. Read [AGENTS.md](./AGENTS.md) for contribution guidelines
5. Run `sbt test` to ensure your environment is set up correctly
