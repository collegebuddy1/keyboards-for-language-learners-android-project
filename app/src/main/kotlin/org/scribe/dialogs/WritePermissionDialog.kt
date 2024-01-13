package org.scribe.dialogs

import android.app.Activity
import android.text.Html
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.dialog_write_permission.view.*
import kotlinx.android.synthetic.main.dialog_write_permission_otg.view.*
import org.scribe.R
import org.scribe.activities.BaseSimpleActivity
import org.scribe.extensions.humanizePath
import org.scribe.extensions.setupDialogStuff

class WritePermissionDialog(activity: Activity, val mode: Mode, val callback: () -> Unit) {
    sealed class Mode {
        object Otg : Mode()
        object SdCard : Mode()
        data class OpenDocumentTreeSDK30(val path: String) : Mode()
        object CreateDocumentSDK30 : Mode()
    }

    var dialog: AlertDialog

    init {
        val layout = if (mode == Mode.SdCard) R.layout.dialog_write_permission else R.layout.dialog_write_permission_otg
        val view = activity.layoutInflater.inflate(layout, null)

        val glide = Glide.with(activity)
        val crossFade = DrawableTransitionOptions.withCrossFade()
        when (mode) {
            Mode.Otg -> {
                view.write_permissions_dialog_otg_text.setText(R.string.confirm_usb_storage_access_text)
                glide.load(R.drawable.img_write_storage_otg).transition(crossFade).into(view.write_permissions_dialog_otg_image)
            }
            Mode.SdCard -> {
                glide.load(R.drawable.img_write_storage).transition(crossFade).into(view.write_permissions_dialog_image)
                glide.load(R.drawable.img_write_storage_sd).transition(crossFade).into(view.write_permissions_dialog_image_sd)
            }
            is Mode.OpenDocumentTreeSDK30 -> {
                val humanizedPath = activity.humanizePath(mode.path)
                view.write_permissions_dialog_otg_text.text =
                    Html.fromHtml(activity.getString(R.string.confirm_storage_access_android_text_specific, humanizedPath))
                glide.load(R.drawable.img_write_storage_sdk_30).transition(crossFade).into(view.write_permissions_dialog_otg_image)

                view.write_permissions_dialog_otg_image.setOnClickListener {
                    dialogConfirmed()
                }
            }
            Mode.CreateDocumentSDK30 -> {
                view.write_permissions_dialog_otg_text.text = Html.fromHtml(activity.getString(R.string.confirm_create_doc_for_new_folder_text))
                glide.load(R.drawable.img_write_storage_create_doc_sdk_30).transition(crossFade).into(view.write_permissions_dialog_otg_image)

                view.write_permissions_dialog_otg_image.setOnClickListener {
                    dialogConfirmed()
                }
            }
        }

        dialog = AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setOnCancelListener {
                BaseSimpleActivity.funAfterSAFPermission?.invoke(false)
                BaseSimpleActivity.funAfterSAFPermission = null
            }
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.confirm_storage_access_title)
            }
    }

    private fun dialogConfirmed() {
        dialog.dismiss()
        callback()
    }
}
