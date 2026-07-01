package com.segnities007

import com.segnities007.service.DetectionService
import com.segnities007.service.FileStorage
import com.segnities007.service.IoTControlService
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
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

            single<Database> {
                Database.connect(
                    url = "jdbc:sqlite:detections.db",
                    driver = "org.sqlite.JDBC",
                )
            }

            single { FileStorage() }
            single { DetectionService(get()) }
            single { IoTControlService() }
        })
    }
}
