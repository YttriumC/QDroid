package ng.i.sav.log

import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.MarkerFactory
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

class SLF4JServiceProviderImpl : SLF4JServiceProvider {
    private val loggerFactory = LoggerFactoryImpl()
    private val mdcAdapter = DMCAdaptorImpl()
    private val markerFactory = MarkerFactory.getIMarkerFactory()

    companion object {
        var REQUESTED_API_VERSION = "2.0.0"
    }

    override fun getLoggerFactory(): ILoggerFactory {
        return loggerFactory
    }

    override fun getMarkerFactory(): IMarkerFactory {
        return markerFactory
    }

    override fun getMDCAdapter(): MDCAdapter {
        return mdcAdapter
    }

    override fun getRequestedApiVersion(): String {
        return REQUESTED_API_VERSION
    }

    override fun initialize() {

    }

}
