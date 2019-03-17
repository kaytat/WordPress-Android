package org.wordpress.android.viewmodel.posts

import android.support.annotation.ColorRes
import org.apache.commons.text.StringEscapeUtils
import org.wordpress.android.R
import org.wordpress.android.R.string
import org.wordpress.android.analytics.AnalyticsTracker
import org.wordpress.android.analytics.AnalyticsTracker.Stat.POST_LIST_BUTTON_PRESSED
import org.wordpress.android.fluxc.model.PostModel
import org.wordpress.android.fluxc.model.post.PostStatus
import org.wordpress.android.fluxc.model.post.PostStatus.PENDING
import org.wordpress.android.fluxc.model.post.PostStatus.PRIVATE
import org.wordpress.android.fluxc.model.post.PostStatus.SCHEDULED
import org.wordpress.android.fluxc.store.UploadStore.UploadError
import org.wordpress.android.ui.posts.PostAdapterItemUploadStatus
import org.wordpress.android.ui.posts.PostUtils
import org.wordpress.android.ui.prefs.AppPrefsWrapper
import org.wordpress.android.ui.uploads.UploadUtils
import org.wordpress.android.ui.utils.UiString
import org.wordpress.android.ui.utils.UiString.UiStringRes
import org.wordpress.android.ui.utils.UiString.UiStringText
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T.POSTS
import org.wordpress.android.widgets.PostListButtonType
import org.wordpress.android.widgets.PostListButtonType.BUTTON_EDIT
import org.wordpress.android.widgets.PostListButtonType.BUTTON_PREVIEW
import org.wordpress.android.widgets.PostListButtonType.BUTTON_PUBLISH
import org.wordpress.android.widgets.PostListButtonType.BUTTON_RETRY
import org.wordpress.android.widgets.PostListButtonType.BUTTON_SUBMIT
import org.wordpress.android.widgets.PostListButtonType.BUTTON_SYNC
import org.wordpress.android.widgets.PostListButtonType.BUTTON_VIEW
import javax.inject.Inject

private const val MAX_NUMBER_OF_VISIBLE_ACTIONS = 3

/**
 * Helper class which encapsulates logic for creating UiStates for items in the PostsList.
 */
class PostListItemUiStateHelper @Inject constructor(private val appPrefsWrapper: AppPrefsWrapper) {
    fun createPostListItemUiState(
        post: PostModel,
        uploadStatus: PostAdapterItemUploadStatus,
        unhandledConflicts: Boolean,
        capabilitiesToPublish: Boolean,
        statsSupported: Boolean,
        featuredImageUrl: String?,
        formattedDate: String,
        onAction: (PostModel, PostListButtonType, AnalyticsTracker.Stat) -> Unit
    ): PostListItemUiState {
        val postStatus: PostStatus = PostStatus.fromPost(post)

        return PostListItemUiState(
                post.remotePostId,
                post.id,
                getTitle(post),
                getExcerpt(post),
                featuredImageUrl,
                UiStringText(formattedDate),  // TODO Get name of the author
                getStatusLabels(postStatus, post.isLocalDraft, post.isLocallyChanged, uploadStatus, unhandledConflicts),
                getStatusLabelsColor(
                        postStatus,
                        post.isLocalDraft,
                        post.isLocallyChanged,
                        uploadStatus,
                        unhandledConflicts
                ),
                createActions(
                        postStatus,
                        post.isLocalDraft,
                        post.isLocallyChanged,
                        uploadStatus,
                        capabilitiesToPublish,
                        statsSupported
                ) { btnType -> onAction.invoke(post, btnType, POST_LIST_BUTTON_PRESSED) }
                ,
                showProgress = shouldShowProgress(uploadStatus),
                showOverlay = shouldShowOverlay(uploadStatus),
                onSelected = {
                    onAction.invoke(post, BUTTON_EDIT, AnalyticsTracker.Stat.POST_LIST_ITEM_SELECTED)
                }
        )
    }

    private fun getTitle(post: PostModel): UiString {
        return if (post.title.isNotBlank()) {
            UiStringText(StringEscapeUtils.unescapeHtml4(post.title))
        } else UiStringRes(string.untitled_in_parentheses)
    }

    private fun getExcerpt(post: PostModel): UiString? =
            PostUtils.getPostListExcerptFromPost(post)
                    .takeIf { !it.isNullOrBlank() }
                    ?.let { StringEscapeUtils.unescapeHtml4(it) }
                    ?.let { PostUtils.collapseShortcodes(it) }
                    ?.let { UiStringText(it) }

    private fun shouldShowProgress(uploadStatus: PostAdapterItemUploadStatus): Boolean {
        return !uploadStatus.isUploadFailed && (uploadStatus.isUploadingOrQueued || uploadStatus.hasInProgressMediaUpload)
    }

