package com.example

import android.app.Application
import com.example.core.filter.AllowlistManager
import com.example.core.filter.AllowlistManagerImpl
import com.example.core.filter.BlocklistManager
import com.example.core.filter.BlocklistManagerImpl
import com.example.core.filter.DnsFilter
import com.example.core.filter.DnsFilterImpl
import com.example.core.filter.DomainClassifier
import com.example.core.filter.DomainClassifierImpl
import com.example.data.local.DataStoreManager
import com.example.data.local.SabrDatabase
import com.example.data.repository.ProtectionRepository
import com.example.data.repository.ProtectionRepositoryImpl
import com.example.domain.manager.ProtectionStateManager
import com.example.domain.manager.ProtectionStateManagerImpl
import com.example.domain.manager.UnblockSessionManager
import com.example.domain.manager.UnblockSessionManagerImpl
import com.example.domain.manager.VpnController
import com.example.domain.manager.VpnControllerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SabrApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { SabrDatabase.getInstance(this) }
    val dataStoreManager by lazy { DataStoreManager(this) }

    val allowlistManager: AllowlistManager by lazy {
        AllowlistManagerImpl(database.customRuleDao(), applicationScope)
    }

    val blocklistManager: BlocklistManager by lazy {
        BlocklistManagerImpl(database.customRuleDao(), applicationScope)
    }

    val domainClassifier: DomainClassifier by lazy {
        DomainClassifierImpl(allowlistManager, blocklistManager)
    }

    val dnsFilter: DnsFilter by lazy {
        DnsFilterImpl(domainClassifier)
    }

    val vpnController: VpnController by lazy {
        VpnControllerImpl()
    }

    val unblockSessionManager: UnblockSessionManager by lazy {
        UnblockSessionManagerImpl(dataStoreManager)
    }

    val protectionStateManager: ProtectionStateManager by lazy {
        ProtectionStateManagerImpl(unblockSessionManager, vpnController)
    }

    val repository: ProtectionRepository by lazy {
        ProtectionRepositoryImpl(
            protectionStateManager = protectionStateManager,
            dataStoreManager = dataStoreManager,
            customRuleDao = database.customRuleDao(),
            filterLogDao = database.filterLogDao(),
            domainClassifier = domainClassifier,
            dnsFilter = dnsFilter,
            blocklistManager = blocklistManager,
            allowlistManager = allowlistManager,
            externalScope = applicationScope
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}
