package ru.brauer.catalogofgoods.ui.catalogofgoods

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nhaarman.mockito_kotlin.*
import io.mockk.every
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.Flow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations.openMocks
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.domain.IRepository
import ru.brauer.catalogofgoods.stubs.SchedulerProviderStub

class CatalogOfGoodsViewModelTest {

    private lateinit var viewModel: CatalogOfGoodsViewModel

    @Mock
    private lateinit var repository: IRepository

    @Mock
    private lateinit var compositeDisposable: CompositeDisposable

    private lateinit var mocks: AutoCloseable

    @Before
    fun setup() {
        mocks = openMocks(this)

        val pagingFlow = mock<Flow<PagingData<Goods>>>()


        every { any<Flow<PagingData<Goods>>>().cachedIn(any()) } returns pagingFlow

        `when`(pagingFlow.cachedIn(any())).thenReturn(mock<Flow<PagingData<Goods>>>())
        `when`(repository.getPagingFlowFromLocalSource(any())).thenReturn(pagingFlow)
        viewModel = CatalogOfGoodsViewModel(
            repository = repository,
            compositeDisposable = compositeDisposable,
            schedulerProvider = SchedulerProviderStub()
        )
    }

    @Test
    fun loading_CommerceML_test() {
        `when`(repository.getGoods(any())).thenReturn(
            Single.just(
                listOf(Goods.empty())
            )
        )

        viewModel.observe(mock(), mock(), mock())
        verify(repository, times(1)).getGoods(any())
    }

    @After
    fun close() {
        mocks.close()
    }
}