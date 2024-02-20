package zeroxfourf.wristkey

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import wristkey.R

class CustomFullscreenDialogFragment(
    private val title: String,
    private val message: String,
    private val positiveButtonText: String?,
    private val positiveButtonIcon: Drawable?,
    private val negativeButtonText: String?,
    private val negativeButtonIcon: Drawable?
) : DialogFragment() {
    private var onPositiveClickListener: (() -> Unit)? = null

    fun setOnPositiveClickListener (listener: () -> Unit) {
        onPositiveClickListener = listener
    }

    private var onNegativeClickListener: (() -> Unit)? = null

    fun setOnNegativeClickListener (listener: () -> Unit) {
        onNegativeClickListener = listener
    }

    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialogTitle: TextView = view.findViewById(R.id.title)
        val dialogMessage: TextView = view.findViewById(R.id.message)
        val positiveButton: Button = view.findViewById(R.id.positive_button)
        val negativeButton: Button = view.findViewById(R.id.negative_button)

        dialogTitle.text = title
        dialogMessage.text = message
        positiveButton.text = positiveButtonText
        negativeButton.text = negativeButtonText
        positiveButton.setCompoundDrawablesRelativeWithIntrinsicBounds (positiveButtonIcon, null, null, null)
        negativeButton.setCompoundDrawablesRelativeWithIntrinsicBounds (negativeButtonIcon, null, null, null)

        if (positiveButtonText.isNullOrBlank()) positiveButton.visibility = View.GONE
        if (negativeButtonText.isNullOrBlank()) negativeButton.visibility = View.GONE

        positiveButton.setOnClickListener {
            onPositiveClickListener?.invoke()
            dismiss()
        }

        negativeButton.setOnClickListener {
            onNegativeClickListener?.invoke()
            dismiss()
        }

    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}
