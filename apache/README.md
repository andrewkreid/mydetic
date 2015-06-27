# Apache Configuration

This directory contains files and instructions for deploying
the MyDetic REST API as a WSGI service in the [Apache](http://httpd.apache.org/)
web server.

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
sudo yum install httpd mod_ssl mod_wsgi

# Generate private key 
openssl genrsa -out ca.key 2048 

# Generate CSR 
openssl req -new -key ca.key -out ca.csr

# Generate Self Signed Key
openssl x509 -req -days 365 -in ca.csr -signkey ca.key -out ca.crt

# Copy the files to the correct locations
cp ca.crt /etc/pki/tls/certs
cp ca.key /etc/pki/tls/private/ca.key
cp ca.csr /etc/pki/tls/private/ca.csr

```

Edit ```/etc/httpd/conf.d/ssl.conf``` and make the following changes

```
SSLCertificateFile /etc/pki/tls/certs/ca.crt
SSLCertificateKeyFile /etc/pki/tls/private/ca.key
```

Restart Apache
```
/etc/init.d/httpd restart
```

You should be able to browse to https://<external_instance_dns>/ and see the test
page.

### Get the MyDetic server source

sudo yum install git


### configure VirtualEnv

cd $HOME
virtualenv mydetic-env
source mydetic-env/bin/activate

cd src/mydetic
pip install -r requirements.txt
python ./setup.py install
 
### customise WSGI file

edit ```mydetic.wsgi```. Change the location of the virtualenv files.

sudo mkdir -p /var/www/mydetic
sudo cp mydetic.wsgi /var/www/mydetic