    private fun getStatusLabels(
        postStatus: PostStatus,
        isLocalDraft: Boolean,
        isLocallyChanged: Boolean,
        uploadStatus: PostAdapterItemUploadStatus,
        hasUnhandledConflicts: Boolean
    ): UiString? {
        val isError = uploadStatus.uploadError != null && !uploadStatus.hasInProgressMediaUpload

        return when {
            isError && uploadStatus.uploadError != null -> getErrorLabel(uploadStatus.uploadError)
            uploadStatus.isUploading -> UiStringRes(string.post_uploading)
            uploadStatus.hasInProgressMediaUpload -> UiStringRes(string.uploading_media)
            uploadStatus.isQueued || uploadStatus.hasPendingMediaUpload -> UiStringRes(string.post_queued)
            hasUnhandledConflicts -> UiStringRes(string.local_post_is_conflicted)
            isLocalDraft -> UiStringRes(string.local_draft)
            isLocallyChanged -> UiStringRes(string.local_changes)
            postStatus == PRIVATE -> UiStringRes(string.post_status_post_private)
            postStatus == PENDING -> UiStringRes(string.post_status_pending_review)
            else -> null // do not show any label
        }
    }

    private fun getErrorLabel(uploadError: UploadError): UiString? {
        return when {
            uploadError.mediaError != null -> UiStringRes(string.error_media_recover_post)
            uploadError.postError != null -> UploadUtils.getErrorMessageResIdFromPostError(
                    false,
                    uploadError.postError
            )
            else -> {
                AppLog.e(POSTS, "MediaError and postError are both null.")
                null
            }
        }
    }

    @ColorRes private fun getStatusLabelsColor(
        postStatus: PostStatus,
        isLocalDraft: Boolean,
        isLocallyChanged: Boolean,
        uploadStatus: PostAdapterItemUploadStatus,
        hasUnhandledConflicts: Boolean
    ): Int? {
        val isError = uploadStatus.uploadError != null && !uploadStatus.hasInProgressMediaUpload
        val isWarning = uploadStatus.isQueued || uploadStatus.hasPendingMediaUpload
                || uploadStatus.hasInProgressMediaUpload || uploadStatus.isUploading || hasUnhandledConflicts
        val isInfo = isLocalDraft || isLocallyChanged || postStatus == PRIVATE || postStatus == PENDING

        return when {
            isError -> R.color.alert_red
            isWarning -> R.color.wp_grey_darken_20
            isInfo -> R.color.alert_yellow_dark
            else -> null
        }
    }

    private fun shouldShowOverlay(uploadStatus: PostAdapterItemUploadStatus): Boolean {
        // show overlay when post upload is in progress or (media upload is in progress and the user is not using Aztec)
        return uploadStatus.isUploading || (!appPrefsWrapper.isAztecEditorEnabled && uploadStatus.isUploadingOrQueued)
    }

    private fun createActions(
        postStatus: PostStatus,
        isLocalDraft: Boolean,
        isLocallyChanged: Boolean,
        uploadStatus: PostAdapterItemUploadStatus,
        siteHasCapabilitiesToPublish: Boolean,
        statsSupported: Boolean,
        onButtonClicked: (PostListButtonType) -> Unit
    ): List<PostListItemAction> {
        val canRetryUpload = uploadStatus.uploadError != null && !uploadStatus.hasInProgressMediaUpload
        val canPublishPost = !uploadStatus.isUploadingOrQueued
                && (isLocallyChanged || isLocalDraft || postStatus == PostStatus.DRAFT)
        val canShowStats = statsSupported
                && postStatus == PostStatus.PUBLISHED
                && !isLocalDraft
                && !isLocallyChanged
        val canShowViewButton = !canRetryUpload
        val canShowPublishButton = canRetryUpload || canPublishPost
        val buttonTypes = ArrayList<PostListButtonType>()

        buttonTypes.add(PostListButtonType.BUTTON_EDIT)
        if (canShowPublishButton) {
            buttonTypes.add(
                    if (!siteHasCapabilitiesToPublish) {
                        BUTTON_SUBMIT
                    } else if (canRetryUpload) {
                        BUTTON_RETRY
                    } else if (postStatus == SCHEDULED && isLocallyChanged) {
                        BUTTON_SYNC
                    } else {
                        BUTTON_PUBLISH
                    }
            )
        }

        if (canShowViewButton) {
            buttonTypes.add(
                    if (isLocalDraft || isLocallyChanged) {
                        BUTTON_PREVIEW
                    } else {
                        BUTTON_VIEW
                    }
            )
        }

        if (postStatus != PostStatus.TRASHED) {
            buttonTypes.add(PostListButtonType.BUTTON_TRASH)
        } else {
            buttonTypes.add(PostListButtonType.BUTTON_DELETE)
            buttonTypes.add(PostListButtonType.BUTTON_RESTORE)
        }

        if (canShowStats) {
            buttonTypes.add(PostListButtonType.BUTTON_STATS)
        }

        return if (buttonTypes.size > MAX_NUMBER_OF_VISIBLE_ACTIONS) {
            val visibleItems = buttonTypes.take(MAX_NUMBER_OF_VISIBLE_ACTIONS - 1).map {
                PostListItemAction.SingleItem(it, onButtonClicked)
            }
            val itemsUnderMore = buttonTypes.subList(MAX_NUMBER_OF_VISIBLE_ACTIONS - 1, buttonTypes.size).map {
                PostListItemAction.SingleItem(it, onButtonClicked)
            }
            visibleItems.plus(PostListItemAction.MoreItem(itemsUnderMore, onButtonClicked))
        } else {
            buttonTypes.map {
                PostListItemAction.SingleItem(it, onButtonClicked)
            }
        }
    }
}
