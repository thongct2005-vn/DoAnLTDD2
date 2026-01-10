package com.example.app.ui.comment.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.domain.model.CommentItem

@Composable
fun CommentList(
    commentItems: List<CommentItem>,
    expandedParentIds: Set<String>,
    visibleReplyCountMap: Map<String, Int>,
    onToggleExpand: (String) -> Unit,
    onLoadMoreReplies: (String) -> Unit,
    onReplyClick: (CommentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    fun findRootId(item: CommentItem): String {
        if (item is CommentItem.Parent) return item.comment.id
        var currentId = item.comment.parentId
        while (currentId != null) {
            val found = commentItems.find { it.comment.id == currentId }
            if (found is CommentItem.Parent) return found.comment.id
            currentId = found?.comment?.parentId
        }
        return item.comment.id
    }

    LazyColumn(modifier = modifier) {
        val parents = commentItems.filterIsInstance<CommentItem.Parent>()

        parents.forEach { parent ->
            val parentId = parent.comment.id
            val replyCount = commentItems.count { it is CommentItem.Reply && findRootId(it) == parentId }

            item(key = parentId) {
                CommentRow(
                    item = parent,
                    replyCount = replyCount,
                    isExpanded = expandedParentIds.contains(parentId),
                    onToggleExpand = { onToggleExpand(parentId) },
                    onReplyClick = onReplyClick
                )
            }

            if (expandedParentIds.contains(parentId)) {
                val replies = commentItems
                    .filterIsInstance<CommentItem.Reply>()
                    .filter { findRootId(it) == parentId }

                val visibleCount = visibleReplyCountMap[parentId] ?: 5
                val visibleReplies = replies.take(visibleCount)

                items(
                    items = visibleReplies,
                    key = { it.comment.id }
                ) { reply ->
                    CommentRow(
                        item = reply,
                        replyCount = 0,
                        isExpanded = false,
                        onToggleExpand = {},
                        onReplyClick = onReplyClick
                    )
                }

                if (replies.isNotEmpty()) {
                    item(key = "controls_$parentId") {
                        ReplyControls(
                            visibleCount = visibleCount,
                            totalCount = replies.size,
                            onLoadMore = { onLoadMoreReplies(parentId) },
                            onCollapse = { onToggleExpand(parentId) }
                        )
                    }
                }
            }
        }
    }
}