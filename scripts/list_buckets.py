#!/usr/bin/env python

import boto
from boto.s3.key import Key
import datetime

s3 = boto.s3.connect_to_region('ap-southeast-2')

dev_bucket = s3.get_bucket('mydetic-dev.ghosttrails.net')

k = Key(dev_bucket)
k.key = 'test1'
k.set_contents_from_string('foooooo')


#for bucket in s3.get_all_buckets():
#    print bucket
