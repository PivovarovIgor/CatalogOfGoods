package ru.brauer.catalogofgoods.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
    private var notificationService: NotificationManagerCompat? = null

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    override fun onCreate() {
        App.instance.appComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //val PendingIntent: PendingIntent = Inte

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFY_ID, createNotification("started"), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }
        beginLoadingData()
        return START_STICKY
    }

    private fun createNotification(text: String): Notification {

        if (notificationService == null) {
            val notCan =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelName = getString(R.string.channel_name_of_loading_service)
                    NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
                        .apply {
                            enableLights(true)
                            enableVibration(false)
                            description = channelName
                            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                        }
                } else {
                    TODO("VERSION.SDK_INT < O")
                }

            notificationService = NotificationManagerCompat.from(this)

            notificationService?.createNotificationChannel(notCan)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(text)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_baseline_cloud_download_24))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setTicker(text)
                .setOnlyAlertOnce(true)
                .setProgress(0,0,true)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .build()
        } else {
            TODO("VERSION.SDK_INT < S")
        }
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