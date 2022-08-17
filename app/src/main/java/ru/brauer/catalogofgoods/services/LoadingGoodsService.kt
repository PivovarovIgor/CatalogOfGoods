package ru.brauer.catalogofgoods.services

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.domain.BackgroundLoadingState
import ru.brauer.catalogofgoods.domain.IRepository
import ru.brauer.catalogofgoods.rx.ISchedulerProvider
import ru.brauer.catalogofgoods.ui.MainActivity
import javax.inject.Inject

class LoadingGoodsService : Service() {

    @Inject
    lateinit var repository: IRepository
    @Inject
    lateinit var compositeDisposable: CompositeDisposable
    @Inject
    lateinit var schedulerProvider: ISchedulerProvider
    @Inject
    lateinit var channel: BackgroundLoadingStateChannel

    private var disposable: Disposable? = null
    private var coroutineScope: CoroutineScope? = null
    private var notificationService: NotificationManagerCompat? = null
    private var notificationBuilder: NotificationCompat.Builder? = null

    private val processingLoadingObserver = object : Observer<BackgroundLoadingState.LoadingState> {

        private var processingDisposable: Disposable? = null

        override fun onSubscribe(disposableOnSubscribe: Disposable) {
            if (processingDisposable?.isDisposed == false) {
                processingDisposable?.dispose()
            }
            processingDisposable = disposableOnSubscribe
        }

        override fun onNext(processing: BackgroundLoadingState.LoadingState) {
            channel.trySend(processing)
        }

        override fun onError(exception: Throwable) {
            channel.trySend(BackgroundLoadingState.Error(exception))
            completeLoadingData()
        }

        override fun onComplete() {
            channel.trySend(BackgroundLoadingState.Complete)
            completeLoadingData()
        }
    }

    private fun completeLoadingData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        coroutineScope?.cancel()
        coroutineScope = null
    }

    override fun onCreate() {
        App.instance.appComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFY_ID,
                createNotification("started"),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        }
        beginLoadingData()
        return START_STICKY
    }

    private fun createNotification(text: String): Notification {

        if (notificationService == null) {
            val notCan =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelName = getString(R.string.channel_name_of_loading_service)
                    NotificationChannel(
                        CHANNEL_ID,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH
                    )
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

            if (notificationBuilder == null) {
                val pendingIntent = Intent(this, MainActivity::class.java)
                    .let { intent ->
                        TaskStackBuilder.create(this)
                            .addNextIntent(intent)
                    }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.ic_baseline_cloud_download_24
                        )
                    )
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .setProgress(0, 0, true)
                    .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                    .setCategory(Notification.CATEGORY_PROGRESS)
            }
            notificationBuilder?.let {
                it.setContentTitle(text)
                it.setTicker(text)
                it.build()
            } ?: throw IllegalStateException()

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
        coroutineScope?.cancel()
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            .apply {
                launch {
                    channel.backgroundLoadingState.collect { state ->
                        when(state) {
                            is BackgroundLoadingState.Complete -> { }
                            is BackgroundLoadingState.LoadingState -> {
                                val not = createNotification(state.count.toString())
                                notificationService?.notify(NOTIFY_ID, not)
                            }
                            is BackgroundLoadingState.Error -> {
                                val not = createNotification(state.exception.message.toString())
                                notificationService?.notify(NOTIFY_ID, not)
                            }
                        }
                    }
            }
        }
        disposable = getData()
    }

    private fun getData(): Disposable {
        return repository
            .getGoods(processingLoadingObserver)
            .observeOn(schedulerProvider.ui())
            .doOnDispose {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.updating_data_is_stop),
                    Toast.LENGTH_LONG
                ).show()
            }
            .subscribe({
                Toast.makeText(
                    applicationContext,
                    getString(R.string.updating_data_is_start),
                    Toast.LENGTH_LONG
                ).show()
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