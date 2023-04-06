package com.example.processaidlmain

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import com.example.processaidlmain.databinding.ActivityMainBinding
import com.example.processaidlservice.MyAIDLInterface
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var serviceConnection: ServiceConnection
    var progressCoroutineJob: Job? = null
    var myAIDLInterface : MyAIDLInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        serviceConnection = object:ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // MyAIDLInterface를 받음
                myAIDLInterface = MyAIDLInterface.Stub.asInterface(service)
                // 10번 메세지 전달
                myAIDLInterface?.start()
                // 노래 총시간을 가져옴
                binding.messengerProgress.max = myAIDLInterface!!.maxDuration
                // 코루틴을 통해서 프로그래스바 진행
                val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
                progressCoroutineJob = backgroundScope.launch {
                    while(binding.messengerProgress.progress < binding.messengerProgress.max){
                        delay(1000)
                        binding.messengerProgress.incrementProgressBy(1000)
                    }
                    myAIDLInterface?.stop()
                    unbindService(serviceConnection)
                    binding.messengerProgress.progress = 0
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {

            }

        }

        binding.messengerPlay.setOnClickListener {
            val intent = Intent("ACTION_SERVICE_AIDL")
            intent.setPackage("com.example.processaidlservice")
            bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE)
        }
        binding.messengerStop.setOnClickListener {
            myAIDLInterface?.stop()
            unbindService(serviceConnection)
            progressCoroutineJob?.cancel()
            binding.messengerProgress.progress = 0
        }
    }
}