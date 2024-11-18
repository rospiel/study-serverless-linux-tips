#!/usr/bin/env bash
#chmod +x deploy.sh
set -ex

mvn -f ShortenerFunction clean package

aws configure set default.region us-east-1 

aws cloudformation update-stack \
  --stack-name "shortener-network" \
  --template-file "network.template.yaml"

aws cloudformation update-stack \
  --stack-name "shortener-database" \
  --template-file "database.template.yaml"  

sam deploy -t "template.yaml" \
  --stack-name "shortener-stack" \
  --capabilities CAPABILITY_IAM \
  --resolve-s3

