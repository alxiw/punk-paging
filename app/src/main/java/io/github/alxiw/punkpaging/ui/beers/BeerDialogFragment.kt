package io.github.alxiw.punkpaging.ui.beers

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.alxiw.punkpaging.R

class BeerDialogFragment : AppCompatDialogFragment() {

    private var beerId: Int? = null
    private var beerTitle: String? = null
    private var beerDescription: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            beerId = it.getInt(BEER_ID_KEY)
            beerTitle = it.getString(BEER_TITLE_KEY)
            beerDescription = it.getString(BEER_DESCRIPTION_KEY)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialog.dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(beerTitle)
            .setMessage(beerDescription)
            .setOnDismissListener {

            }
            .setPositiveButton(getString(R.string.dialog_ok)) { dialog, which ->

            }
            .show()
    }

    companion object {
        private const val BEER_ID_KEY = "beer_id"
        private const val BEER_TITLE_KEY = "beer_title"
        private const val BEER_DESCRIPTION_KEY = "beer_description"

        @JvmStatic
        fun newInstance(id: Int, title: String, description: String): BeerDialogFragment {
            return BeerDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(BEER_ID_KEY, id)
                    putString(BEER_TITLE_KEY, title)
                    putString(BEER_DESCRIPTION_KEY, description)
                }
            }
        }
    }
}
