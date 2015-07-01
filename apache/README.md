# Apache Configuration

This directory contains files and instructions for deploying
the MyDetic REST API as a WSGI service in the [Apache](http://httpd.apache.org/)
web server on an EC2 instance in AWS.

**NOTE:** This isn't the best, or a perfectly secure configuration, just the bare minimum required to get going.

Background Reading:

* [Flask mod_wsgi tutorial](http://flask.pocoo.org/docs/0.10/deploying/mod_wsgi/)

## 1. Create a new EC2 instance in AWS

* t2.micro, Amazon Linux x64.
* In the "configure security group" step, make sure to open HTTP and HTTPS in addition to SSH

### Set up Apache for HTTPS

(Because we're using HTTP Basic Auth for the moment and otherwise passwords will be
sent in the clear)

(based on [these CentOS instructions](http://wiki.centos.org/HowTos/Https)).

```bash
sudo yum install httpd mod_ssl mod_wsgi-python27
```

# Generate private key
```
openssl genrsa -out ca.key 2048
```

# Generate CSR
```
openssl req -new -key ca.key -out ca.csr
```

# Generate Self Signed Key
```
openssl x509 -req -days 365 -in ca.csr -signkey ca.key -out ca.crt
```

# Copy the files to the correct locations
```
cp ca.crt /etc/pki/tls/certs
cp ca.key /etc/pki/tls/private/ca.key
cp ca.csr /etc/pki/tls/private/ca.csr

```

Edit ```/etc/httpd/conf.d/ssl.conf``` and make the following changes

```
SSLCertificateFile /etc/pki/tls/certs/ca.crt
SSLCertificateKeyFile /etc/pki/tls/private/ca.key
```

Restart Apache and configure to start on boot
```
/etc/init.d/httpd restart
chkconfig httpd on
```

You should be able to browse to https://<external_instance_dns>/ and see the test
page.

### Get the MyDetic server source

```
sudo yum install git

cd $HOME
mkdir src
cd src
git clone https://github.com/andrewkreid/mydetic.git
```

### configure VirtualEnv

```
cd $HOME
virtualenv mydetic-env
source mydetic-env/bin/activate

cd src/mydetic
pip install -r requirements.txt
cd src
python ./setup.py install
```

### customise WSGI file

edit ```$HOME/src/mydetic/apache/mydetic.wsgi```. Change the location of the virtualenv files.

```
sudo mkdir -p /var/www/mydetic
sudo cp mydetic.wsgi /var/www/mydetic
```

### Create Apache VirtualHost

```
sudo cp mydetic-wsgi.conf /etc/httpd/conf.d/
```

Edit ```/etc/httpd/conf.d/mydetic-wsgi.conf``` and replace ```YOUR_HOST_NAME_OR_IP``` with the
exernal DNS name of the instance (from the AWS console).

Notice that the ```MYDETIC_CONFIG``` and ```MYDETIC_LOGCONFIG``` environment variables i
refer to files that don't exist yet. We'll create these next.

### Create The MyDetic config file

Create ```/home/ec2-user/src/mydetic/mydetic-dev-s3.config``` using the following template:

```json
{
    "s3_config": {
        "aws_access_key_id": "YOUR_S3_ACCESS_KEY",
            "aws_secret_access_key": "YOUR_S3_SECRET_KEY",
            "bucket": "YOUR_BUCKET_NAME",
            "region": "ap-southeast-2"
    },
    "auth_config": {
        "method": "HTTP Basic",
        "store": "file",
        "store_file": "/home/ec2-user/src/mydetic/passwords.json"
    }
}

```

Customise the file with your AWS credentials and the S3 bucket name you want to use to store
MyDetic files.

### Configure Logging

Next, create a Python logging config file in ```/home/ec2-user/src/mydetic/mydetic-logging.conf```.
This file uses the [standard config file format of the Python logging library](https://docs.python.org/2/library/logging.config.html#configuration-file-format). Here's an example that logs to syslog:

```
[loggers]
keys=root

[handlers]
keys=syslog

[formatters]
keys=form01

[logger_root]
level=DEBUG
qualname=(root)
handlers=syslog

[handler_stderr]
class=StreamHandler
level=DEBUG
formatter=form01
args=(sys.stderr,)

[handler_syslog]
class=handlers.SysLogHandler
level=DEBUG
formatter=form01
host=localhost
address=/dev/log
facility=LOG_LOCAL6
args=('/dev/log', handlers.SysLogHandler.LOG_LOCAL6)

[formatter_form01]
format=mydetic %(levelname)s %(message)s
datefmt=
class=logging.Formatter
```

If you use this example, you'll need to configure ```rsyslog``` to log LOCAL6 somewhere. Create
a file in ```/etc/rsyslog.d``` containing the following:

```
local6.*        /var/log/mydetic.log
```

### Create A Password File

For now, MyDetic uses HTTP Basic Auth for authentication (log in with Google coming soon 
(hopefully)). Use the following steps to create passwords:

```
cd $HOME/src/mydetic
scripts/file_pass.py -f /home/ec2-user/src/mydetic/passwords.json -u mreynolds -p serenityrox123 set
```

### See if it all works

```
sudo /etc/init.d/rsyslog restart
sudo /etc/init.d/httpd restart
```

```
curl -u mreynolds:serenityrox123 -k https://ec2-54-153-165-166.ap-southeast-2.compute.amazonaws.com/mydetic/api/v1.0/memories?user_id=mreynolds
{"memories": [], "user_id": "mreynolds"}
```


### Troubleshooting

TODO: Running server standalone.

