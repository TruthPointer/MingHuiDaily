package org.tpmobile.minghuidaily.data

sealed class FetchState<T : Any> {
    data class Success<T : Any>(val data: T) : FetchState<T>()
    data class Failure(val error: Exception) : FetchState<Nothing>()
}

/*
sealed class ArticleFetchState:  FetchState<Article>() {
    data class Success(val data: Article) : ArticleFetchState()
    data class Failure(val error: Exception) : ArticleFetchState()
}

sealed class MainCategoryFetchState:  FetchState<List<Category>>() {
    data class Success(val data: MainCategoryFetchState) : ArticleFetchState()
    data class Failure(val error: Exception) : ArticleFetchState()
}

sealed class DetailArticleFetchState:  FetchState<DetailArticle>() {
    data class Success(val data: DetailArticle) : DetailArticleFetchState()
    data class Failure(val error: Exception) : DetailArticleFetchState()
}*/
