package com.intuisoft.plaid.features.settings.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentAddressBookBinding
import com.intuisoft.plaid.features.settings.adapters.AddressBookAdapter
import com.intuisoft.plaid.features.settings.viewmodel.AddressBookViewModel
import com.intuisoft.plaid.common.model.SavedAddressModel
import com.intuisoft.plaid.common.util.extensions.toArrayList
import org.koin.androidx.viewmodel.ext.android.viewModel


class AddressBookFragment : ConfigurableFragment<FragmentAddressBookBinding>(pinProtection = true) {
    private val viewModel: AddressBookViewModel by viewModel()

    private val adapter = AddressBookAdapter(
        onAddressSelected = ::onAddressSelected,
        onCopyAddress = ::onCopyAddress
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddressBookBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {

        viewModel.showAddresses()
        binding.savedAddresses.adapter = adapter
        viewModel.addresses.observe(viewLifecycleOwner, Observer {
            adapter.addAddresses(it.toArrayList())

            binding.savedAddresses.isVisible = it.isNotEmpty()
            binding.noAddressContainer.isVisible = it.isEmpty()
        })
    }

    private fun onAddressSelected(addressModel: SavedAddressModel) {
        showSaveAddressDialog(
            context = requireContext(),
            titleText = getString(R.string.save_address_update_title),
            saveButtonText = getString(R.string.update),
            cancelButtonText = getString(R.string.delete),
            initialAddressText = addressModel.address,
            initialNameText = addressModel.addressName,
            isAddressValid = {
                viewModel.isAddressValid(it)
            },
            saveAddress = { name, address ->
                viewModel.updateAddress(addressModel.addressName, name, address)
                viewModel.showAddresses()
                true
            },
            onCancel = {
                SettingsFragment.warningDialog(
                    context = requireContext(),
                    title = getString(R.string.save_address_delete_title),
                    subtitle = getString(R.string.save_address_delete_subtitle, addressModel.addressName),
                    positive = getString(R.string.delete),
                    negative = getString(R.string.cancel),
                    positiveTint = 0,
                    onPositive = {
                        viewModel.removeAddress(addressModel.addressName)
                        viewModel.showAddresses()
                    },
                    onNegative = {
                        // do nothin
                    }
                )
            }
        )
    }

    private fun onCopyAddress(addressModel: SavedAddressModel) {
        requireContext().copyToClipboard(addressModel.address, "Address")
        styledSnackBar(requireView(), getString(R.string.save_address_copy_message, addressModel.addressName))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN
    }

    override fun actionBarTitle(): Int {
        return R.string.address_book_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun actionBarActionRight(): Int {
        return R.drawable.ic_add
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun onActionRight() {
        showSaveAddressDialog(
            context = requireContext(),
            titleText = getString(R.string.save_address_title),
            saveButtonText = getString(R.string.save),
            cancelButtonText = getString(R.string.cancel),
            isAddressValid = {
                viewModel.isAddressValid(it)
            },
            saveAddress = { name, address ->
                if(viewModel.savedAddressExists(name)) {
                    false
                } else {
                    viewModel.saveAddress(name, address)
                    viewModel.showAddresses()
                    true
                }
            }
        )
    }

    override fun navigationId(): Int {
        return R.id.appearanceFragment
    }

    companion object {

        fun showSaveAddressDialog(
            context: Context,
            titleText: String,
            saveButtonText: String,
            cancelButtonText: String,
            initialNameText: String = "",
            initialAddressText: String = "",
            isAddressValid: (String) -> Boolean,
            saveAddress: (String, String) -> Boolean,
            onCancel: (() -> Unit)? = null
        ) {
            val bottomSheetDialog = BottomSheetDialog(context)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_save_address)
            val title = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)!!
            val save = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.save)!!
            val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
            val name = bottomSheetDialog.findViewById<EditText>(R.id.name)!!
            val textLeft = bottomSheetDialog.findViewById<TextView>(R.id.textLeft)!!
            val address = bottomSheetDialog.findViewById<EditText>(R.id.address)!!
            val errorMessage = bottomSheetDialog.findViewById<TextView>(R.id.validation_error)!!

            title.text = titleText
            save.setButtonText(saveButtonText)
            cancel.setButtonText(cancelButtonText)
            name.setText(initialNameText)
            address.setText(initialAddressText)
            save.enableButton(false)
            name.doOnTextChanged { text, start, before, count ->
                textLeft.text = "${text?.length ?: 0}/25"
                save.enableButton(text?.isNotEmpty() ?: false && address.text.isNotEmpty())
            }

            address.doOnTextChanged { text, start, before, count ->
                save.enableButton(text?.isNotEmpty() ?: false && name.text.isNotEmpty())
            }

            save.onClick {
                if(isAddressValid(address.text.toString())) {
                    if(saveAddress(name.text.toString(), address.text.toString())) {
                        bottomSheetDialog.cancel()
                    } else {
                        errorMessage.isVisible = true
                        errorMessage.setText(context.getString(R.string.save_address_validation_error_already_exists))
                    }
                } else {
                    errorMessage.isVisible = true
                    errorMessage.setText(context.getString(R.string.save_address_validation_error_invalid_address))
                }
            }

            cancel.onClick {
                bottomSheetDialog.cancel()
                onCancel?.invoke()
            }


            bottomSheetDialog.behavior.state = STATE_EXPANDED
            bottomSheetDialog.show()
        }
    }
}