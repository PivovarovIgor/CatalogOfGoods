package ru.brauer.catalogofgoods.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.domain.BackgroundLoadingState
import ru.brauer.catalogofgoods.domain.IRepository
import ru.brauer.catalogofgoods.rx.ISchedulerProvider
import javax.inject.Inject

class LoadingGoodsService : Service() {

    @Inject lateinit var repository: IRepository
    @Inject lateinit var compositeDisposable: CompositeDisposable
    @Inject lateinit var schedulerProvider: ISchedulerProvider

    private var disposable: Disposable? = null
    private var notificationService: NotificationManager? = null

    private val processingLoadingObserver = object : Observer<BackgroundLoadingState.LoadingState> {

        private var processingDisposable: Disposable? = null

        override fun onSubscribe(disposableOnSubscribe: Disposable) {
            if (processingDisposable?.isDisposed == false) {
                processingDisposable?.dispose()
            }
            processingDisposable = disposableOnSubscribe
        }

        override fun onNext(processing: BackgroundLoadingState.LoadingState) {
           // backgroundProcessing.postValue(processing)
            val not = createNotification(processing.count.toString())
            notificationService?.notify(NOTIFY_ID, not)
            println("Loading ${processing.count}")
        }

        override fun onError(exeption: Throwable) {
            val not = createNotification(exeption.message.toString())
            notificationService?.notify(NOTIFY_ID, not)
        }

        override fun onComplete() {
            stopForeground(NOTIFY_ID)
        }
    }

    override fun onCreate() {
        App.instance.appComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //val PendingIntent: PendingIntent = Inte

        startForeground(NOTIFY_ID, createNotification("started"))
        beginLoadingData()
        return START_STICKY
    }

    private fun createNotification(text: String): Notification {

        if (notificationService == null) {
            val notCan =
                NotificationChannel("Loading service", "Loading", NotificationManager.IMPORTANCE_HIGH)
            notificationService = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationService?.createNotificationChannel(notCan)
        }
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Loading catalog")
            .setContentTitle(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun beginLoadingData() {
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
        disposable = getData()
    }

    private fun getData(): Disposable {
        return repository
            .getGoods(processingLoadingObserver)
            .observeOn(schedulerProvider.ui())
            .doOnDispose {
                Toast.makeText(applicationContext, "Loading is stop", Toast.LENGTH_LONG).show()
            }
            .subscribe({
                Toast.makeText(applicationContext, "Loading is success", Toast.LENGTH_LONG).show()
            }, {
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
            }).also { compositeDisposable.add(it) }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        repository.disposeObservables()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "Loading service"
        private const val NOTIFY_ID = 1
    }
}