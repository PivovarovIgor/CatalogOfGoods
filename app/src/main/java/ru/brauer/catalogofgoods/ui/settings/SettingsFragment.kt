package ru.brauer.catalogofgoods.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.terrakok.cicerone.Router
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.settings.FtpSettingsData
import ru.brauer.catalogofgoods.databinding.FragmentCatalogOfGoodsBinding
import ru.brauer.catalogofgoods.databinding.FragmentSettingsBinding
import ru.brauer.catalogofgoods.di.viewmodel.ViewModelFactory
import ru.brauer.catalogofgoods.ui.IScreens
import javax.inject.Inject

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private var binding: FragmentSettingsBinding? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var screens: IScreens

    private val viewModel: SettingsViewModel by lazy {
        ViewModelProvider(
            this@SettingsFragment,
            viewModelFactory
        )[SettingsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentSettingsBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        App.instance.appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)
        viewModel.liveData.observe(viewLifecycleOwner, ::renderData)
        binding?.run {
            buttonApply.setOnClickListener {
                viewModel.saveSettings(
                    FtpSettingsData(
                        hostAddress = textInputFtpHostAddress.text.toString(),
                        path = textInputPath.text.toString(),
                        login = textInputFtpLogin.text.toString(),
                        password = textInputPassword.text.toString()
                    )
                )
            }
        }
    }

    private fun renderData(ftpSettingsData: FtpSettingsData?) {
        binding?.run {
            textInputFtpHostAddress.setText(ftpSettingsData?.hostAddress)
            textInputPath.setText(ftpSettingsData?.path)
            textInputFtpLogin.setText(ftpSettingsData?.login)
            textInputPassword.setText(ftpSettingsData?.password)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}