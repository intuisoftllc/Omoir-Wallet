package com.intuisoft.plaid.features.settings.ui

import android.content.res.Configuration
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.uriFromResource
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventMemeView
import com.intuisoft.plaid.common.coroutines.OmoirScope
import com.intuisoft.plaid.common.model.AppTheme
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.FragmentEasterEggBinding
import kotlinx.android.synthetic.main.fragment_easter_egg.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class MemeFragment : ConfigurableFragment<FragmentEasterEggBinding>(
    pinProtection = false,
    requiresWallet = false
) {

    private val localStoreRepository: LocalStoreRepository by inject()
    private val eventTracker: EventTracker by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEasterEggBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        // do nothing
        eventTracker.log(EventMemeView())
        var theme = localStoreRepository.getAppTheme()
        if(theme == AppTheme.AUTO) {
            val nightModeFlags = requireContext().resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
            theme = when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> AppTheme.DARK
                Configuration.UI_MODE_NIGHT_NO -> AppTheme.LIGHT
                else -> AppTheme.LIGHT
            }
        }

        when(theme) {
            AppTheme.LIGHT, AppTheme.DARK -> {
                binding.videoView.setVideoURI(
                    uriFromResource(R.raw.meme_video_1)
                )

                videoView.setOnErrorListener { mediaPlayer, what, extra ->
                    Toast.makeText(requireContext(),
                        "error loading video", Toast.LENGTH_SHORT).show();
                    onVideoComplete()
                    true
                }
                videoView.start()
                videoView.setOnCompletionListener(OnCompletionListener {
                    onVideoComplete()
                })
            }
        }
    }

    fun onVideoComplete() {
        OmoirScope.MainScope.launch {
            delay(350)
            withBinding {
                val mPlayer: MediaPlayer =
                    MediaPlayer.create(requireContext(), R.raw.pop_sound)
                mPlayer.start()

                memeUnleashedTitle.isVisible = true
                meme1.isVisible = true
                meme2.isVisible = true
                meme3.isVisible = true
                meme4.isVisible = true
            }

            delay(1350)
            withBinding {
                val mPlayer: MediaPlayer =
                    MediaPlayer.create(requireContext(), R.raw.confetti_sound)
                mPlayer.start()
                val party = Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                    position = Position.Relative(0.5, 0.3)
                )
                konfettiView.start(party)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN
    }

    override fun actionBarTitle(): Int {
        return R.string.easter_egg_screen_title
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun navigationId(): Int {
        return R.id.memeFragment
    }
}