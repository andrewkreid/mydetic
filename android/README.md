This directory contains the source for the MyDetic Android app

## Installing the Self-Signed Certificate

Note: As of Android P, this doesn't seem to work anymore. You'll need a real cert :(.

Android's HTTP libraries check SSL certificate validity, and make this hard
to disable to encourage good security.

To allow a self-signed certificate (which the MyDetic REST API will probably
have), you need to do the following:

(instructions taken from [this CodeProject page](http://www.codeproject.com/Articles/826045/Android-security-Implementation-of-Self-signed-SSL))

Download [bcprov-jdk15on-146.jar](http://www.bouncycastle.org/download/bcprov-jdk15on-146.jar).

Find the public SSL key you installed into Apache (`/etc/pki/tls/certs/ca.crt`) and copy it to a text file locally.

Run

    keytool -import -alias mydetic -file ca.crt \
        -keystore mydeticssl.bks -storetype BKS -providerClass \
        org.bouncycastle.jce.provider.BouncyCastleProvider \
        -providerpath bcprov-jdk15on-146.jar

Set the password to `mydetic`

Copy the resulting `mydeticssl.bks` file to `MyDetic/app/src/main/res/raw/`
