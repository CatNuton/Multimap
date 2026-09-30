package com.operationsoft.multimap.lib.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.doAfterTextChanged
import com.operationsoft.multimap.R
import com.operationsoft.multimap.lib.essentials.ApplyEventArgs
import com.operationsoft.multimap.lib.essentials.Tools.Companion.toEditable
import com.operationsoft.multimap.lib.essentials.Tools.Companion.addNumber
import com.operationsoft.multimap.lib.essentials.Tools.Companion.scaleSavingRatio
import org.osmdroid.views.overlay.Marker

class WaypointViewActivity @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val btnApply: Button?
    private val ivIcon: ImageView?
    private val etTitle: EditText?
    private val tvError: TextView?
    private val etDescription: EditText?

    private var activity: AppCompatActivity? = null
    private var iconPicker: ActivityResultLauncher<PickVisualMediaRequest>? = null

    private val defaultTitle: String = "Waypoint"
    private val defaultDescription: String = "Waypoint Description"

    var markers: MutableList<Marker?> = mutableListOf()

//    val title: String
//        get() = etTitle!!.editableText.toString()
//    val description: String
//        get() = etDescription!!.editableText.toString()
    var icon: Drawable? = getDrawable(this.context,
        org.osmdroid.library.R.drawable.marker_default)

    var onApplied: ((ApplyEventArgs) -> Unit)? = null

    companion object{
        private const val MARKER_SIZE_DP = 200
    }

    init {
        orientation = VERTICAL

        LayoutInflater.from(context).inflate(
            R.layout.activity_waypoint_view,
            this, true)

        ivIcon = findViewById<ImageView>(R.id.ivIcon)
        btnApply = findViewById<Button>(R.id.btnApply)
        etTitle = findViewById<EditText>(R.id.etTitle)
        tvError = findViewById<TextView>(R.id.tvError)
        etDescription = findViewById<EditText>(R.id.etDescription)

        setIcon(icon!!.toBitmap())
        etTitle!!.text = defaultTitle.toEditable()
        etDescription!!.text = defaultDescription.toEditable()

        etTitle.doAfterTextChanged(::etTitleOnTextChanged)
        ivIcon.setOnClickListener(::ivIconOnClick)
        btnApply.setOnClickListener(::btnApplyOnClick)
    }

    public fun attachActivity(activity: AppCompatActivity) {
        this.activity = activity
        iconPicker = activity.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            uri?.let {
                activity.contentResolver.openInputStream(it)?.use { input ->
                    BitmapFactory.decodeStream(input)?.let { bmp -> setIcon(bmp) }
                }
            }
        }
    }

    public fun setParameters(title: String, description: String, icon: Bitmap){
        etTitle!!.text = title.toEditable()
        etDescription!!.text = description.toEditable()
        setIcon(icon)
    }

    public fun ivIconOnClick(view: View?){
        iconPicker?.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    public fun btnApplyOnClick(view: View?){
        val applyEventArgs = ApplyEventArgs(etTitle!!.text.toString(),
            etDescription!!.text.toString(), icon!!)
        onApplied?.invoke(applyEventArgs)
    }

    private fun etTitleOnTextChanged(e: Editable?){
        if (isTitleInList(etTitle!!.text.toString())){
            tvError!!.setText(com.operationsoft.multimap.R.string.errorTitleExist)
            tvError.visibility = VISIBLE
            btnApply!!.isEnabled = false
        }
        else{
            tvError!!.visibility = GONE
            btnApply!!.isEnabled = true
        }
    }

    private fun isTitleInList(title: String) : Boolean {
        if (!markers.isEmpty()) {
            for (i in markers){
                if (i!!.title == title){
                    return true
                }
            }
        }
        return false
    }

    private fun setIcon(bitmap: Bitmap){
        val scaledBitmap = bitmap.scaleSavingRatio(MARKER_SIZE_DP)
        icon = scaledBitmap.toDrawable(this.resources)
        ivIcon!!.setImageBitmap(scaledBitmap)
    }

    protected override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        val inputTitle = etTitle!!.editableText.toString()
        if (isTitleInList(inputTitle)){
            etTitle.setText(inputTitle.addNumber())
        }
    }
}