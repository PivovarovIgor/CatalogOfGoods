package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations.openMocks
import org.robolectric.annotation.Config
import ru.brauer.catalogofgoods.TestCoroutineRule
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.domain.IRepository
import ru.brauer.catalogofgoods.services.BackgroundLoadingStateChannel
import ru.brauer.catalogofgoods.stubs.SchedulerProviderStub

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
class CatalogOfGoodsViewModelTest {

    private lateinit var viewModel: CatalogOfGoodsViewModel

    private val lifecycleOwnerFake = LifecycleOwnerFake()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Mock
    private lateinit var repository: IRepository

    @Mock
    private lateinit var compositeDisposable: CompositeDisposable

    @Mock
    private lateinit var backgroundLoadingStateChannel: BackgroundLoadingStateChannel

    private lateinit var mocks: AutoCloseable

    @Before
    fun setup() {
        mocks = openMocks(this)
        lifecycleOwnerFake.resume()
        testCoroutineRule.runBlockingTest {
            val pagingFlow: Flow<PagingData<Goods>> = flow {
                emit(PagingData.empty())
            }
            `when`(repository.getPagingFlowFromLocalSource(any())).thenReturn(pagingFlow)
            viewModel = CatalogOfGoodsViewModel(
                repository = repository,
                compositeDisposable = compositeDisposable,
                backgroundLoadingStateChannel = backgroundLoadingStateChannel,
            )
        }
    }

    @Test
    fun loading_CommerceML_test() {
        `when`(repository.getGoods(any())).thenReturn(
            Single.just(
                listOf()
            )
        )

        viewModel.observe(lifecycleOwnerFake, { })
        verify(repository, times(1)).getGoods(any())
    }

    @After
    fun close() {
        lifecycleOwnerFake.destroy()
        mocks.close()
    }
}

class LifecycleOwnerFake : LifecycleOwner {

    private var currentStateFake = Lifecycle.State.RESUMED

    private val lifecycleFake = LifecycleFake()

    private inner class LifecycleFake : Lifecycle() {

        override fun addObserver(observer: LifecycleObserver) {}

        override fun removeObserver(observer: LifecycleObserver) {}

        override fun getCurrentState(): State = currentStateFake
    }

    override fun getLifecycle(): Lifecycle = lifecycleFake

    fun destroy() {
        currentStateFake = Lifecycle.State.DESTROYED
    }

    fun resume() {
        currentStateFake = Lifecycle.State.RESUMED
    }
}