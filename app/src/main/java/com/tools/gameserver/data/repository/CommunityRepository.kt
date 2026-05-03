package com.tools.gameserver.data.repository

import android.util.Log
import com.tools.gameserver.data.model.CommunityFile
import com.tools.gameserver.data.model.CommunityPost
import com.tools.gameserver.data.model.CommunityProtocol
import com.tools.gameserver.data.service.api.ApiClient
import com.tools.gameserver.data.service.api.ApiResult
import org.json.JSONObject

object CommunityRepository {

    private const val TAG = "CommunityRepo"

    suspend fun loadPosts(
        page: Int = 1,
        limit: Int = 50,
        search: String = "",
        tag: String = ""
    ): Result<List<CommunityPost>> {
        val aggregatedResult = tryLoadPostsFromApi(page, limit, search, tag)
        if (aggregatedResult.isSuccess) return aggregatedResult

        Log.w(TAG, "Aggregation API failed, falling back to protocols+files")
        return try {
            val protocolsResult = ApiClient.getProtocols(page, limit)
            if (protocolsResult is ApiResult.Success) {
                val json = JSONObject(protocolsResult.body)
                val items = json.optJSONArray("data") ?: json.optJSONArray("protocols")
                val posts = mutableListOf<CommunityPost>()
                if (items != null) {
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val postId = item.optString("id", "")
                        val name = item.optString("name", "")
                        val author = item.optString("author", "")
                        val description = item.optString("description", "")
                        val method = item.optString("method", "POST")
                        val url = item.optString("url", "")
                        val rawHeaders = item.optString("raw_headers", "")
                        val rawBody = item.optString("raw_body", "")
                        val tagsStr = item.optString("tags", "")
                        val tagsList = if (tagsStr.isNotBlank()) {
                            tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        } else emptyList()
                        val filesArray = item.optJSONArray("files")
                        val files = mutableListOf<CommunityFile>()
                        if (filesArray != null) {
                            for (fi in 0 until filesArray.length()) {
                                val f = filesArray.getJSONObject(fi)
                                files.add(CommunityFile(
                                    id = f.optString("id", ""),
                                    name = f.optString("name", ""),
                                    description = f.optString("description", ""),
                                    filePath = f.optString("file_path", ""),
                                    fileSize = f.optLong("file_size", 0),
                                    fileType = f.optString("file_type", ""),
                                    author = f.optString("author", ""),
                                    downloads = f.optInt("downloads", 0),
                                    tags = f.optString("tags", "")
                                ))
                            }
                        }
                        posts.add(
                            CommunityPost(
                                id = "p_$postId",
                                name = name,
                                author = author,
                                description = description,
                                method = method,
                                url = url,
                                rawHeaders = rawHeaders,
                                rawBody = rawBody,
                                tags = tagsList,
                                downloads = item.optInt("downloads", 0),
                                rating = item.optDouble("rating", 0.0).toFloat(),
                                fileCount = item.optInt("file_count", files.size),
                                files = files,
                                postType = "protocol"
                            )
                        )
                    }
                }
                Log.d(TAG, "Loaded ${posts.size} posts via fallback (protocols only)")
                Result.success(posts)
            } else {
                Result.failure(Exception((protocolsResult as? ApiResult.Error)?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPost(postId: String): Result<CommunityPost> {
        return try {
            val result = ApiClient.getCommunityPost(postId)
            when (result) {
                is ApiResult.Success -> {
                    val json = JSONObject(result.body)
                    val data = json.optJSONObject("data") ?: json
                    val post = parseCommunityPost(data)
                    Log.d(TAG, "Loaded post detail via aggregation API: ${post.name}")
                    Result.success(post)
                }
                is ApiResult.Error -> {
                    // Try fallback: direct protocol download
                    try {
                        val protoResult = ApiClient.downloadProtocol(postId.removePrefix("p_"))
                        if (protoResult is ApiResult.Success) {
                            val protoJson = JSONObject(protoResult.body)
                            val data = protoJson.optJSONObject("data") ?: protoJson
                            val post = parseCommunityPost(data)
                            Log.d(TAG, "Loaded post detail via fallback: ${post.name}")
                            Result.success(post)
                        } else {
                            Result.failure(Exception((protoResult as? ApiResult.Error)?.message ?: "Post not found"))
                        }
                    } catch (e: Exception) {
                        Result.failure(Exception(result.message))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadProtocols(page: Int = 1, limit: Int = 50): Result<List<CommunityProtocol>> {
        return try {
            when (val result = ApiClient.getProtocols(page, limit)) {
                is ApiResult.Success -> {
                    val json = JSONObject(result.body)
                    val items = json.optJSONArray("data") ?: json.optJSONArray("protocols")
                    val protocols = mutableListOf<CommunityProtocol>()
                    if (items != null) {
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            val tagsStr = item.optString("tags", "")
                            val tagsList = if (tagsStr.isNotBlank()) {
                                tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            } else emptyList()
                            protocols.add(
                                CommunityProtocol(
                                    id = item.optString("id", ""),
                                    name = item.optString("name", ""),
                                    author = item.optString("author", ""),
                                    description = item.optString("description", ""),
                                    rawHeaders = item.optString("raw_headers", ""),
                                    rawBody = item.optString("raw_body", ""),
                                    downloads = item.optInt("downloads", 0),
                                    rating = item.optDouble("rating", 0.0).toFloat(),
                                    tags = tagsList,
                                    method = item.optString("method", "POST"),
                                    url = item.optString("url", "")
                                )
                            )
                        }
                    }
                    Result.success(protocols)
                }
                is ApiResult.Error -> Result.failure(Exception(result.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadFiles(page: Int = 1, limit: Int = 50): Result<List<CommunityFile>> {
        return try {
            when (val result = ApiClient.getFiles(page, limit)) {
                is ApiResult.Success -> {
                    val json = JSONObject(result.body)
                    val items = json.optJSONArray("data") ?: json.optJSONArray("files")
                    val files = mutableListOf<CommunityFile>()
                    if (items != null) {
                        for (i in 0 until items.length()) {
                            val f = items.getJSONObject(i)
                            files.add(
                                CommunityFile(
                                    id = f.optString("id", ""),
                                    name = f.optString("name", ""),
                                    description = f.optString("description", ""),
                                    filePath = f.optString("file_path", ""),
                                    fileSize = f.optLong("file_size", 0),
                                    fileType = f.optString("file_type", ""),
                                    author = f.optString("author", ""),
                                    downloads = f.optInt("downloads", 0),
                                    tags = f.optString("tags", "")
                                )
                            )
                        }
                    }
                    Result.success(files)
                }
                is ApiResult.Error -> Result.failure(Exception(result.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun tryLoadPostsFromApi(page: Int, limit: Int, search: String, tag: String): Result<List<CommunityPost>> {
        return try {
            val result = ApiClient.searchCommunity(search, tag, page, limit)
            when (result) {
                is ApiResult.Success -> {
                    val json = JSONObject(result.body)
                    val items = json.optJSONArray("data") ?: json.optJSONArray("posts")
                    val posts = mutableListOf<CommunityPost>()
                    if (items != null) {
                        for (i in 0 until items.length()) {
                            posts.add(parseCommunityPost(items.getJSONObject(i)))
                        }
                    }
                    Log.d(TAG, "Loaded ${posts.size} posts via aggregation API")
                    Result.success(posts)
                }
                is ApiResult.Error -> Result.failure(Exception(result.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCommunityPost(item: JSONObject): CommunityPost {
        val postId = item.optString("id", "")
        val postType = item.optString("post_type", "protocol")
        val tagsStr = item.optString("tags", "")
        val tagsList = if (tagsStr.isNotBlank()) {
            tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else emptyList()

        val filesArray = item.optJSONArray("files")
        val files = mutableListOf<CommunityFile>()
        if (filesArray != null) {
            for (i in 0 until filesArray.length()) {
                val f = filesArray.getJSONObject(i)
                files.add(
                    CommunityFile(
                        id = f.optString("id", ""),
                        name = f.optString("name", ""),
                        description = f.optString("description", ""),
                        filePath = f.optString("file_path", ""),
                        fileSize = f.optLong("file_size", 0),
                        fileType = f.optString("file_type", ""),
                        author = f.optString("author", ""),
                        downloads = f.optInt("downloads", 0),
                        tags = f.optString("tags", "")
                    )
                )
            }
        }

        return CommunityPost(
            id = postId,
            name = item.optString("name", ""),
            author = item.optString("author", ""),
            description = item.optString("description", ""),
            method = item.optString("method", ""),
            url = item.optString("url", ""),
            rawHeaders = item.optString("raw_headers", ""),
            rawBody = item.optString("raw_body", ""),
            tags = tagsList,
            downloads = item.optInt("downloads", 0),
            rating = item.optDouble("rating", 0.0).toFloat(),
            fileCount = item.optInt("file_count", files.size),
            files = files,
            postType = postType
        )
    }
}