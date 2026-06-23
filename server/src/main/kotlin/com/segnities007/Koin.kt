package com.segnities007

import com.segnities007.service.DetectionService
import com.segnities007.service.FileStorage
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<HelloService> {
                HelloService {
                    println(environment.log.info("Hello, World!"))
                }
            }

            single<R2dbcDatabase> {
                R2dbcDatabase.connect(
                    url = "r2dbc:h2:file:///./h2",
                    user = "root",
                    password = "",
                )
            }

            single { FileStorage() }
            single { DetectionService(get()) }
        })
    }
}
