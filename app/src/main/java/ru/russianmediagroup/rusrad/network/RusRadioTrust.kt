package ru.russianmediagroup.rusrad.network

import okhttp3.CertificatePinner
import okio.Buffer
import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.*
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class RusRadioTrust {

    var trustManager: X509TrustManager
        internal set

    init {
        try {
            trustManager = trustManagerForCertificates(trustedCertificatesInputStream())
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Returns an input stream containing one or more certificate PEM files. This implementation just
     * embeds the PEM files in Java strings; most applications will instead read this from a resource
     * file that gets bundled with the application.
     */
    private fun trustedCertificatesInputStream(): InputStream {
        // PEM files for root certificates of Comodo and Entrust. These two CAs are sufficient to view
        // https://publicobject.com (Comodo) and https://squareup.com (Entrust). But they aren't
        // sufficient to connect to most HTTPS sites including https://godaddy.com and https://visa.com.
        // Typically developers will need to get a PEM file from their organization's TLS administrator.

        val rusradioCert = "" +
                "-----BEGIN CERTIFICATE-----\n" +
                "MIIGNzCCBR+gAwIBAgISA/z2VMdFGIk7Hzh+GVFWG5/NMA0GCSqGSIb3DQEBCwUA\n" +
                "MEoxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MSMwIQYDVQQD\n" +
                "ExpMZXQncyBFbmNyeXB0IEF1dGhvcml0eSBYMzAeFw0xODA0MTMyMDE5MDdaFw0x\n" +
                "ODA3MTIyMDE5MDdaMBYxFDASBgNVBAMTC3J1c3JhZGlvLnJ1MIIBIjANBgkqhkiG\n" +
                "9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsO6f4kutGg7WnKzYqsOyXO811UK14WTvo36l\n" +
                "Ml7J0rl9R6wXBdpkgslLFZ6kjSwl39Gg5gZoxwSe5Y338VjQgrZoOcw6Llv6oudm\n" +
                "XykvJUpO8fiC+0n4oVKm+0LqifEReHVpsl/rNv9kF7qtYpsX5tbaIo/y5b7wh750\n" +
                "PTAaOruPOd9lXte7hayHmyAA/NuSb/bl9R4JDh7DVRt8abl3fgKzxXEaAb5FjVpj\n" +
                "QqBxDxDUWBf9j1XSn6Y3T9VodIkVWZVyifyLkNjB63K2RqqwmIZgEuQnTPrBuRdZ\n" +
                "+F7XNcY/bMWE6+hUNzQ311f3cp/TysDjd0V3YxsoYEt4RMwBqQIDAQABo4IDSTCC\n" +
                "A0UwDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcD\n" +
                "AjAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBQb6rS+fyToSMwOPFzf0r6YNIuKlDAf\n" +
                "BgNVHSMEGDAWgBSoSmpjBH3duubRObemRWXv86jsoTBvBggrBgEFBQcBAQRjMGEw\n" +
                "LgYIKwYBBQUHMAGGImh0dHA6Ly9vY3NwLmludC14My5sZXRzZW5jcnlwdC5vcmcw\n" +
                "LwYIKwYBBQUHMAKGI2h0dHA6Ly9jZXJ0LmludC14My5sZXRzZW5jcnlwdC5vcmcv\n" +
                "ME4GA1UdEQRHMEWCEWthemFuLnJ1c3JhZGlvLnJ1ghJuZXdkZXYucnVzcmFkaW8u\n" +
                "cnWCC3J1c3JhZGlvLnJ1gg93d3cucnVzcmFkaW8ucnUwgf4GA1UdIASB9jCB8zAI\n" +
                "BgZngQwBAgEwgeYGCysGAQQBgt8TAQEBMIHWMCYGCCsGAQUFBwIBFhpodHRwOi8v\n" +
                "Y3BzLmxldHNlbmNyeXB0Lm9yZzCBqwYIKwYBBQUHAgIwgZ4MgZtUaGlzIENlcnRp\n" +
                "ZmljYXRlIG1heSBvbmx5IGJlIHJlbGllZCB1cG9uIGJ5IFJlbHlpbmcgUGFydGll\n" +
                "cyBhbmQgb25seSBpbiBhY2NvcmRhbmNlIHdpdGggdGhlIENlcnRpZmljYXRlIFBv\n" +
                "bGljeSBmb3VuZCBhdCBodHRwczovL2xldHNlbmNyeXB0Lm9yZy9yZXBvc2l0b3J5\n" +
                "LzCCAQIGCisGAQQB1nkCBAIEgfMEgfAA7gB1AFWB1MIWkDYBSuoLm1c8U/DA5Dh4\n" +
                "cCUIFy+jqh0HE9MMAAABYsDgSp4AAAQDAEYwRAIgSep41kDCQla64UarkOCXL/NA\n" +
                "u+fCBdfW+Kw7Wt3YjcQCIGQcLQHQYIj0rFAxqSbb9nFV7eQAUXTWO7opbnM2JMWt\n" +
                "AHUAKTxRllTIOWW6qlD8WAfUt2+/WHopctykwwz05UVH9HgAAAFiwOBKpwAABAMA\n" +
                "RjBEAiBrfyQl41t+qGfFol6/i1rz0nZd+bQ4g03aH77zXF99fgIgeN1gg1bYxFIM\n" +
                "VK8/lE+VPb5Zbbe0uRNEAPrJ3uN/Er8wDQYJKoZIhvcNAQELBQADggEBAGZnNa6m\n" +
                "3PKuJJ4uh0gakXcI4Jp1fr4zTsJHwxXgcQMxzVCvGT/69kps/XFfFF+9FUYpIIfA\n" +
                "Wu9G5WAlqtwxSbMMtbq0wC/FS8TOgJCckcTp9QZoIHtIMqV3gnjSyZcH9rpmswvx\n" +
                "bRWDwDPDrfO4XDIzVWfI8ufpGXOeVMI2LjiyDA2D3wksbmPGjXznhxLbMJd+dJnk\n" +
                "LcmfI6sYOry84Zj7R7Ti9kbP5gt8Ski698XPL3W0UV+D+r9i5es2659qh+JsZupM\n" +
                "b+YfctIqs44Lpff5rqLybYhIBebcT4RW4rwpTyK3xtr73qYiL/qm/t3AYrfPorpJ\n" +
                "TrcGtxNvl5drES8=\n" +
                "-----END CERTIFICATE-----\n"


        val digitalOcean = "" +
                "-----BEGIN CERTIFICATE-----\n" +
                "MIIEkjCCA3qgAwIBAgIQCgFBQgAAAVOFc2oLheynCDANBgkqhkiG9w0BAQsFADA/\n" +
                "MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMT\n" +
                "DkRTVCBSb290IENBIFgzMB4XDTE2MDMxNzE2NDA0NloXDTIxMDMxNzE2NDA0Nlow\n" +
                "SjELMAkGA1UEBhMCVVMxFjAUBgNVBAoTDUxldCdzIEVuY3J5cHQxIzAhBgNVBAMT\n" +
                "GkxldCdzIEVuY3J5cHQgQXV0aG9yaXR5IFgzMIIBIjANBgkqhkiG9w0BAQEFAAOC\n" +
                "AQ8AMIIBCgKCAQEAnNMM8FrlLke3cl03g7NoYzDq1zUmGSXhvb418XCSL7e4S0EF\n" +
                "q6meNQhY7LEqxGiHC6PjdeTm86dicbp5gWAf15Gan/PQeGdxyGkOlZHP/uaZ6WA8\n" +
                "SMx+yk13EiSdRxta67nsHjcAHJyse6cF6s5K671B5TaYucv9bTyWaN8jKkKQDIZ0\n" +
                "Z8h/pZq4UmEUEz9l6YKHy9v6Dlb2honzhT+Xhq+w3Brvaw2VFn3EK6BlspkENnWA\n" +
                "a6xK8xuQSXgvopZPKiAlKQTGdMDQMc2PMTiVFrqoM7hD8bEfwzB/onkxEz0tNvjj\n" +
                "/PIzark5McWvxI0NHWQWM6r6hCm21AvA2H3DkwIDAQABo4IBfTCCAXkwEgYDVR0T\n" +
                "AQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwfwYIKwYBBQUHAQEEczBxMDIG\n" +
                "CCsGAQUFBzABhiZodHRwOi8vaXNyZy50cnVzdGlkLm9jc3AuaWRlbnRydXN0LmNv\n" +
                "bTA7BggrBgEFBQcwAoYvaHR0cDovL2FwcHMuaWRlbnRydXN0LmNvbS9yb290cy9k\n" +
                "c3Ryb290Y2F4My5wN2MwHwYDVR0jBBgwFoAUxKexpHsscfrb4UuQdf/EFWCFiRAw\n" +
                "VAYDVR0gBE0wSzAIBgZngQwBAgEwPwYLKwYBBAGC3xMBAQEwMDAuBggrBgEFBQcC\n" +
                "ARYiaHR0cDovL2Nwcy5yb290LXgxLmxldHNlbmNyeXB0Lm9yZzA8BgNVHR8ENTAz\n" +
                "MDGgL6AthitodHRwOi8vY3JsLmlkZW50cnVzdC5jb20vRFNUUk9PVENBWDNDUkwu\n" +
                "Y3JsMB0GA1UdDgQWBBSoSmpjBH3duubRObemRWXv86jsoTANBgkqhkiG9w0BAQsF\n" +
                "AAOCAQEA3TPXEfNjWDjdGBX7CVW+dla5cEilaUcne8IkCJLxWh9KEik3JHRRHGJo\n" +
                "uM2VcGfl96S8TihRzZvoroed6ti6WqEBmtzw3Wodatg+VyOeph4EYpr/1wXKtx8/\n" +
                "wApIvJSwtmVi4MFU5aMqrSDE6ea73Mj2tcMyo5jMd6jmeWUHK8so/joWUoHOUgwu\n" +
                "X4Po1QYz+3dszkDqMp4fklxBwXRsW10KXzPMTZ+sOPAveyxindmjkW8lGy+QsRlG\n" +
                "PfZ+G6Z6h7mjem0Y+iWlkYcV4PIWL1iwBi8saCbGS5jN2p8M+X+Q7UNKEkROb3N6\n" +
                "KOqkqm57TH2H3eDJAkSnh6/DNFu0Qg==\n" +
                "-----END CERTIFICATE-----\n"

        return Buffer()
                .writeUtf8(rusradioCert)
                .writeUtf8(digitalOcean)
                .inputStream()
    }

    /**
     * Returns a trust manager that trusts `certificates` and none other. HTTPS services whose
     * certificates have not been signed by these certificates will fail with a `SSLHandshakeException`.
     *
     *
     *
     * This can be used to replace the host platform's built-inputStream trusted certificates with a custom
     * set. This is useful inputStream development where certificate authority-trusted certificates aren't
     * available. Or inputStream production, to avoid reliance on third-party certificate authorities.
     *
     *
     *
     * See also [CertificatePinner], which can limit trusted certificates while still using
     * the host platform's built-inputStream trust store.
     *
     *
     * <h3>Warning: Customizing Trusted Certificates is Dangerous!</h3>
     *
     *
     *
     * Relying on your own trusted certificates limits your server team's ability to update their
     * TLS certificates. By installing a specific set of trusted certificates, you take on additional
     * operational complexity and limit your ability to migrate between certificate authorities. Do
     * not use custom trusted certificates inputStream production without the blessing of your server's TLS
     * administrator.
     */
    @Throws(GeneralSecurityException::class)
    private fun trustManagerForCertificates(inputStream: InputStream): X509TrustManager {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificates = certificateFactory.generateCertificates(inputStream)
        if (certificates.isEmpty()) {
            throw IllegalArgumentException("expected non-empty set of trusted certificates")
        }

        // Put the certificates a key store.
        val password = "password".toCharArray() // Any password will work.
        val keyStore = newEmptyKeyStore(password)
        var index = 0
        for (certificate in certificates) {
            val certificateAlias = Integer.toString(index++)
            keyStore.setCertificateEntry(certificateAlias, certificate)
        }

        // Use it to build an X509 trust manager.
        val keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, password)
        val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
            throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
        }
        return trustManagers[0] as X509TrustManager
    }

    @Throws(GeneralSecurityException::class)
    private fun newEmptyKeyStore(password: CharArray): KeyStore {
        try {
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            val inputStream: InputStream? = null // By convention, 'null' creates an empty key store.
            keyStore.load(inputStream, password)
            return keyStore
        } catch (e: IOException) {
            throw AssertionError(e)
        }

    }


}