package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentRecoveryPhraseImportBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.WalletConfigurationData
import kotlinx.android.synthetic.main.fragment_public_key_import.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class RecoveryPhraseImportFragment : ConfigurableFragment<FragmentRecoveryPhraseImportBinding>(
    pinProtection = true,
    secureScreen = true
) {
    protected val viewModel: CreateWalletViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRecoveryPhraseImportBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_WALLET_DATA
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        viewModel.setConfiguration(configuration!!.configData as WalletConfigurationData)

        binding.importWallet.enableButton(false)
        binding.seedPhrase.resetView()

        binding.removeLastWord.setOnClickListener {
            viewModel.removeLastWord()
        }

        binding.word.doOnTextChanged { text, start, before, count ->
            viewModel.updateWordSuggestions(binding.word.text.toString())
        }

        binding.word.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // if the event is a key down event on the enter button
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    viewModel.addWordToList(binding.word.text.toString(), true)
                    return true
                }
                return false
            }
        })

        binding.importWallet.onClick {
            viewModel.importSeedPhrase()
        }

        viewModel.wordSuggestion1.observe(viewLifecycleOwner, Observer {
            binding.wordSuggestion1.visibility = if(it.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            binding.wordSuggestion1.text = it
        })

        viewModel.wordSuggestion2.observe(viewLifecycleOwner, Observer {
            binding.wordSuggestion2.visibility = if(it.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            binding.wordSuggestion2.text = it
        })

        viewModel.wordSuggestion3.observe(viewLifecycleOwner, Observer {
            binding.wordSuggestion3.visibility = if(it.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            binding.wordSuggestion3.text = it
        })

        viewModel.wordSuggestion4.observe(viewLifecycleOwner, Observer {
            binding.wordSuggestion4.visibility = if(it.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            binding.wordSuggestion4.text = it
        })

        viewModel.wordSuggestionMany.observe(viewLifecycleOwner, Observer {
            binding.moreWordsIcon.visibility = if(it) View.VISIBLE else View.INVISIBLE
        })

        viewModel.importAllowed.observe(viewLifecycleOwner, Observer {
            binding.importWallet.enableButton(it)
        })

        viewModel.onRemoveLastWord.observe(viewLifecycleOwner, Observer {
            binding.seedPhrase.removeLastWord()
        })

        binding.wordSuggestion1.setOnClickListener {
            viewModel.addWordToList(binding.wordSuggestion1.text.toString())
        }

        binding.wordSuggestion2.setOnClickListener {
            viewModel.addWordToList(binding.wordSuggestion2.text.toString())
        }

        binding.wordSuggestion3.setOnClickListener {
            viewModel.addWordToList(binding.wordSuggestion3.text.toString())
        }

        binding.wordSuggestion4.setOnClickListener {
            viewModel.addWordToList(binding.wordSuggestion4.text.toString())
        }

        viewModel.clearWordEntry.observe(viewLifecycleOwner, Observer {
            binding.word.setText("")
        })

        binding.word.setOnEditorActionListener { view, action, event ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                viewModel.addWordToList(binding.word.text.toString(), true)
            }

            true
        }

        viewModel.onConfirm.observe(viewLifecycleOwner, Observer {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_WALLET_DATA,
                    configData = viewModel.getConfiguration()
                )
            )

            navigate(
                R.id.nameWalletFragment,
                bundle,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        })

        viewModel.onAddWord.observe(viewLifecycleOwner, Observer {
            binding.seedPhrase.nextWord(it)
        })

        viewModel.onInputRejected.observe(viewLifecycleOwner, Observer {
            onInvalidBitcoinAddressAnimation()
        })

        viewModel.onDisplayExplanation.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), it, true)
        })
    }

    fun onInvalidBitcoinAddressAnimation() {
        val shake: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.shake)
        binding.word.startAnimation(shake)
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.recoveryPhraseImportFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}